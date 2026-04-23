package com.blog.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SeriesListVO {

    private Long id;

    private String name;

    private String description;

    private String coverImage;

    private Integer mode;

    private Integer articleCount;

    private Long viewCount;

    private Integer status;

    private LocalDateTime createTime;
}
