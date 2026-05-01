package com.blog.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class TopicVO {

    private Long id;

    private String title;

    private String description;

    private String source;

    private String sourceUrl;

    private String sourceLink; // 别名，与 sourceUrl 相同

    private String analysis;

    private Integer analysisStatus;

    private String aiStatus; // 分析状态字符串: pending/analyzing/completed/failed

    private Boolean aiAnalyzed; // 是否已分析

    private Boolean aiAnalyzing; // 是否正在分析

    private Object aiAnalysisResult; // 解析后的分析结果

    private String status; // 状态字符串: PENDING/WRITING/PUBLISHED/ABANDONED

    private Long articleId;

    private String priority; // 优先级字符串: HIGH/MEDIUM/LOW

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
