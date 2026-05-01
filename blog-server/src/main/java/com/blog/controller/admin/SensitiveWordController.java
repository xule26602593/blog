package com.blog.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.Result;
import com.blog.domain.dto.SensitiveWordDTO;
import com.blog.domain.vo.SensitiveWordVO;
import com.blog.service.SensitiveWordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "敏感词管理接口")
@RestController
@RequestMapping("/api/admin/sensitive-words")
@RequiredArgsConstructor
public class SensitiveWordController {

    private final SensitiveWordService sensitiveWordService;

    @Operation(summary = "分页查询敏感词")
    @GetMapping
    public Result<Page<SensitiveWordVO>> pageList(
            @RequestParam(required = false) String word,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(sensitiveWordService.pageList(word, category, pageNum, pageSize));
    }

    @Operation(summary = "添加敏感词")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody SensitiveWordDTO dto) {
        sensitiveWordService.add(dto);
        return Result.success();
    }

    @Operation(summary = "批量添加敏感词")
    @PostMapping("/batch")
    public Result<Void> batchAdd(@RequestBody List<@Valid SensitiveWordDTO> list) {
        sensitiveWordService.batchAdd(list);
        return Result.success();
    }

    @Operation(summary = "更新敏感词")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody SensitiveWordDTO dto) {
        sensitiveWordService.update(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除敏感词")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sensitiveWordService.delete(id);
        return Result.success();
    }

    @Operation(summary = "刷新敏感词缓存")
    @PostMapping("/refresh")
    public Result<Void> refreshCache() {
        sensitiveWordService.refreshCache();
        return Result.success();
    }
}
