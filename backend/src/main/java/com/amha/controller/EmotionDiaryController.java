package com.amha.controller;

import com.amha.common.PageResult;
import com.amha.common.Result;
import com.amha.dto.EmotionDiaryPageQuery;
import com.amha.dto.EmotionDiaryRequest;
import com.amha.dto.EmotionDiaryVO;
import com.amha.service.EmotionDiaryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/emotion-diary")
@RequiredArgsConstructor
public class EmotionDiaryController {

    private final EmotionDiaryService emotionDiaryService;

    @PostMapping
    public Result<Void> submitDiary(@Valid @RequestBody EmotionDiaryRequest request,
                                    HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        emotionDiaryService.submitDiary(request, userId);
        return Result.success();
    }

    /**
     * 用户端：获取自己的日记列表（含AI分析结果）
     */
    @GetMapping("/my")
    public Result<List<EmotionDiaryVO>> getMyDiaries(HttpServletRequest httpRequest) {
        Long userId = (Long) httpRequest.getAttribute("userId");
        return Result.success(emotionDiaryService.getUserDiaries(userId));
    }

    // ---- 管理端 ----

    @GetMapping("/admin/page")
    public Result<PageResult<EmotionDiaryVO>> getAdminPage(EmotionDiaryPageQuery query) {
        return Result.success(emotionDiaryService.getAdminPage(query));
    }

    @DeleteMapping("/admin/{id}")
    public Result<Void> deleteAdmin(@PathVariable Long id) {
        emotionDiaryService.deleteAdmin(id);
        return Result.success();
    }
}
