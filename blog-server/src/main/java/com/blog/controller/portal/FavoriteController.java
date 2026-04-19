package com.blog.controller.portal;

import com.blog.common.result.PageResult;
import com.blog.common.result.Result;
import com.blog.domain.vo.FavoriteVO;
import com.blog.service.FavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "前台收藏接口")
@RestController
@RequestMapping("/api/portal")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @Operation(summary = "获取收藏列表")
    @GetMapping("/favorites")
    public Result<PageResult<FavoriteVO>> getFavorites(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        return Result.success(favoriteService.getFavorites(pageNum, pageSize, keyword));
    }
}
