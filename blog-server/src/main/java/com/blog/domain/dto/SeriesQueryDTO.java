package com.blog.domain.dto;

import lombok.Data;

@Data
public class SeriesQueryDTO {

    private Integer pageNum = 1;

    private Integer pageSize = 10;

    private String name;

    private Integer mode;

    private Integer status;
}
