package com.blog.service.ai;

public interface SummaryService {

    /**
     * 生成摘要
     */
    String generateSummary(String title, String content);

    /**
     * 使用指定模板生成摘要
     */
    String generateSummary(String title, String content, String templateKey);
}
