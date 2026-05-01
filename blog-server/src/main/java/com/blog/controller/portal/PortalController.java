package com.blog.controller.portal;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.Result;
import com.blog.domain.dto.ArticleQueryDTO;
import com.blog.domain.entity.Article;
import com.blog.domain.vo.ArticleListVO;
import com.blog.domain.vo.ArticleVO;
import com.blog.domain.vo.CategoryVO;
import com.blog.domain.vo.TagVO;
import com.blog.security.LoginUser;
import com.blog.service.ArticleService;
import com.blog.service.CategoryService;
import com.blog.service.TagService;
import com.blog.service.UserActionService;
import com.blog.service.ai.RecommendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "前台文章接口")
@RestController
@RequestMapping("/api/portal")
@RequiredArgsConstructor
public class PortalController {

    private final ArticleService articleService;
    private final CategoryService categoryService;
    private final TagService tagService;
    private final UserActionService userActionService;
    private final RecommendService recommendService;

    // ========== 文章接口 ==========

    @Operation(summary = "分页查询文章列表")
    @GetMapping("/articles")
    public Result<Page<ArticleListVO>> pageArticles(ArticleQueryDTO query) {
        query.setStatus(1); // 只查询已发布
        return Result.success(articleService.pageArticle(query));
    }

    @Operation(summary = "获取文章详情")
    @GetMapping("/article/{id}")
    public Result<ArticleVO> getArticle(@PathVariable Long id) {
        return Result.success(articleService.getArticleById(id));
    }

    @Operation(summary = "获取热门文章")
    @GetMapping("/articles/hot")
    public Result<List<ArticleListVO>> getHotArticles(@RequestParam(defaultValue = "5") int limit) {
        return Result.success(articleService.getHotArticles(limit));
    }

    @Operation(summary = "获取置顶文章")
    @GetMapping("/articles/top")
    public Result<List<ArticleListVO>> getTopArticles() {
        return Result.success(articleService.getTopArticles());
    }

    @Operation(summary = "搜索文章")
    @GetMapping("/articles/search")
    public Result<Page<ArticleListVO>> searchArticles(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(articleService.searchArticle(keyword, pageNum, pageSize));
    }

    @Operation(summary = "获取归档列表")
    @GetMapping("/articles/archive")
    public Result<List<ArticleListVO>> getArchiveList() {
        return Result.success(articleService.getArchiveList());
    }

    @Operation(summary = "按分类查询文章")
    @GetMapping("/articles/category/{categoryId}")
    public Result<Page<ArticleListVO>> getArticlesByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(articleService.getArticlesByCategory(categoryId, pageNum, pageSize));
    }

    @Operation(summary = "按标签查询文章")
    @GetMapping("/articles/tag/{tagId}")
    public Result<Page<ArticleListVO>> getArticlesByTag(
            @PathVariable Long tagId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(articleService.getArticlesByTag(tagId, pageNum, pageSize));
    }

    @Operation(summary = "点赞文章")
    @PostMapping("/article/{id}/like")
    public Result<Boolean> likeArticle(@PathVariable Long id) {
        return Result.success(userActionService.toggleLike(id));
    }

    @Operation(summary = "收藏文章")
    @PostMapping("/article/{id}/favorite")
    public Result<Boolean> favoriteArticle(@PathVariable Long id) {
        return Result.success(userActionService.toggleFavorite(id));
    }

    // ========== 分类接口 ==========

    @Operation(summary = "获取分类列表")
    @GetMapping("/categories")
    public Result<List<CategoryVO>> listCategories() {
        return Result.success(categoryService.listAll());
    }

    // ========== 标签接口 ==========

    @Operation(summary = "获取标签列表")
    @GetMapping("/tags")
    public Result<List<TagVO>> listTags() {
        return Result.success(tagService.listAll());
    }

    // ========== 推荐接口 ==========

    @Operation(summary = "获取推荐文章")
    @GetMapping("/articles/recommendations")
    public Result<List<ArticleListVO>> getRecommendations(
            @RequestParam(required = false) Long articleId, @RequestParam(defaultValue = "5") int limit) {
        Long userId = getCurrentUserId();
        List<Article> articles = recommendService.getRecommendations(userId, articleId, limit);
        List<ArticleListVO> vos = articles.stream().map(this::convertToListVO).toList();
        return Result.success(vos);
    }

    @Operation(summary = "记录阅读（更新用户画像）")
    @PostMapping("/articles/reading/{articleId}")
    public Result<Void> recordReading(@PathVariable Long articleId) {
        Long userId = getCurrentUserId();
        if (userId != null) {
            recommendService.updateUserProfile(userId, articleId);
        }
        return Result.success();
    }

    // ========== 辅助方法 ==========

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser.getUserId();
        }
        return null;
    }

    private ArticleListVO convertToListVO(Article article) {
        ArticleListVO vo = new ArticleListVO();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setSummary(article.getSummary());
        vo.setCoverImage(article.getCoverImage());
        vo.setCategoryName(article.getCategoryName());
        vo.setViewCount(article.getViewCount());
        vo.setLikeCount(article.getLikeCount());
        vo.setCommentCount(article.getCommentCount());
        if (article.getPublishTime() != null) {
            vo.setPublishTime(article.getPublishTime().toString());
        }
        return vo;
    }
}
