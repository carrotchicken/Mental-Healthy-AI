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
public class ConsultationMessageVO {
    private Long id;
    private Long sessionId;
    private Integer senderType;
    private String senderTypeDesc;
    private Integer messageType;
    private String messageTypeDesc;
    private String content;
    private Integer contentLength;
    private String contentPreview;
    private Boolean isError;
    private LocalDateTime createdAt;
}
