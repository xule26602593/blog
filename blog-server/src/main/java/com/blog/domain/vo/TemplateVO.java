package com.blog.domain.vo;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class TemplateVO {

    private Long id;

    private String name;

    private String description;

    private String content;

    private Long categoryId;

    private String categoryName;

    private String tags;

    private Integer isDefault;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
