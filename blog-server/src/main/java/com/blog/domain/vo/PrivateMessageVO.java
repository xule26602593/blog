package com.blog.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PrivateMessageVO {

    private Long id;

    private Long conversationId;

    private Long senderId;

    private Long receiverId;

    private String senderNickname;

    private String senderAvatar;

    private String content;

    private Integer messageType;

    private Integer isRead;

    private LocalDateTime createTime;
}
