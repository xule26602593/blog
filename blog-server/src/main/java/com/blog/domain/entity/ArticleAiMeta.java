package com.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("article_ai_meta")
public class ArticleAiMeta {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long articleId;
    private String aiSummary;
    private String aiTags;
    private Integer summaryVersion;
    private Integer tagsVersion;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
