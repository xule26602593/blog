package com.blog.controller.portal;

import com.blog.common.result.Result;
import com.blog.domain.entity.Announcement;
import com.blog.service.AnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "前台公告接口")
@RestController
@RequestMapping("/api/portal/announcements")
@RequiredArgsConstructor
public class PortalAnnouncementController {

    private final AnnouncementService announcementService;

    @Operation(summary = "获取已发布公告列表")
    @GetMapping
    public Result<List<Announcement>> getPublishedAnnouncements() {
        return Result.success(announcementService.getPublishedAnnouncements());
    }
}
