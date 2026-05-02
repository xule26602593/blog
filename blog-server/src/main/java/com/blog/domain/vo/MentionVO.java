package com.blog.domain.vo;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class MentionVO {
    private Long id;
    private String sourceType;
    private Long sourceId;
    private Long mentionerId;
    private String mentionerNickname;
    private String mentionerAvatar;
    private String content;
    private LocalDateTime createTime;
    // 关联信息
    private String articleTitle;
}
