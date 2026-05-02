package com.blog.controller.portal;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.PageResult;
import com.blog.common.result.Result;
import com.blog.domain.dto.CommentDTO;
import com.blog.domain.dto.LikeResultDTO;
import com.blog.domain.enums.CommentSortType;
import com.blog.domain.vo.CommentVO;
import com.blog.domain.vo.ReplyVO;
import com.blog.domain.vo.UserSimpleVO;
import com.blog.security.LoginUser;
import com.blog.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "前台评论接口")
@RestController
@RequestMapping("/api/portal/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "分页查询文章评论（旧接口）")
    @GetMapping("/article/{articleId}")
    public Result<Page<CommentVO>> pageComments(
            @PathVariable Long articleId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(commentService.pageComment(articleId, pageNum, pageSize));
    }

    @Operation(summary = "获取文章评论列表（支持排序）")
    @GetMapping("/article/{articleId}/list")
    public Result<PageResult<CommentVO>> listComments(
            @PathVariable Long articleId,
            @RequestParam(defaultValue = "hot") String sortBy,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long currentUserId = getCurrentUserId();
        CommentSortType sortType = CommentSortType.fromCode(sortBy);
        return Result.success(commentService.listComments(articleId, sortType, page, size, currentUserId));
    }

    @Operation(summary = "发表评论")
    @PostMapping
    public Result<CommentVO> createComment(@Valid @RequestBody CommentDTO dto) {
        Long currentUserId = getCurrentUserId();
        CommentVO vo = commentService.createComment(dto, currentUserId);
        return Result.success(vo);
    }

    @Operation(summary = "获取评论的回复列表")
    @GetMapping("/{commentId}/replies")
    public Result<PageResult<ReplyVO>> listReplies(
            @PathVariable Long commentId,
            @RequestParam(defaultValue = "hot") String sortBy,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long currentUserId = getCurrentUserId();
        CommentSortType sortType = CommentSortType.fromCode(sortBy);
        return Result.success(commentService.listReplies(commentId, sortType, page, size, currentUserId));
    }

    @Operation(summary = "点赞/取消点赞评论")
    @PostMapping("/{commentId}/like")
    public Result<LikeResultDTO> toggleLike(@PathVariable Long commentId) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(commentService.toggleLike(commentId, currentUserId));
    }

    @Operation(summary = "获取评论点赞列表")
    @GetMapping("/{commentId}/likes")
    public Result<PageResult<UserSimpleVO>> listLikes(
            @PathVariable Long commentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(commentService.listLikes(commentId, page, size));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            return ((LoginUser) authentication.getPrincipal()).getUserId();
        }
        return null;
    }
}
