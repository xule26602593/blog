package com.blog.controller.portal;

import com.blog.common.result.PageResult;
import com.blog.common.result.Result;
import com.blog.domain.vo.ReadingHistoryVO;
import com.blog.service.ReadingHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "前台阅读历史接口")
@RestController
@RequestMapping("/api/portal/history")
@RequiredArgsConstructor
public class ReadingHistoryController {

    private final ReadingHistoryService readingHistoryService;

    @Operation(summary = "记录阅读历史")
    @PostMapping("/{articleId}")
    public Result<Void> recordHistory(@PathVariable Long articleId) {
        readingHistoryService.recordHistory(articleId);
        return Result.success();
    }

    @Operation(summary = "获取阅读历史列表")
    @GetMapping
    public Result<PageResult<ReadingHistoryVO>> getHistoryList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(readingHistoryService.getHistoryList(pageNum, pageSize));
    }

    @Operation(summary = "删除单条阅读历史")
    @DeleteMapping("/{articleId}")
    public Result<Void> deleteHistory(@PathVariable Long articleId) {
        readingHistoryService.deleteHistory(articleId);
        return Result.success();
    }

    @Operation(summary = "清空阅读历史")
    @DeleteMapping
    public Result<Void> clearHistory() {
        readingHistoryService.clearHistory();
        return Result.success();
    }
}
