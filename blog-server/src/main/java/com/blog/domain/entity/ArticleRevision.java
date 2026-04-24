package com.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("article_revision")
public class ArticleRevision implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long articleId;

    private Integer version;

    private String title;

    private String content;

    private String summary;

    private Long editorId;

    private String changeNote;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
