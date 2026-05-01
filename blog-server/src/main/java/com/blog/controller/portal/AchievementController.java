package com.blog.controller.portal;

import com.blog.common.result.Result;
import com.blog.domain.vo.AchievementVO;
import com.blog.domain.vo.UserAchievementVO;
import com.blog.security.LoginUser;
import com.blog.service.AchievementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "成就接口", description = "成就徽章相关接口")
@RestController
@RequestMapping("/api/portal/achievements")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;

    @Operation(summary = "获取所有成就列表")
    @GetMapping
    public Result<List<AchievementVO>> listAll() {
        return Result.success(achievementService.listAll());
    }

    @Operation(summary = "按分类获取成就列表")
    @GetMapping("/category/{category}")
    public Result<List<AchievementVO>> listByCategory(@PathVariable String category) {
        return Result.success(achievementService.listByCategory(category));
    }

    @Operation(summary = "获取用户成就列表")
    @GetMapping("/my")
    public Result<List<UserAchievementVO>> getUserAchievements(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(achievementService.getUserAchievements(loginUser.getUserId()));
    }

    @Operation(summary = "获取用户已解锁成就")
    @GetMapping("/my/unlocked")
    public Result<List<UserAchievementVO>> getUserUnlockedAchievements(@AuthenticationPrincipal LoginUser loginUser) {
        return Result.success(achievementService.getUserUnlockedAchievements(loginUser.getUserId()));
    }

    @Operation(summary = "获取单个成就进度")
    @GetMapping("/{achievementId}/progress")
    public Result<UserAchievementVO> getProgress(
            @AuthenticationPrincipal LoginUser loginUser, @PathVariable Long achievementId) {
        return Result.success(achievementService.getUserAchievementProgress(loginUser.getUserId(), achievementId));
    }
}
