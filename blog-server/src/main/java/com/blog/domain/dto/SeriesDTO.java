package com.blog.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class SeriesDTO {

    private Long id;

    @NotBlank(message = "系列名称不能为空")
    private String name;

    private String description;

    private String coverImage;

    private Integer mode;

    private Integer sort;

    private Integer status;

    private List<Long> articleIds;
}
