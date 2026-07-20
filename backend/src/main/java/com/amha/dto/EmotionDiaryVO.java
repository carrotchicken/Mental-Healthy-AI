package com.amha.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmotionDiaryVO {
    private Long id;
    private Long userId;
    private String username;
    private String nickname;
    private LocalDate diaryDate;
    private Integer moodScore;
    private String dominantEmotion;
    private String emotionTriggers;
    private String diaryContent;
    private String diaryContentPreview;
    private Integer sleepQuality;
    private Integer stressLevel;
    private String aiEmotionAnalysis;
    private LocalDateTime aiAnalysisUpdatedAt;
    private Boolean hasAiEmotionAnalysis;
    private String aiAnalysisStatus;
    private Integer contentLength;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
