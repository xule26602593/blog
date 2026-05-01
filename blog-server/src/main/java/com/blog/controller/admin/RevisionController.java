package com.blog.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.Result;
import com.blog.domain.vo.RevisionVO;
import com.blog.security.LoginUser;
import com.blog.service.RevisionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "版本历史接口")
@RestController
@RequestMapping("/api/admin/articles/{articleId}/revisions")
@RequiredArgsConstructor
public class RevisionController {

    private final RevisionService revisionService;

    @Operation(summary = "获取文章版本历史")
    @GetMapping
    public Result<Page<RevisionVO>> getRevisions(
            @PathVariable Long articleId,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        return Result.success(revisionService.getRevisions(articleId, pageNum, pageSize));
    }

    @Operation(summary = "获取特定版本详情")
    @GetMapping("/{version}")
    public Result<RevisionVO> getRevision(@PathVariable Long articleId, @PathVariable Integer version) {
        return Result.success(revisionService.getRevision(articleId, version));
    }

    @Operation(summary = "回退到特定版本")
    @PostMapping("/{version}/restore")
    public Result<Void> restore(@PathVariable Long articleId, @PathVariable Integer version) {
        Long editorId = getCurrentUserId();
        revisionService.restore(articleId, version, editorId);
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
