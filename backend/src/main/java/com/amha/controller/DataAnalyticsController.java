package com.amha.controller;

import com.amha.common.Result;
import com.amha.dto.DashboardVO;
import com.amha.service.DataAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/data-analytics")
@RequiredArgsConstructor
public class DataAnalyticsController {

    private final DataAnalyticsService dataAnalyticsService;

    @GetMapping("/overview")
    public Result<DashboardVO> getOverview() {
        return Result.success(dataAnalyticsService.getOverview());
    }
}
