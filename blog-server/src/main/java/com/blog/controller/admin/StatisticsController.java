package com.blog.controller.admin;

import com.blog.common.result.Result;
import com.blog.domain.vo.StatisticsOverviewVO;
import com.blog.service.StatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "访问统计接口")
@RestController
@RequestMapping("/api/admin/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @Operation(summary = "获取统计概览")
    @GetMapping("/overview")
    public Result<StatisticsOverviewVO> getOverview() {
        return Result.success(statisticsService.getOverview());
    }

    @Operation(summary = "获取趋势数据")
    @GetMapping("/trend")
    public Result<List<Map<String, Object>>> getTrend(
            @RequestParam(defaultValue = "pv") String type,
            @RequestParam(defaultValue = "7") Integer days) {
        return Result.success(statisticsService.getTrend(type, days));
    }

    @Operation(summary = "获取热门文章")
    @GetMapping("/hot-articles")
    public Result<List<Map<String, Object>>> getHotArticles(
            @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(statisticsService.getHotArticles(limit));
    }
}
