package com.amha.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartSessionResponse {
    private Long sessionId;
    private String status;
    private Long startTime;
    private String initialMessage;
    private Integer messageCount;
    private String sessionTitle;
}
