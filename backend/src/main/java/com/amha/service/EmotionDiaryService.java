package com.amha.service;

import com.amha.common.PageResult;
import com.amha.dto.EmotionDiaryPageQuery;
import com.amha.dto.EmotionDiaryRequest;
import com.amha.dto.EmotionDiaryVO;

import java.util.List;

public interface EmotionDiaryService {
    void submitDiary(EmotionDiaryRequest request, Long userId);
    PageResult<EmotionDiaryVO> getAdminPage(EmotionDiaryPageQuery query);
    void deleteAdmin(Long id);

    /**
     * 获取用户日记列表（带AI分析结果）
     */
    List<EmotionDiaryVO> getUserDiaries(Long userId);

    /**
     * 重新触发未完成分析的日记的AI分析
     */
    void triggerPendingAnalysis();
}
