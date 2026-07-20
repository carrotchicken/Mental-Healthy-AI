package com.amha.agent;

import com.amha.entity.ConsultationMessage;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * AI对话Agent接口，为后续接入真实LLM/Agent应用层预留。
 * 当前使用Mock实现，后续替换为真实AI服务。
 */
public interface ChatAgent {

    /**
     * 流式对话响应，逐词返回内容
     * @param sessionId 会话ID
     * @param userMessage 用户最新消息
     * @param history 历史上下文消息
     * @return 流式返回的文本片段
     */
    Flux<String> streamResponse(Long sessionId, String userMessage,
                                List<ConsultationMessage> history);

    /**
     * 分析会话中的情绪状态
     * @param sessionId 会话ID
     * @param messages 会话消息列表
     * @return 情绪分析结果
     */
    Map<String, Object> analyzeEmotion(Long sessionId, List<ConsultationMessage> messages);
}
