package com.blog.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * AI 标签提取结果结构
 */
public record TagListOutput(@JsonProperty(required = true, value = "tags") List<String> tags) {}
