package com.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("conversation")
public class Conversation implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long user1Id;

    private Long user2Id;

    private Long lastMessageId;

    private LocalDateTime lastMessageTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
