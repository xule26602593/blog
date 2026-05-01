package com.blog.domain.dto;

import lombok.Data;

@Data
public class TopicQueryRequest {

    private String status; // PENDING/WRITING/PUBLISHED/ABANDONED

    private String priority; // HIGH/MEDIUM/LOW

    private Integer analysisStatus;

    private String keyword;

    private Integer pageNum = 1;

    private Integer pageSize = 10;
}
