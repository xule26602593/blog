package com.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("checkin_config")
public class CheckinConfig implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer consecutiveDays;

    private Integer rewardPoints;

    private String rewardType;

    private String description;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
