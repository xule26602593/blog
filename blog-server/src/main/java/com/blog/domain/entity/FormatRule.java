package com.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("format_rule")
public class FormatRule {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String ruleKey;

    private String ruleName;

    private String description;

    private String ruleType;

    private String ruleConfig;

    private Integer priority;

    private Integer isDefault;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
