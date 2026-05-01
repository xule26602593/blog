package com.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("topic")
public class Topic implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    private String source;

    private String sourceUrl;

    private String analysis;

    private Integer analysisStatus;

    private Integer status;

    private Long articleId;

    private Integer priority;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
