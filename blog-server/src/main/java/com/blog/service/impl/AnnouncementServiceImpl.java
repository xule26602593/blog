package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.exception.BusinessException;
import com.blog.common.result.ErrorCode;
import com.blog.domain.entity.Announcement;
import com.blog.repository.mapper.AnnouncementMapper;
import com.blog.service.AnnouncementService;
import com.blog.service.NotificationService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementMapper announcementMapper;
    private final NotificationService notificationService;

    @Override
    public List<Announcement> getAnnouncementList() {
        return announcementMapper.selectList(
                new LambdaQueryWrapper<Announcement>().orderByDesc(Announcement::getCreateTime));
    }

    @Override
    public List<Announcement> getPublishedAnnouncements() {
        return announcementMapper.selectList(new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getStatus, 1)
                .orderByDesc(Announcement::getPublishTime));
    }

    @Override
    public Announcement getAnnouncementById(Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "公告不存在");
        }
        return announcement;
    }

    @Override
    public void saveOrUpdateAnnouncement(Announcement announcement) {
        if (announcement.getId() == null) {
            announcement.setStatus(0); // 默认草稿
            announcementMapper.insert(announcement);
        } else {
            announcementMapper.updateById(announcement);
        }
    }

    @Override
    public void deleteAnnouncement(Long id) {
        announcementMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void publishAnnouncement(Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "公告不存在");
        }

        announcement.setStatus(1);
        announcement.setPublishTime(LocalDateTime.now());
        announcementMapper.updateById(announcement);

        // 异步创建通知
        notificationService.createAnnouncementNotification(id, announcement.getTitle());
    }
}
