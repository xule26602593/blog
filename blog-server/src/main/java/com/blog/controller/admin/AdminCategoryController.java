package com.blog.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.Result;
import com.blog.domain.dto.CategoryDTO;
import com.blog.domain.vo.CategoryVO;
import com.blog.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "后台分类管理")
@RestController
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "获取所有分类")
    @GetMapping
    public Result<List<CategoryVO>> listAll() {
        return Result.success(categoryService.listAll());
    }

    @Operation(summary = "分页查询分类")
    @GetMapping("/page")
    public Result<Page<CategoryVO>> pageCategories(
            @RequestParam(defaultValue = "1") int pageNum, @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(categoryService.pageCategory(pageNum, pageSize));
    }

    @Operation(summary = "获取分类详情")
    @GetMapping("/{id}")
    public Result<CategoryVO> getCategory(@PathVariable Long id) {
        return Result.success(categoryService.getById(id));
    }

    @Operation(summary = "保存或更新分类")
    @PostMapping
    public Result<Void> saveCategory(@Valid @RequestBody CategoryDTO dto) {
        categoryService.saveOrUpdate(dto);
        return Result.success();
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success();
    }
}
