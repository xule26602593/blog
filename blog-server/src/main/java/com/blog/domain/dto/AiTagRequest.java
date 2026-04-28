package com.blog.domain.dto;

import lombok.Data;

@Data
public class AiTagRequest {
    private Long articleId;
    private String title;
    private String content;
}
