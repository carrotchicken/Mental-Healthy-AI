package com.amha.dto;

import lombok.Data;

@Data
public class ArticleSaveRequest {
    private String title;
    private String content;
    private String coverImage;
    private Long categoryId;
    private String summary;
    private String tags;
    private String id;
}
