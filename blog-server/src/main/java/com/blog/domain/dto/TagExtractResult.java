package com.blog.domain.dto;

import com.blog.domain.entity.Tag;
import lombok.Data;

import java.util.List;

@Data
public class TagExtractResult {
    private List<Tag> existingTags;
    private List<String> newTagNames;
}
