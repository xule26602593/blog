package com.blog.service.impl.ai;

import com.blog.service.ai.AiService;
import com.blog.service.ai.SummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class SummaryServiceImpl implements SummaryService {

    private final AiService aiService;

    @Override
    public String generateSummary(String title, String content) {
        return generateSummary(title, content, "summary_default");
    }

    @Override
    public String generateSummary(String title, String content, String templateKey) {
        return aiService.generate(templateKey, Map.of(
            "title", title != null ? title : "",
            "content", truncate(content, 8000)
        ));
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) : text;
    }
}
