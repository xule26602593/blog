package com.blog.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.Result;
import com.blog.domain.entity.WritingTemplate;
import com.blog.security.LoginUser;
import com.blog.service.WritingTemplateService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/writing-templates")
@RequiredArgsConstructor
public class WritingTemplateController {

    private final WritingTemplateService templateService;

    @GetMapping
    public Result<List<WritingTemplate>> list() {
        Long currentUserId = getCurrentUserId();
        List<WritingTemplate> templates = templateService.listAvailable(currentUserId);
        return Result.success(templates);
    }

    @GetMapping("/page")
    public Result<Page<WritingTemplate>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) Long authorId) {
        Page<WritingTemplate> page = templateService.page(pageNum, pageSize, authorId);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<WritingTemplate> getById(@PathVariable Long id) {
        WritingTemplate template = templateService.getById(id);
        return Result.success(template);
    }

    @PostMapping
    public Result<Void> create(@RequestBody WritingTemplate template) {
        template.setAuthorId(getCurrentUserId());
        templateService.create(template);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody WritingTemplate template) {
        template.setId(id);
        templateService.update(template);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        templateService.delete(id);
        return Result.success();
    }

    @PostMapping("/{id}/use")
    public Result<Void> useTemplate(@PathVariable Long id) {
        templateService.useTemplate(id);
        return Result.success();
    }

    private Long getCurrentUserId() {
        Object principal =
                SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof LoginUser) {
            return ((LoginUser) principal).getUserId();
        }
        return null;
    }
}
