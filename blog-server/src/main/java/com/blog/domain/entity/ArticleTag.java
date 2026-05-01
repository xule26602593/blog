package com.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import lombok.Data;

@Data
@TableName("article_tag")
public class ArticleTag implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long articleId;

    private Long tagId;
}
