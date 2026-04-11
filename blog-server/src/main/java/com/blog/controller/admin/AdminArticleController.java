package com.blog.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.Result;
import com.blog.domain.dto.ArticleDTO;
import com.blog.domain.dto.ArticleQueryDTO;
import com.blog.domain.vo.ArticleListVO;
import com.blog.domain.vo.ArticleVO;
import com.blog.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "后台文章管理")
@RestController
@RequestMapping("/api/admin/articles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminArticleController {

    private final ArticleService articleService;

    @Operation(summary = "分页查询文章")
    @GetMapping
    public Result<Page<ArticleListVO>> pageArticles(ArticleQueryDTO query) {
        return Result.success(articleService.pageArticle(query));
    }

    @Operation(summary = "获取文章详情")
    @GetMapping("/{id}")
    public Result<ArticleVO> getArticle(@PathVariable Long id) {
        return Result.success(articleService.getArticleById(id));
    }

    @Operation(summary = "保存或更新文章")
    @PostMapping
    public Result<Void> saveArticle(@Valid @RequestBody ArticleDTO dto) {
        articleService.saveOrUpdateArticle(dto);
        return Result.success();
    }

    @Operation(summary = "删除文章")
    @DeleteMapping("/{id}")
    public Result<Void> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return Result.success();
    }

    @Operation(summary = "更新文章状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        articleService.updateStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "切换置顶状态")
    @PutMapping("/{id}/top")
    public Result<Void> toggleTop(@PathVariable Long id) {
        articleService.toggleTop(id);
        return Result.success();
    }
}
