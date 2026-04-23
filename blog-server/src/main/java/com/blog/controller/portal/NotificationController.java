package com.blog.controller.portal;

import com.blog.common.result.Result;
import com.blog.domain.vo.NotificationVO;
import com.blog.security.LoginUser;
import com.blog.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/portal")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/notifications")
    public Result<Map<String, Object>> getNotifications(@RequestParam(required = false) Integer type) {
        Long userId = getCurrentUserId();
        List<NotificationVO> list = notificationService.getNotificationList(userId, type);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        return Result.success(result);
    }

    @GetMapping("/notifications/unread-count")
    public Result<Integer> getUnreadCount() {
        Long userId = getCurrentUserId();
        Integer count = notificationService.getUnreadCount(userId);
        return Result.success(count);
    }

    @PutMapping("/notifications/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        notificationService.markAsRead(userId, id);
        return Result.success();
    }

    @PutMapping("/notifications/read-all")
    public Result<Void> markAllAsRead() {
        Long userId = getCurrentUserId();
        notificationService.markAllAsRead(userId);
        return Result.success();
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            return ((LoginUser) authentication.getPrincipal()).getUserId();
        }
        return null;
    }
}
