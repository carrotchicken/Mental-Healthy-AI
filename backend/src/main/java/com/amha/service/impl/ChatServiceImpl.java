package com.amha.service.impl;

import com.amha.agent.ChatAgent;
import com.amha.common.BusinessException;
import com.amha.common.PageResult;
import com.amha.dto.*;
import com.amha.entity.ConsultationMessage;
import com.amha.entity.ConsultationSession;
import com.amha.entity.User;
import com.amha.mapper.ConsultationMessageMapper;
import com.amha.mapper.ConsultationSessionMapper;
import com.amha.mapper.UserMapper;
import com.amha.service.ChatService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ConsultationSessionMapper sessionMapper;
    private final ConsultationMessageMapper messageMapper;
    private final UserMapper userMapper;

    @Qualifier("realChatAgent")
    private final ChatAgent chatAgent;

    @Override
    public StartSessionResponse startSession(StartSessionRequest request, Long userId) {
        ConsultationSession session = new ConsultationSession();
        session.setUserId(userId);
        session.setSessionTitle(request.getSessionTitle());
        session.setStatus("ACTIVE");
        session.setStartedAt(LocalDateTime.now());
        session.setMessageCount(0);
        sessionMapper.insert(session);

        return StartSessionResponse.builder()
                .sessionId(session.getId())
                .status("ACTIVE")
                .startTime(System.currentTimeMillis())
                .initialMessage(request.getInitialMessage())
                .messageCount(0)
                .sessionTitle(request.getSessionTitle())
                .build();
    }

    @Override
    public PageResult<SessionVO> getSessionPage(SessionPageQuery query, Long userId) {
        LambdaQueryWrapper<ConsultationSession> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(ConsultationSession::getUserId, userId);
        }
        wrapper.orderByDesc(ConsultationSession::getLastMessageTime)
               .orderByDesc(ConsultationSession::getCreatedAt);

        Page<ConsultationSession> page = new Page<>(query.getPageNum(), query.getPageSize());
        IPage<ConsultationSession> result = sessionMapper.selectPage(page, wrapper);

        List<SessionVO> records = result.getRecords().stream().map(session -> {
            User user = userMapper.selectById(session.getUserId());
            return SessionVO.builder()
                    .id(session.getId())
                    .userId(session.getUserId())
                    .userNickname(user != null ? user.getNickname() : "")
                    .sessionTitle(session.getSessionTitle())
                    .startedAt(session.getStartedAt())
                    .durationMinutes(session.getDurationMinutes())
                    .messageCount(session.getMessageCount())
                    .lastMessageContent(session.getLastMessageContent())
                    .lastMessageTime(session.getLastMessageTime())
                    .build();
        }).collect(Collectors.toList());

        return PageResult.of(records, result.getCurrent(), result.getSize(), result.getTotal());
    }

    @Override
    public void deleteSession(Long sessionId, Long userId) {
        ConsultationSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }
        User user = userMapper.selectById(userId);
        if (user.getUserType() != 2 && !session.getUserId().equals(userId)) {
            throw new BusinessException("无权删除该会话");
        }
        messageMapper.delete(new LambdaQueryWrapper<ConsultationMessage>()
                .eq(ConsultationMessage::getSessionId, sessionId));
        sessionMapper.deleteById(sessionId);
    }

    @Override
    public List<ConsultationMessageVO> getSessionMessages(Long sessionId) {
        List<ConsultationMessage> messages = messageMapper.selectList(
                new LambdaQueryWrapper<ConsultationMessage>()
                        .eq(ConsultationMessage::getSessionId, sessionId)
                        .orderByAsc(ConsultationMessage::getCreatedAt));

        return messages.stream().map(msg -> {
            String content = msg.getContent();
            int contentLength = content != null ? content.length() : 0;
            String preview = content != null && content.length() > 50
                    ? content.substring(0, 50) + "..." : content;

            return ConsultationMessageVO.builder()
                    .id(msg.getId())
                    .sessionId(msg.getSessionId())
                    .senderType(msg.getSenderType())
                    .senderTypeDesc(msg.getSenderType() == 1 ? "用户" : "AI")
                    .messageType(msg.getMessageType())
                    .messageTypeDesc(msg.getMessageType() == 0 ? "文本" : "其他")
                    .content(content)
                    .contentLength(contentLength)
                    .contentPreview(preview)
                    .isError(msg.getIsError() == 1)
                    .createdAt(msg.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    public SseEmitter streamChat(ChatStreamRequest request, Long userId) {
        Long sessionId = Long.parseLong(request.getSessionId());
        ConsultationSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException("会话不存在");
        }

        // 保存用户消息
        ConsultationMessage userMsg = new ConsultationMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setSenderType(1);
        userMsg.setContent(request.getUserMessage());
        messageMapper.insert(userMsg);

        // 更新会话
        session.setMessageCount(session.getMessageCount() + 1);
        session.setLastMessageContent(request.getUserMessage());
        session.setLastMessageTime(LocalDateTime.now());
        sessionMapper.updateById(session);

        // 获取历史消息（包含刚插入的用户消息作为上下文）
        List<ConsultationMessage> history = messageMapper.selectList(
                new LambdaQueryWrapper<ConsultationMessage>()
                        .eq(ConsultationMessage::getSessionId, sessionId)
                        .orderByAsc(ConsultationMessage::getCreatedAt));

        // SSE流式响应
        SseEmitter emitter = new SseEmitter(300000L); // 5分钟超时

        final StringBuilder fullResponse = new StringBuilder();

        chatAgent.streamResponse(sessionId, request.getUserMessage(), history)
                .doOnNext(chunk -> {
                    try {
                        fullResponse.append(chunk);
                        String sseData = "{\"code\":\"200\",\"data\":{\"content\":\"" +
                                escapeJson(chunk) + "\"}}";
                        emitter.send(SseEmitter.event().name("message").data(sseData));
                    } catch (Exception e) {
                        log.error("SSE send error", e);
                    }
                })
                .doOnComplete(() -> {
                    try {
                        // 保存AI回复到数据库
                        String aiContent = fullResponse.toString();
                        if (!aiContent.isEmpty()) {
                            ConsultationMessage aiMsg = new ConsultationMessage();
                            aiMsg.setSessionId(sessionId);
                            aiMsg.setSenderType(2);
                            aiMsg.setContent(aiContent);
                            messageMapper.insert(aiMsg);

                            // 更新会话
                            session.setMessageCount(session.getMessageCount() + 1);
                            session.setLastMessageContent(aiContent);
                            session.setLastMessageTime(LocalDateTime.now());
                            if (session.getStartedAt() != null) {
                                session.setDurationMinutes((int) Duration.between(
                                        session.getStartedAt(), LocalDateTime.now()).toMinutes());
                            }
                            sessionMapper.updateById(session);
                        }

                        emitter.send(SseEmitter.event().name("done").data("{\"code\":\"200\"}"));
                        emitter.complete();
                    } catch (Exception e) {
                        log.error("SSE complete error", e);
                    }
                })
                .doOnError(e -> {
                    log.error("SSE stream error for session {}: {}", sessionId, e.getMessage());
                    try {
                        emitter.send(SseEmitter.event().name("error")
                                .data("{\"code\":\"500\",\"message\":\"AI回复失败，请稍后重试\"}"));
                        emitter.complete();
                    } catch (Exception ex) {
                        emitter.completeWithError(ex);
                    }
                })
                .subscribe();

        return emitter;
    }

    @Override
    public Map<String, Object> getSessionEmotion(Long sessionId) {
        List<ConsultationMessage> messages = messageMapper.selectList(
                new LambdaQueryWrapper<ConsultationMessage>()
                        .eq(ConsultationMessage::getSessionId, sessionId)
                        .orderByAsc(ConsultationMessage::getCreatedAt));
        return chatAgent.analyzeEmotion(sessionId, messages);
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
