package com.amha.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardVO {
    private SystemOverview systemOverview;
    private EmotionHeatmap emotionHeatmap;
    private List<EmotionTrendItem> emotionTrend;
    private ConsultationStats consultationStats;
    private List<UserActivityItem> userActivity;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SystemOverview {
        private Long totalUsers;
        private Long activeUsers;
        private Long totalDiaries;
        private Long totalSessions;
        private Double avgMoodScore;
        private Long todayNewUsers;
        private Long todayNewDiaries;
        private Long todayNewSessions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmotionHeatmap {
        private List<List<Object>> gridData;
        private Map<String, Integer> emotionDistribution;
        private String peakEmotionTime;
        private String dateRange;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmotionTrendItem {
        private String date;
        private Double avgMoodScore;
        private Long recordCount;
        private Double positiveRatio;
        private Double negativeRatio;
        private String dominantEmotion;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsultationStats {
        private Long totalSessions;
        private Double avgDurationMinutes;
        private List<DailyTrendItem> dailyTrend;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyTrendItem {
        private String date;
        private Long sessionCount;
        private Long userCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserActivityItem {
        private String date;
        private Long activeUsers;
        private Long newUsers;
        private Long diaryUsers;
        private Long consultationUsers;
    }
}
