package com.blog.controller.portal;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.Result;
import com.blog.domain.dto.SeriesQueryDTO;
import com.blog.domain.vo.SeriesListVO;
import com.blog.domain.vo.SeriesVO;
import com.blog.service.SeriesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "门户系列")
@RestController
@RequestMapping("/api/portal/series")
@RequiredArgsConstructor
public class PortalSeriesController {

    private final SeriesService seriesService;

    @Operation(summary = "分页查询系列")
    @GetMapping
    public Result<Page<SeriesListVO>> pageSeries(SeriesQueryDTO query) {
        query.setStatus(1);
        return Result.success(seriesService.pageSeries(query));
    }

    @Operation(summary = "获取系列详情")
    @GetMapping("/{id}")
    public Result<SeriesVO> getSeries(@PathVariable Long id) {
        return Result.success(seriesService.getSeriesById(id));
    }

    @Operation(summary = "获取热门系列")
    @GetMapping("/hot")
    public Result<List<SeriesListVO>> getHotSeries(@RequestParam(defaultValue = "5") int limit) {
        return Result.success(seriesService.getHotSeries(limit));
    }
}
