package com.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("writing_template")
public class WritingTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String description;

    private String content;

    private Long categoryId;

    private String defaultTags;

    private String previewImage;

    private Integer usageCount;

    private Integer isBuiltin;

    private Integer isDefault;

    private Integer status;

    private Long authorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
