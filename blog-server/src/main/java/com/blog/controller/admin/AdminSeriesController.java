package com.blog.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.Result;
import com.blog.domain.dto.SeriesDTO;
import com.blog.domain.dto.SeriesQueryDTO;
import com.blog.domain.vo.SeriesListVO;
import com.blog.domain.vo.SeriesVO;
import com.blog.service.SeriesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "后台系列管理")
@RestController
@RequestMapping("/api/admin/series")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminSeriesController {

    private final SeriesService seriesService;

    @Operation(summary = "分页查询系列")
    @GetMapping
    public Result<Page<SeriesListVO>> pageSeries(SeriesQueryDTO query) {
        return Result.success(seriesService.pageSeries(query));
    }

    @Operation(summary = "获取系列详情")
    @GetMapping("/{id}")
    public Result<SeriesVO> getSeries(@PathVariable Long id) {
        return Result.success(seriesService.getSeriesById(id));
    }

    @Operation(summary = "保存或更新系列")
    @PostMapping
    public Result<Void> saveSeries(@Valid @RequestBody SeriesDTO dto) {
        seriesService.saveOrUpdateSeries(dto);
        return Result.success();
    }

    @Operation(summary = "删除系列")
    @DeleteMapping("/{id}")
    public Result<Void> deleteSeries(@PathVariable Long id) {
        seriesService.deleteSeries(id);
        return Result.success();
    }

    @Operation(summary = "添加文章到系列")
    @PostMapping("/{id}/articles")
    public Result<Void> addArticles(@PathVariable Long id, @RequestBody List<Long> articleIds) {
        seriesService.addArticlesToSeries(id, articleIds);
        return Result.success();
    }

    @Operation(summary = "从系列移除文章")
    @DeleteMapping("/{id}/articles/{articleId}")
    public Result<Void> removeArticle(@PathVariable Long id, @PathVariable Long articleId) {
        seriesService.removeArticleFromSeries(id, articleId);
        return Result.success();
    }

    @Operation(summary = "调整文章顺序")
    @PutMapping("/{id}/articles/order")
    public Result<Void> updateArticlesOrder(@PathVariable Long id, @RequestBody List<Long> articleIds) {
        seriesService.updateArticlesOrder(id, articleIds);
        return Result.success();
    }
}
