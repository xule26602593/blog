package com.blog.domain.vo;

import com.blog.domain.enums.NotificationType;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class NotificationVO {
    private Long id;
    private Integer typeCode; // 数据库原始值
    private String type; // 字符串类型: FOLLOW, COMMENT, REPLY, ANNOUNCEMENT
    private String title;
    private String content;
    private Long relatedId;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private Integer isRead;
    private LocalDateTime createTime;

    public void setTypeCode(Integer typeCode) {
        this.typeCode = typeCode;
        this.type = NotificationType.getNameByCode(typeCode);
    }
}
