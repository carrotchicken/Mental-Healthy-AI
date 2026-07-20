package com.amha.agent;

import com.amha.entity.ConsultationMessage;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;

/**
 * 真实AI Agent实现，通过HTTP调用Python Agent服务。
 * 使用WebClient进行非阻塞HTTP通信。
 */
@Slf4j
@Component("realChatAgent")
@org.springframework.context.annotation.Primary
public class RealChatAgent implements ChatAgent {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public RealChatAgent(@Value("${agent.base-url}") String agentBaseUrl,
                         ObjectMapper objectMapper) {
        this.webClient = WebClient.builder()
                .baseUrl(agentBaseUrl)
                .build();
        this.objectMapper = objectMapper;
    }

    @Override
    public Flux<String> streamResponse(Long sessionId, String userMessage,
                                       List<ConsultationMessage> history) {
        List<Map<String, Object>> messages = new ArrayList<>();
        if (history != null && !history.isEmpty()) {
            // 跳过最后一条用户消息（即当前消息），因为它已作为 userMessage 单独传递
            int endIndex = history.size();
            if (history.get(endIndex - 1).getSenderType() == 1) {
                endIndex--;
            }
            for (int i = 0; i < endIndex; i++) {
                ConsultationMessage msg = history.get(i);
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("senderType", msg.getSenderType());
                m.put("content", msg.getContent() != null ? msg.getContent() : "");
                messages.add(m);
            }
        }

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("sessionId", String.valueOf(sessionId));
        requestBody.put("messages", messages);
        requestBody.put("userMessage", userMessage);

        return webClient.post()
                .uri("/api/agent/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .accept(MediaType.APPLICATION_NDJSON)
                .retrieve()
                .bodyToFlux(String.class)
                .map(line -> {
                    try {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> parsed = objectMapper.readValue(line, Map.class);
                        @SuppressWarnings("unchecked")
                        Map<String, Object> data = (Map<String, Object>) parsed.get("data");
                        if (data != null && data.get("content") != null) {
                            return (String) data.get("content");
                        }
                    } catch (Exception e) {
                        log.warn("Failed to parse agent response line: {}", e.getMessage());
                    }
                    return "";
                })
                .filter(content -> !content.isEmpty())
                .onErrorResume(e -> {
                    log.error("Agent chat stream error: {}", e.getMessage());
                    return Flux.just("[AI服务暂时不可用，请稍后重试]");
                });
    }

    @Override
    public Map<String, Object> analyzeEmotion(Long sessionId,
                                               List<ConsultationMessage> messages) {
        List<Map<String, Object>> msgList = new ArrayList<>();
        if (messages != null) {
            for (ConsultationMessage msg : messages) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("senderType", msg.getSenderType());
                m.put("content", msg.getContent() != null ? msg.getContent() : "");
                msgList.add(m);
            }
        }

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("messages", msgList);

        try {
            String response = webClient.post()
                    .uri("/api/agent/emotion/analyze")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(30));

            if (response != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> parsed = objectMapper.readValue(response, Map.class);
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) parsed.get("data");
                if (data != null) {
                    return data;
                }
            }
        } catch (Exception e) {
            log.error("Agent emotion analysis error: {}", e.getMessage());
        }

        // 返回默认结果
        Map<String, Object> fallback = new LinkedHashMap<>();
        fallback.put("primaryEmotion", "未知");
        fallback.put("emotionScore", 50);
        fallback.put("riskLevel", 0);
        fallback.put("riskDescription", "AI分析服务暂不可用");
        fallback.put("isNegative", false);
        fallback.put("suggestion", "");
        return fallback;
    }

    /**
     * 调用Python Agent分析日记情感
     */
    public Mono<String> analyzeDiary(Map<String, Object> diaryData) {
        Map<String, Object> requestBody = new LinkedHashMap<>(diaryData);

        return webClient.post()
                .uri("/api/agent/diary/analyze")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> {
                    try {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> parsed = objectMapper.readValue(response, Map.class);
                        @SuppressWarnings("unchecked")
                        Map<String, Object> data = (Map<String, Object>) parsed.get("data");
                        if (data != null && data.get("analysis") != null) {
                            return (String) data.get("analysis");
                        }
                    } catch (Exception e) {
                        log.warn("Failed to parse diary analysis response: {}", e.getMessage());
                    }
                    return "";
                })
                .onErrorResume(e -> {
                    log.error("Agent diary analysis error: {}", e.getMessage());
                    return Mono.just("");
                });
    }
}
