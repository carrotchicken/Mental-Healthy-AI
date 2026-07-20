package com.amha.service;

import com.amha.common.PageResult;
import com.amha.dto.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

public interface ChatService {
    StartSessionResponse startSession(StartSessionRequest request, Long userId);
    PageResult<SessionVO> getSessionPage(SessionPageQuery query, Long userId);
    void deleteSession(Long sessionId, Long userId);
    List<ConsultationMessageVO> getSessionMessages(Long sessionId);
    SseEmitter streamChat(ChatStreamRequest request, Long userId);
    Map<String, Object> getSessionEmotion(Long sessionId);
}
