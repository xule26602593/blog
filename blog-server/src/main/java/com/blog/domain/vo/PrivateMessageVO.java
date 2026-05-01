package com.blog.domain.vo;

import java.time.LocalDateTime;
import lombok.Data;

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
