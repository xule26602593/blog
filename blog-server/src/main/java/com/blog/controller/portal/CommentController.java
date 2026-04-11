package com.blog.controller.portal;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.Result;
import com.blog.domain.dto.CommentDTO;
import com.blog.domain.vo.CommentVO;
import com.blog.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "前台评论接口")
@RestController
@RequestMapping("/api/portal/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "分页查询文章评论")
    @GetMapping("/article/{articleId}")
    public Result<Page<CommentVO>> pageComments(
            @PathVariable Long articleId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(commentService.pageComment(articleId, pageNum, pageSize));
    }

    @Operation(summary = "发表评论")
    @PostMapping
    public Result<Void> addComment(@Valid @RequestBody CommentDTO dto) {
        commentService.addComment(dto);
        return Result.success();
    }
}
