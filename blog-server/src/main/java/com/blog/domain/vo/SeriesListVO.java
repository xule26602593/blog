package com.blog.domain.vo;

import java.time.LocalDateTime;
import lombok.Data;

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
