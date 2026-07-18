"""
改造后的 ChatService —— 具备工具调用能力的 Agent。

核心变化：从"LLM 直接回复"升级为"LLM 决定调用工具还是直接回复"。
流程：
  用户消息 → LLM（带工具定义）
    ├─ 不需要工具 → 直接流式回复（和改造前一样）
    └─ 需要工具 → 返回 tool_call → 执行工具 → 结果喂回 LLM → 再次判断
                   ↑_______________________________________________↓
                   循环直到 LLM 不再调用工具，最终流式回复

技术基础：
  - DeepSeek 兼容 OpenAI function calling 协议
  - LangChain ChatOpenAI 的 .bind_tools() 自动生成 function schema
  - 工具调用循环通过 AIMessage.tool_calls 检测
"""

import json
import logging
from typing import AsyncGenerator, List, Dict

from langchain_openai import ChatOpenAI
from langchain_core.messages import (
    HumanMessage, AIMessage, SystemMessage, ToolMessage
)

import config
from prompts.chat_prompt import CHAT_SYSTEM_PROMPT
from tools.mental_health_tools import ALL_TOOLS

logger = logging.getLogger(__name__)


class AgentChatService:
    """带工具调用能力的对话服务。"""

    # ── 工具与 LLM 绑定 ──────────────────────
    # max_tool_calls: 单轮对话最多触发几次工具调用
    MAX_TOOL_CALLS = 3

    def __init__(self):
        # 创建 LLM 实例并绑定工具
        self.base_llm = ChatOpenAI(
            model=config.DEEPSEEK_MODEL,
            api_key=config.DEEPSEEK_API_KEY,
            base_url=config.DEEPSEEK_BASE_URL,
            temperature=0.7,
            max_tokens=1024,
        )
        # 绑定了工具的 LLM：每次 invoke 时自动传入 tool schema
        self.llm_with_tools = self.base_llm.bind_tools(ALL_TOOLS)

    # ── 历史消息构建（和改造前一样） ──────────
    def _build_messages(self, history: List[Dict]) -> List:
        messages = [SystemMessage(content=CHAT_SYSTEM_PROMPT)]
        for msg in history:
            sender_type = msg.get("senderType", 1)
            content = msg.get("content", "")
            if sender_type == 1:
                messages.append(HumanMessage(content=content))
            elif sender_type == 2:
                messages.append(AIMessage(content=content))
        return messages

    # ── 工具执行 ──────────────────────────────
    def _execute_tool(self, tool_name: str, tool_args: dict) -> str:
        """根据 tool_name 查找到对应的工具函数并执行，返回结果的字符串。"""
        tool_map = {t.name: t for t in ALL_TOOLS}
        tool_fn = tool_map.get(tool_name)
        if not tool_fn:
            return "错误：未找到工具「{}」。可用工具：{}".format(
                tool_name, list(tool_map.keys()))
        try:
            result = tool_fn.invoke(tool_args)
            return str(result)
        except Exception as e:
            logger.error("工具 {} 执行失败: {}".format(tool_name, e))
            return "工具执行出错：{}".format(str(e))

    # ── 核心：带工具循环的流式对话 ─────────────
    async def stream_chat(
        self, messages: List[Dict], user_message: str
    ) -> AsyncGenerator[str, None]:
        """
        流式对话，具备工具调用能力。
        """
        history = self._build_messages(messages)
        history.append(HumanMessage(content=user_message))

        tool_call_count = 0
        should_continue = True

        while should_continue and tool_call_count < self.MAX_TOOL_CALLS:
            # 用绑定了工具的 LLM 调用
            response = await self.llm_with_tools.ainvoke(history)

            # 检查 LLM 是否想调用工具
            tool_calls = getattr(response, "tool_calls", None) or []

            if not tool_calls:
                # LLM 不需要工具 → 流式返回最终回复
                should_continue = False
                # 将 AIMessage 加入历史，让 astream 接着这个上下文生成
                history.append(response)
                async for chunk in self.base_llm.astream(history):
                    if chunk.content:
                        yield chunk.content
            else:
                # LLM 需要工具 → 执行工具 → 结果喂回
                tool_call_count += 1
                # 将 LLM 的工具调用决定加入历史
                history.append(response)

                for tc in tool_calls:
                    tool_name = tc["name"]
                    tool_args = tc["args"]
                    logger.info(
                        "工具调用 #{}/{}: {} {}".format(
                            tool_call_count, self.MAX_TOOL_CALLS,
                            tool_name, json.dumps(tool_args, ensure_ascii=False)
                        )
                    )
                    result = self._execute_tool(tool_name, tool_args)
                    # ToolMessage 是标准格式，tool_call_id 关联到具体调用
                    history.append(ToolMessage(
                        content=result,
                        tool_call_id=tc["id"]
                    ))

                # 循环：带着工具结果再次问 LLM
                # should_continue 保持 True

        if tool_call_count >= self.MAX_TOOL_CALLS:
            # 达到工具调用上限，强制让 LLM 基于已有信息回复
            history.append(HumanMessage(
                content="请基于以上所有信息，用自然语言总结并回复用户，不要再调用工具。"
            ))
            async for chunk in self.base_llm.astream(history):
                if chunk.content:
                    yield chunk.content


# Singleton
agent_chat_service = AgentChatService()
