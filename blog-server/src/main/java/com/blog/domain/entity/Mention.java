package com.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("mention")
public class Mention implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sourceType;

    private Long sourceId;

    private Long mentionedUserId;

    private Long mentionerId;

    private String content;

    private Integer isNotified;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
