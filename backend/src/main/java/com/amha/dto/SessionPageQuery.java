package com.amha.dto;

import lombok.Data;

@Data
public class SessionPageQuery {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String emotionTag;
}
