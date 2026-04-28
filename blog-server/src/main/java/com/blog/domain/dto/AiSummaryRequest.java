package com.blog.domain.dto;

import lombok.Data;

@Data
public class AiSummaryRequest {
    private Long articleId;
    private String title;
    private String content;
    private String templateKey;
}
