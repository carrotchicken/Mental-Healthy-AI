package com.amha.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmotionDiaryRequest {
    @NotBlank(message = "日记内容不能为空")
    private String diaryContent;

    @NotBlank(message = "日记日期不能为空")
    private String diaryDate;

    @NotBlank(message = "主要情绪不能为空")
    private String dominantEmotion;

    @NotBlank(message = "情绪触发因素不能为空")
    private String emotionTriggers;

    @NotNull(message = "情绪评分不能为空")
    private Integer moodScore;

    private Integer sleepQuality;
    private Integer stressLevel;
}
