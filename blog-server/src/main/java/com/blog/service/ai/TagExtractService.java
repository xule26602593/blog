package com.blog.service.ai;

import com.blog.domain.dto.TagExtractResult;

public interface TagExtractService {

    /**
     * 提取标签
     */
    TagExtractResult extractTags(String title, String content);
}
