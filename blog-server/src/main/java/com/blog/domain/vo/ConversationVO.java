package com.blog.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConversationVO {

    private Long id;

    private Long peerId; // 对方用户ID

    private String peerNickname;

    private String peerAvatar;

    private String lastMessage;

    private LocalDateTime lastMessageTime;

    private Integer unreadCount;

    private LocalDateTime createTime;
}
