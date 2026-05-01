package com.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

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
