package com.blog.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.Result;
import com.blog.domain.dto.TagDTO;
import com.blog.domain.vo.TagVO;
import com.blog.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "后台标签管理")
@RestController
@RequestMapping("/api/admin/tags")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminTagController {

    private final TagService tagService;

    @Operation(summary = "获取所有标签")
    @GetMapping
    public Result<List<TagVO>> listAll() {
        return Result.success(tagService.listAll());
    }

    @Operation(summary = "分页查询标签")
    @GetMapping("/page")
    public Result<Page<TagVO>> pageTags(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(tagService.pageTag(pageNum, pageSize));
    }

    @Operation(summary = "保存或更新标签")
    @PostMapping
    public Result<Void> saveTag(@Valid @RequestBody TagDTO dto) {
        tagService.saveOrUpdate(dto);
        return Result.success();
    }

    @Operation(summary = "删除标签")
    @DeleteMapping("/{id}")
    public Result<Void> deleteTag(@PathVariable Long id) {
        tagService.delete(id);
        return Result.success();
    }
}
