package com.blog.service.impl.ai;

import com.blog.domain.dto.TagExtractResult;
import com.blog.domain.entity.Tag;
import com.blog.service.TagService;
import com.blog.service.ai.AiService;
import com.blog.service.ai.TagExtractService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TagExtractServiceImpl implements TagExtractService {

    private final AiService aiService;
    private final TagService tagService;
    private final ObjectMapper objectMapper;

    @Override
    public TagExtractResult extractTags(String title, String content) {
        String result = aiService.generate(
                "tags_default", Map.of("title", title != null ? title : "", "content", truncate(content, 6000)));

        List<String> aiTags = parseTags(result);

        // 与现有标签库匹配
        List<Tag> existingTags = tagService.findByNames(aiTags);
        List<String> newTagNames = aiTags.stream()
                .filter(tag -> existingTags.stream().noneMatch(t -> t.getName().equals(tag)))
                .toList();

        TagExtractResult tagResult = new TagExtractResult();
        tagResult.setExistingTags(existingTags);
        tagResult.setNewTagNames(newTagNames);
        return tagResult;
    }

    private List<String> parseTags(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("解析标签失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) : text;
    }
}
