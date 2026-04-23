package com.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("series_article")
public class SeriesArticle implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long seriesId;

    private Long articleId;

    private Integer chapterOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
