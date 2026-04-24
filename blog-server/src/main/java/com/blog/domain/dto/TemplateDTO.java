package com.blog.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TemplateDTO {

    @NotBlank(message = "模板名称不能为空")
    @Size(max = 100, message = "模板名称不能超过100字")
    private String name;

    @Size(max = 500, message = "模板描述不能超过500字")
    private String description;

    @NotBlank(message = "模板内容不能为空")
    private String content;

    private Long categoryId;

    private String tags;

    private Integer isDefault = 0;

    private Integer status = 1;
}
