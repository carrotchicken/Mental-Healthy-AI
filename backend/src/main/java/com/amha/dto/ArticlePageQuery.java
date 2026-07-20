package com.amha.dto;

import lombok.Data;

@Data
public class ArticlePageQuery {
    private String title;
    private String categoryId;
    private String status;
    private String authorName;
    private Integer currentPage = 1;
    private Integer pageSize = 10;
}
