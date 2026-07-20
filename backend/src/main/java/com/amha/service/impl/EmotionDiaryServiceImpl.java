package com.amha.service.impl;

import cn.hutool.core.util.StrUtil;
import com.amha.agent.RealChatAgent;
import com.amha.common.BusinessException;
import com.amha.common.PageResult;
import com.amha.dto.EmotionDiaryPageQuery;
import com.amha.dto.EmotionDiaryRequest;
import com.amha.dto.EmotionDiaryVO;
import com.amha.entity.EmotionDiary;
import com.amha.entity.User;
import com.amha.mapper.EmotionDiaryMapper;
import com.amha.mapper.UserMapper;
import com.amha.service.EmotionDiaryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmotionDiaryServiceImpl implements EmotionDiaryService {

    private final EmotionDiaryMapper diaryMapper;
    private final UserMapper userMapper;
    private final RealChatAgent realChatAgent;

    @Override
    public void submitDiary(EmotionDiaryRequest request, Long userId) {
        EmotionDiary diary = new EmotionDiary();
        diary.setUserId(userId);
        diary.setDiaryDate(LocalDate.parse(request.getDiaryDate()));
        diary.setMoodScore(request.getMoodScore());
        diary.setDominantEmotion(request.getDominantEmotion());
        diary.setEmotionTriggers(request.getEmotionTriggers());
        diary.setDiaryContent(request.getDiaryContent());
        diary.setSleepQuality(request.getSleepQuality());
        diary.setStressLevel(request.getStressLevel());
        diary.setHasAiEmotionAnalysis(0);
        diary.setAiAnalysisStatus("PENDING");
        diaryMapper.insert(diary);

        // 异步触发AI分析
        triggerAiAnalysis(diary);
    }

    /**
     * 异步调用Python Agent进行日记情感分析
     */
    @Async
    public void triggerAiAnalysis(EmotionDiary diary) {
        try {
            Map<String, Object> diaryData = new LinkedHashMap<>();
            diaryData.put("dominantEmotion", diary.getDominantEmotion());
            diaryData.put("moodScore", diary.getMoodScore());
            diaryData.put("emotionTriggers", diary.getEmotionTriggers());
            diaryData.put("diaryContent", diary.getDiaryContent());
            diaryData.put("sleepQuality", diary.getSleepQuality() != null ? diary.getSleepQuality() : "");
            diaryData.put("stressLevel", diary.getStressLevel() != null ? diary.getStressLevel() : "");

            String analysis = realChatAgent.analyzeDiary(diaryData).block();
            if (analysis != null && !analysis.isEmpty()) {
                diary.setAiEmotionAnalysis(analysis);
                diary.setHasAiEmotionAnalysis(1);
                diary.setAiAnalysisStatus("COMPLETED");
                diary.setAiAnalysisUpdatedAt(LocalDateTime.now());
            } else {
                diary.setAiAnalysisStatus("FAILED");
            }
        } catch (Exception e) {
            log.error("AI diary analysis failed for diary {}: {}", diary.getId(), e.getMessage());
            diary.setAiAnalysisStatus("FAILED");
        }
        diaryMapper.updateById(diary);
    }

    @Override
    public PageResult<EmotionDiaryVO> getAdminPage(EmotionDiaryPageQuery query) {
        LambdaQueryWrapper<EmotionDiary> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(query.getDominantEmotion())) {
            wrapper.eq(EmotionDiary::getDominantEmotion, query.getDominantEmotion());
        }
        if (query.getMinMoodScore() != null) {
            wrapper.ge(EmotionDiary::getMoodScore, query.getMinMoodScore());
        }
        if (query.getMaxMoodScore() != null) {
            wrapper.le(EmotionDiary::getMoodScore, query.getMaxMoodScore());
        }
        if (StrUtil.isNotBlank(query.getUserId())) {
            wrapper.eq(EmotionDiary::getUserId, Long.parseLong(query.getUserId()));
        }
        wrapper.orderByDesc(EmotionDiary::getCreatedAt);

        Page<EmotionDiary> page = new Page<>(query.getCurrentPage(), query.getSize());
        IPage<EmotionDiary> result = diaryMapper.selectPage(page, wrapper);
        List<EmotionDiaryVO> records = convertToVOList(result.getRecords());
        return PageResult.of(records, result.getCurrent(), result.getSize(), result.getTotal());
    }

    @Override
    public void deleteAdmin(Long id) {
        EmotionDiary diary = diaryMapper.selectById(id);
        if (diary == null) {
            throw new BusinessException("日记不存在");
        }
        diaryMapper.deleteById(id);
    }

    @Override
    public List<EmotionDiaryVO> getUserDiaries(Long userId) {
        LambdaQueryWrapper<EmotionDiary> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmotionDiary::getUserId, userId);
        wrapper.orderByDesc(EmotionDiary::getCreatedAt);
        wrapper.last("LIMIT 30");
        List<EmotionDiary> diaries = diaryMapper.selectList(wrapper);
        return convertToVOList(diaries);
    }

    @Override
    public void triggerPendingAnalysis() {
        LambdaQueryWrapper<EmotionDiary> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EmotionDiary::getAiAnalysisStatus, "PENDING");
        wrapper.last("LIMIT 20");
        List<EmotionDiary> pending = diaryMapper.selectList(wrapper);
        log.info("Triggering analysis for {} pending diaries", pending.size());
        for (EmotionDiary diary : pending) {
            triggerAiAnalysis(diary);
        }
    }

    private List<EmotionDiaryVO> convertToVOList(List<EmotionDiary> diaries) {
        return diaries.stream().map(diary -> {
            User user = userMapper.selectById(diary.getUserId());
            return EmotionDiaryVO.builder()
                    .id(diary.getId())
                    .userId(diary.getUserId())
                    .username(user != null ? user.getUsername() : "")
                    .nickname(user != null ? user.getNickname() : "")
                    .diaryDate(diary.getDiaryDate())
                    .moodScore(diary.getMoodScore())
                    .dominantEmotion(diary.getDominantEmotion())
                    .emotionTriggers(diary.getEmotionTriggers())
                    .diaryContent(diary.getDiaryContent())
                    .diaryContentPreview(diary.getDiaryContent() != null && diary.getDiaryContent().length() > 100
                            ? diary.getDiaryContent().substring(0, 100) + "..."
                            : diary.getDiaryContent())
                    .sleepQuality(diary.getSleepQuality())
                    .stressLevel(diary.getStressLevel())
                    .aiEmotionAnalysis(diary.getAiEmotionAnalysis())
                    .aiAnalysisUpdatedAt(diary.getAiAnalysisUpdatedAt())
                    .hasAiEmotionAnalysis(diary.getHasAiEmotionAnalysis() == 1)
                    .aiAnalysisStatus(diary.getAiAnalysisStatus())
                    .contentLength(diary.getDiaryContent() != null ? diary.getDiaryContent().length() : 0)
                    .createdAt(diary.getCreatedAt())
                    .updatedAt(diary.getUpdatedAt())
                    .build();
        }).collect(Collectors.toList());
    }
}
