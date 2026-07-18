import json
import asyncio
from typing import AsyncGenerator, List, Dict

from langchain_openai import ChatOpenAI
from langchain_core.messages import HumanMessage, AIMessage, SystemMessage

import config
from prompts.chat_prompt import CHAT_SYSTEM_PROMPT


class ChatService:
    """Handles chat conversations with DeepSeek LLM via LangChain."""

    def __init__(self):
        self.llm = ChatOpenAI(
            model=config.DEEPSEEK_MODEL,
            api_key=config.DEEPSEEK_API_KEY,
            base_url=config.DEEPSEEK_BASE_URL,
            temperature=0.7,
            max_tokens=1024,
            streaming=True,
        )

    def _build_messages(self, history: List[Dict]) -> List:
        """Build LangChain message list from conversation history."""
        messages = [SystemMessage(content=CHAT_SYSTEM_PROMPT)]

        for msg in history:
            sender_type = msg.get("senderType", 1)
            content = msg.get("content", "")
            if sender_type == 1:  # User
                messages.append(HumanMessage(content=content))
            elif sender_type == 2:  # AI
                messages.append(AIMessage(content=content))

        return messages

    async def stream_chat(
        self, messages: List[Dict], user_message: str
    ) -> AsyncGenerator[str, None]:
        """
        Stream chat response from LLM.
        Yields content chunks as they arrive from the model.
        Each chunk is a piece of text to be sent as SSE event to frontend.
        """
        history = self._build_messages(messages)
        # Add the latest user message
        history.append(HumanMessage(content=user_message))

        try:
            async for chunk in self.llm.astream(history):
                if chunk.content:
                    yield chunk.content
        except Exception as e:
            error_msg = f"[AI回复出错: {str(e)}]"
            yield error_msg

    async def get_full_response(
        self, messages: List[Dict], user_message: str
    ) -> str:
        """Get complete (non-streaming) response for saving to database."""
        history = self._build_messages(messages)
        history.append(HumanMessage(content=user_message))

        try:
            response = await self.llm.ainvoke(history)
            return response.content if response.content else ""
        except Exception as e:
            return f"[AI回复出错: {str(e)}]"


# Singleton
chat_service = ChatService()
