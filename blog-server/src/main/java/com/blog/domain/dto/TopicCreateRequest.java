package com.blog.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TopicCreateRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200")
    private String title;

    @Size(max = 5000, message = "描述长度不能超过5000")
    private String description;

    @Size(max = 100, message = "来源长度不能超过100")
    private String source;

    @Size(max = 500, message = "来源链接长度不能超过500")
    private String sourceUrl;

    private Integer priority = 2;

    private Boolean autoAnalyze = false;
}
