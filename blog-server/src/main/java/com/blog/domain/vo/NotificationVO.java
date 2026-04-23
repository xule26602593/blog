package com.blog.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationVO {
    private Long id;
    private Integer type;
    private String title;
    private String content;
    private Long relatedId;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private Integer isRead;
    private LocalDateTime createTime;
}
