package com.blog.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.Result;
import com.blog.domain.dto.TemplateDTO;
import com.blog.domain.vo.TemplateVO;
import com.blog.service.TemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "文章模板接口")
@RestController
@RequestMapping("/api/admin/templates")
@RequiredArgsConstructor
public class TemplateController {

    private final TemplateService templateService;

    @Operation(summary = "分页查询模板")
    @GetMapping
    public Result<Page<TemplateVO>> pageList(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(templateService.pageList(name, pageNum, pageSize));
    }

    @Operation(summary = "获取所有模板")
    @GetMapping("/all")
    public Result<List<TemplateVO>> listAll() {
        return Result.success(templateService.listAll());
    }

    @Operation(summary = "获取模板详情")
    @GetMapping("/{id}")
    public Result<TemplateVO> getById(@PathVariable Long id) {
        return Result.success(templateService.getById(id));
    }

    @Operation(summary = "创建模板")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody TemplateDTO dto) {
        templateService.add(dto);
        return Result.success();
    }

    @Operation(summary = "更新模板")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody TemplateDTO dto) {
        templateService.update(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除模板")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        templateService.delete(id);
        return Result.success();
    }
}
