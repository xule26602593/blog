package com.blog.domain.dto;

import com.blog.domain.entity.Tag;
import java.util.List;
import lombok.Data;

@Data
public class TagExtractResult {
    private List<Tag> existingTags;
    private List<String> newTagNames;
}
