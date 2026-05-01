package com.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("article_daily_stats")
public class ArticleDailyStats implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long articleId;

    private LocalDate date;

    private Integer viewCount;

    private Integer likeCount;

    private Integer commentCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
