package com.blog.service;

import com.blog.domain.vo.NotificationVO;
import java.util.List;

public interface NotificationService {

    List<NotificationVO> getNotificationList(Long userId, Integer type);

    Integer getUnreadCount(Long userId);

    void markAsRead(Long userId, Long notificationId);

    void markAllAsRead(Long userId);

    void createFollowNotification(Long userId, Long articleId, String articleTitle, Long senderId, String senderName);

    void createCommentNotification(Long userId, Long articleId, String articleTitle, Long senderId, String senderName);

    void createReplyNotification(Long userId, Long commentId, Long senderId, String senderName);

    void createAnnouncementNotification(Long announcementId, String title);
}
