package com.blog.controller.portal;

import com.blog.common.result.Result;
import com.blog.domain.vo.FollowVO;
import com.blog.security.LoginUser;
import com.blog.service.UserFollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "前台关注接口")
@RestController
@RequestMapping("/api/portal")
@RequiredArgsConstructor
public class FollowController {

    private final UserFollowService userFollowService;

    @Operation(summary = "关注用户")
    @PostMapping("/follow/{userId}")
    public Result<Void> follow(@PathVariable Long userId) {
        Long currentUserId = getCurrentUserId();
        userFollowService.follow(currentUserId, userId);
        return Result.success();
    }

    @Operation(summary = "取消关注用户")
    @DeleteMapping("/follow/{userId}")
    public Result<Void> unfollow(@PathVariable Long userId) {
        Long currentUserId = getCurrentUserId();
        userFollowService.unfollow(currentUserId, userId);
        return Result.success();
    }

    @Operation(summary = "检查是否关注了用户")
    @GetMapping("/follow/check/{userId}")
    public Result<Boolean> checkFollow(@PathVariable Long userId) {
        Long currentUserId = getCurrentUserId();
        boolean isFollowing = userFollowService.isFollowing(currentUserId, userId);
        return Result.success(isFollowing);
    }

    @Operation(summary = "获取关注列表")
    @GetMapping("/following/{userId}")
    public Result<Map<String, Object>> getFollowingList(@PathVariable Long userId) {
        List<FollowVO> list = userFollowService.getFollowingList(userId);
        Integer count = userFollowService.getFollowingCount(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", count);
        return Result.success(result);
    }

    @Operation(summary = "获取粉丝列表")
    @GetMapping("/followers/{userId}")
    public Result<Map<String, Object>> getFollowerList(@PathVariable Long userId) {
        List<FollowVO> list = userFollowService.getFollowerList(userId);
        Integer count = userFollowService.getFollowerCount(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", count);
        return Result.success(result);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            return ((LoginUser) authentication.getPrincipal()).getUserId();
        }
        return null;
    }
}
