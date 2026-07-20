package com.amha.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionVO {
    private Long id;
    private Long userId;
    private String userNickname;
    private String sessionTitle;
    private LocalDateTime startedAt;
    private Integer durationMinutes;
    private Integer messageCount;
    private String lastMessageContent;
    private LocalDateTime lastMessageTime;
}
