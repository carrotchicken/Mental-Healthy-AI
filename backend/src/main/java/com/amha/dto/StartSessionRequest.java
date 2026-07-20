package com.amha.dto;

import lombok.Data;

@Data
public class StartSessionRequest {
    private String initialMessage;
    private String sessionTitle;
}
