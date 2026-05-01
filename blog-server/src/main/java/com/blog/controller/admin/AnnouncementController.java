package com.blog.controller.admin;

import com.blog.common.result.Result;
import com.blog.domain.entity.Announcement;
import com.blog.service.AnnouncementService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping
    public Result<List<Announcement>> getList() {
        return Result.success(announcementService.getAnnouncementList());
    }

    @GetMapping("/{id}")
    public Result<Announcement> getById(@PathVariable Long id) {
        return Result.success(announcementService.getAnnouncementById(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody Announcement announcement) {
        announcementService.saveOrUpdateAnnouncement(announcement);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Announcement announcement) {
        announcement.setId(id);
        announcementService.saveOrUpdateAnnouncement(announcement);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        announcementService.deleteAnnouncement(id);
        return Result.success();
    }

    @PutMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id) {
        announcementService.publishAnnouncement(id);
        return Result.success();
    }
}
