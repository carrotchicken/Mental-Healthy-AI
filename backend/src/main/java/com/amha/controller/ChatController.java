package com.amha.controller;

import com.amha.common.PageResult;
import com.amha.common.Result;
import com.amha.dto.*;
import com.amha.service.ChatService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/psychological-chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/session/start")
    public Result<StartSessionResponse> startSession(@RequestBody StartSessionRequest request,
                                                      HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return Result.success(chatService.startSession(request, userId));
    }

    @GetMapping("/sessions")
    public Result<PageResult<SessionVO>> getSessions(SessionPageQuery query,
                                                      HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        Integer userType = (Integer) httpRequest.getAttribute("userType");
        // 管理员查看所有会话，普通用户只看自己的
        if (userType == 2) {
            // 管理员端，不限制userId
            return Result.success(chatService.getSessionPage(query, null));
        }
        return Result.success(chatService.getSessionPage(query, userId));
    }

    @DeleteMapping("/sessions/{sessionId}")
    public Result<Void> deleteSession(@PathVariable Long sessionId,
                                       HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        chatService.deleteSession(sessionId, userId);
        return Result.success();
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public Result<List<ConsultationMessageVO>> getSessionMessages(@PathVariable Long sessionId) {
        return Result.success(chatService.getSessionMessages(sessionId));
    }

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamChat(@RequestBody ChatStreamRequest request,
                                  HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return chatService.streamChat(request, userId);
    }

    @GetMapping("/session/{sessionId}/emotion")
    public Result<Map<String, Object>> getSessionEmotion(@PathVariable String sessionId) {
        Long sid = Long.parseLong(sessionId.replace("session_", ""));
        return Result.success(chatService.getSessionEmotion(sid));
    }
}
