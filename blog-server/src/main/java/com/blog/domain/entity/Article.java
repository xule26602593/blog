package com.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("article")
public class Article implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String summary;

    private String content;

    private String coverImage;

    private Long categoryId;

    private Long authorId;

    private Long viewCount;

    private Long likeCount;

    private Integer commentCount;

    private Integer isTop;

    private Integer status;

    private LocalDateTime publishTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private String categoryName;

    @TableField(exist = false)
    private String authorName;

    @TableField(exist = false)
    private String authorAvatar;
}
