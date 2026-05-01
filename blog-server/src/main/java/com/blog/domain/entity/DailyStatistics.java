package com.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("daily_statistics")
public class DailyStatistics implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private LocalDate date;

    private Integer pv;

    private Integer uv;

    private Integer ipCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
