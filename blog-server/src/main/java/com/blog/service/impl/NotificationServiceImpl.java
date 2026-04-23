package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.domain.entity.Announcement;
import com.blog.domain.entity.Notification;
import com.blog.domain.entity.User;
import com.blog.domain.vo.NotificationVO;
import com.blog.repository.mapper.AnnouncementMapper;
import com.blog.repository.mapper.NotificationMapper;
import com.blog.repository.mapper.UserMapper;
import com.blog.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;
    private final AnnouncementMapper announcementMapper;

    @Override
    public List<NotificationVO> getNotificationList(Long userId, Integer type) {
        if (type != null && type > 0) {
            return notificationMapper.selectNotificationListByType(userId, type);
        }
        return notificationMapper.selectNotificationList(userId);
    }

    @Override
    public Integer getUnreadCount(Long userId) {
        return notificationMapper.countUnread(userId);
    }

    @Override
    public void markAsRead(Long userId, Long notificationId) {
        notificationMapper.markAsRead(notificationId, userId);
    }

    @Override
    public void markAllAsRead(Long userId) {
        notificationMapper.markAllAsRead(userId);
    }

    @Override
    @Async
    public void createFollowNotification(Long userId, Long articleId, String articleTitle, Long senderId, String senderName) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(1);
        notification.setTitle("关注的作者发布了新文章");
        notification.setContent(senderName + " 发布了《" + truncateTitle(articleTitle) + "》");
        notification.setRelatedId(articleId);
        notification.setSenderId(senderId);
        notification.setIsRead(0);
        notificationMapper.insert(notification);
    }

    @Override
    @Async
    public void createCommentNotification(Long userId, Long articleId, String articleTitle, Long senderId, String senderName) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(2);
        notification.setTitle("文章收到新评论");
        notification.setContent(senderName + " 评论了《" + truncateTitle(articleTitle) + "》");
        notification.setRelatedId(articleId);
        notification.setSenderId(senderId);
        notification.setIsRead(0);
        notificationMapper.insert(notification);
    }

    @Override
    @Async
    public void createReplyNotification(Long userId, Long commentId, Long senderId, String senderName) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(3);
        notification.setTitle("评论收到回复");
        notification.setContent(senderName + " 回复了你的评论");
        notification.setRelatedId(commentId);
        notification.setSenderId(senderId);
        notification.setIsRead(0);
        notificationMapper.insert(notification);
    }

    @Override
    @Async
    public void createAnnouncementNotification(Long announcementId, String title) {
        Announcement announcement = announcementMapper.selectById(announcementId);
        if (announcement == null || announcement.getStatus() != 1) {
            return;
        }

        // 为所有用户创建通知（分批处理避免内存问题）
        int pageSize = 1000;
        int pageNo = 1;
        long total = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getDeleted, 0));

        while ((long) (pageNo - 1) * pageSize < total) {
            List<User> users = userMapper.selectList(
                new LambdaQueryWrapper<User>()
                    .eq(User::getDeleted, 0)
                    .last("LIMIT " + pageSize + " OFFSET " + (pageNo - 1) * pageSize)
            );

            for (User user : users) {
                Notification notification = new Notification();
                notification.setUserId(user.getId());
                notification.setType(4);
                notification.setTitle("系统公告");
                notification.setContent(title);
                notification.setRelatedId(announcementId);
                notification.setSenderId(null);
                notification.setIsRead(0);
                notificationMapper.insert(notification);
            }
            pageNo++;
        }
    }

    private String truncateTitle(String title) {
        if (title == null) return "";
        return title.length() > 20 ? title.substring(0, 20) + "..." : title;
    }
}
