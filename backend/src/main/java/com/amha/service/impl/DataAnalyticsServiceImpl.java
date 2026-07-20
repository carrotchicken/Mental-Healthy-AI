package com.amha.service.impl;

import com.amha.dto.DashboardVO;
import com.amha.mapper.*;
import com.amha.service.DataAnalyticsService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DataAnalyticsServiceImpl implements DataAnalyticsService {

    private final UserMapper userMapper;
    private final EmotionDiaryMapper emotionDiaryMapper;
    private final ConsultationSessionMapper consultationSessionMapper;
    private final ConsultationMessageMapper consultationMessageMapper;

    @Override
    public DashboardVO getOverview() {
        LocalDate today = LocalDate.now();
        LocalDate sevenDaysAgo = today.minusDays(6);

        // 系统概览
        Long totalUsers = userMapper.selectCount(new LambdaQueryWrapper<>());
        Long totalDiaries = emotionDiaryMapper.selectCount(new LambdaQueryWrapper<>());
        Long totalSessions = consultationSessionMapper.selectCount(new LambdaQueryWrapper<>());

        // 今日新增
        Long todayNewUsers = userMapper.selectCount(new LambdaQueryWrapper<com.amha.entity.User>()
                .ge(com.amha.entity.User::getCreatedAt, today.atStartOfDay()));
        Long todayNewDiaries = emotionDiaryMapper.selectCount(new LambdaQueryWrapper<com.amha.entity.EmotionDiary>()
                .ge(com.amha.entity.EmotionDiary::getCreatedAt, today.atStartOfDay()));
        Long todayNewSessions = consultationSessionMapper.selectCount(new LambdaQueryWrapper<com.amha.entity.ConsultationSession>()
                .ge(com.amha.entity.ConsultationSession::getCreatedAt, today.atStartOfDay()));

        // 平均情绪分
        List<com.amha.entity.EmotionDiary> allDiaries = emotionDiaryMapper.selectList(new LambdaQueryWrapper<>());
        Double avgMoodScore = allDiaries.stream()
                .mapToInt(com.amha.entity.EmotionDiary::getMoodScore)
                .average().orElse(0.0);

        // 活跃用户数（近7天有日记或咨询的用户）
        Set<Long> activeUserIds = new HashSet<>();
        emotionDiaryMapper.selectList(new LambdaQueryWrapper<com.amha.entity.EmotionDiary>()
                .ge(com.amha.entity.EmotionDiary::getCreatedAt, sevenDaysAgo.atStartOfDay()))
                .forEach(d -> activeUserIds.add(d.getUserId()));
        consultationSessionMapper.selectList(new LambdaQueryWrapper<com.amha.entity.ConsultationSession>()
                .ge(com.amha.entity.ConsultationSession::getCreatedAt, sevenDaysAgo.atStartOfDay()))
                .forEach(s -> activeUserIds.add(s.getUserId()));

        DashboardVO.SystemOverview overview = DashboardVO.SystemOverview.builder()
                .totalUsers(totalUsers)
                .activeUsers((long) activeUserIds.size())
                .totalDiaries(totalDiaries)
                .totalSessions(totalSessions)
                .avgMoodScore(Math.round(avgMoodScore * 10.0) / 10.0)
                .todayNewUsers(todayNewUsers)
                .todayNewDiaries(todayNewDiaries)
                .todayNewSessions(todayNewSessions)
                .build();

        // 情绪趋势（近7天）
        List<DashboardVO.EmotionTrendItem> emotionTrend = buildEmotionTrend(sevenDaysAgo, today);

        // 情绪分布热力图
        DashboardVO.EmotionHeatmap heatmap = buildEmotionHeatmap(allDiaries);

        // 咨询统计
        DashboardVO.ConsultationStats consultationStats = buildConsultationStats(sevenDaysAgo, today);

        // 用户活跃度趋势
        List<DashboardVO.UserActivityItem> userActivity = buildUserActivity(sevenDaysAgo, today);

        return DashboardVO.builder()
                .systemOverview(overview)
                .emotionHeatmap(heatmap)
                .emotionTrend(emotionTrend)
                .consultationStats(consultationStats)
                .userActivity(userActivity)
                .build();
    }

    private List<DashboardVO.EmotionTrendItem> buildEmotionTrend(LocalDate start, LocalDate end) {
        List<DashboardVO.EmotionTrendItem> result = new ArrayList<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            LocalDate d = date;
            List<com.amha.entity.EmotionDiary> dayDiaries = emotionDiaryMapper.selectList(
                    new LambdaQueryWrapper<com.amha.entity.EmotionDiary>()
                            .eq(com.amha.entity.EmotionDiary::getDiaryDate, d));

            double avgScore = dayDiaries.stream().mapToInt(com.amha.entity.EmotionDiary::getMoodScore)
                    .average().orElse(0.0);
            long count = dayDiaries.size();
            long positiveCount = dayDiaries.stream().filter(di -> di.getMoodScore() >= 6).count();
            long negativeCount = dayDiaries.stream().filter(di -> di.getMoodScore() <= 4).count();
            double positiveRatio = count > 0 ? (double) positiveCount / count : 0.0;
            double negativeRatio = count > 0 ? (double) negativeCount / count : 0.0;

            // 主要情绪
            String dominantEmotion = dayDiaries.stream()
                    .collect(Collectors.groupingBy(com.amha.entity.EmotionDiary::getDominantEmotion, Collectors.counting()))
                    .entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("无数据");

            result.add(DashboardVO.EmotionTrendItem.builder()
                    .date(date.format(fmt))
                    .avgMoodScore(Math.round(avgScore * 10.0) / 10.0)
                    .recordCount(count)
                    .positiveRatio(Math.round(positiveRatio * 1000.0) / 1000.0)
                    .negativeRatio(Math.round(negativeRatio * 1000.0) / 1000.0)
                    .dominantEmotion(dominantEmotion)
                    .build());
        }
        return result;
    }

    private DashboardVO.EmotionHeatmap buildEmotionHeatmap(List<com.amha.entity.EmotionDiary> allDiaries) {
        Map<String, Integer> distribution = allDiaries.stream()
                .collect(Collectors.groupingBy(com.amha.entity.EmotionDiary::getDominantEmotion,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));

        List<List<Object>> gridData = new ArrayList<>();
        // 生成7x24网格数据（简化版）
        for (int hour = 0; hour < 24; hour++) {
            List<Object> row = new ArrayList<>();
            for (int day = 0; day < 7; day++) {
                Map<String, Object> cell = new LinkedHashMap<>();
                cell.put("hour", hour);
                cell.put("day", day);
                cell.put("count", 0);
                row.add(cell);
            }
            gridData.add(row);
        }

        return DashboardVO.EmotionHeatmap.builder()
                .gridData(gridData)
                .emotionDistribution(distribution)
                .peakEmotionTime("20:00-22:00")
                .dateRange("近7天")
                .build();
    }

    private DashboardVO.ConsultationStats buildConsultationStats(LocalDate start, LocalDate end) {
        Long totalSessions = consultationSessionMapper.selectCount(new LambdaQueryWrapper<>());
        List<com.amha.entity.ConsultationSession> allSessions = consultationSessionMapper.selectList(new LambdaQueryWrapper<>());
        double avgDuration = allSessions.stream()
                .mapToInt(s -> s.getDurationMinutes() != null ? s.getDurationMinutes() : 0)
                .average().orElse(0.0);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        List<DashboardVO.DailyTrendItem> dailyTrend = new ArrayList<>();
        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            LocalDate d = date;
            List<com.amha.entity.ConsultationSession> daySessions = consultationSessionMapper.selectList(
                    new LambdaQueryWrapper<com.amha.entity.ConsultationSession>()
                            .ge(com.amha.entity.ConsultationSession::getCreatedAt, d.atStartOfDay())
                            .lt(com.amha.entity.ConsultationSession::getCreatedAt, d.plusDays(1).atStartOfDay()));
            long userCount = daySessions.stream().map(com.amha.entity.ConsultationSession::getUserId).distinct().count();

            dailyTrend.add(DashboardVO.DailyTrendItem.builder()
                    .date(date.format(fmt))
                    .sessionCount((long) daySessions.size())
                    .userCount(userCount)
                    .build());
        }

        return DashboardVO.ConsultationStats.builder()
                .totalSessions(totalSessions)
                .avgDurationMinutes(Math.round(avgDuration * 10.0) / 10.0)
                .dailyTrend(dailyTrend)
                .build();
    }

    private List<DashboardVO.UserActivityItem> buildUserActivity(LocalDate start, LocalDate end) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM-dd");
        List<DashboardVO.UserActivityItem> result = new ArrayList<>();

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            LocalDate d = date;
            LocalDateTime dayStart = d.atStartOfDay();
            LocalDateTime dayEnd = d.plusDays(1).atStartOfDay();

            // 活跃用户
            Set<Long> activeSet = new HashSet<>();
            emotionDiaryMapper.selectList(new LambdaQueryWrapper<com.amha.entity.EmotionDiary>()
                    .ge(com.amha.entity.EmotionDiary::getCreatedAt, dayStart)
                    .lt(com.amha.entity.EmotionDiary::getCreatedAt, dayEnd))
                    .forEach(di -> activeSet.add(di.getUserId()));
            consultationSessionMapper.selectList(new LambdaQueryWrapper<com.amha.entity.ConsultationSession>()
                    .ge(com.amha.entity.ConsultationSession::getCreatedAt, dayStart)
                    .lt(com.amha.entity.ConsultationSession::getCreatedAt, dayEnd))
                    .forEach(s -> activeSet.add(s.getUserId()));

            // 新增用户
            long newUsers = userMapper.selectCount(new LambdaQueryWrapper<com.amha.entity.User>()
                    .ge(com.amha.entity.User::getCreatedAt, dayStart)
                    .lt(com.amha.entity.User::getCreatedAt, dayEnd));

            // 日记用户和咨询用户
            long diaryUsers = emotionDiaryMapper.selectList(new LambdaQueryWrapper<com.amha.entity.EmotionDiary>()
                    .ge(com.amha.entity.EmotionDiary::getCreatedAt, dayStart)
                    .lt(com.amha.entity.EmotionDiary::getCreatedAt, dayEnd))
                    .stream().map(com.amha.entity.EmotionDiary::getUserId).distinct().count();

            long consultationUsers = consultationSessionMapper.selectList(new LambdaQueryWrapper<com.amha.entity.ConsultationSession>()
                    .ge(com.amha.entity.ConsultationSession::getCreatedAt, dayStart)
                    .lt(com.amha.entity.ConsultationSession::getCreatedAt, dayEnd))
                    .stream().map(com.amha.entity.ConsultationSession::getUserId).distinct().count();

            result.add(DashboardVO.UserActivityItem.builder()
                    .date(date.format(fmt))
                    .activeUsers((long) activeSet.size())
                    .newUsers(newUsers)
                    .diaryUsers(diaryUsers)
                    .consultationUsers(consultationUsers)
                    .build());
        }
        return result;
    }
}
