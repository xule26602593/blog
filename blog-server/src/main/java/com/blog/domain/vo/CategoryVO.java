package com.blog.domain.vo;

import lombok.Data;

@Data
public class CategoryVO {

    private Long id;

    private String name;

    private String description;

    private Integer sort;

    private Integer status;

    private Integer articleCount;
}
