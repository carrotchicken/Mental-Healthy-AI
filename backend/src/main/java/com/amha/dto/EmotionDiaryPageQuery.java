package com.amha.dto;

import lombok.Data;

@Data
public class EmotionDiaryPageQuery {
    private Integer currentPage = 1;
    private Integer size = 10;
    private String dominantEmotion;
    private Integer minMoodScore;
    private Integer maxMoodScore;
    private String userId;
}
