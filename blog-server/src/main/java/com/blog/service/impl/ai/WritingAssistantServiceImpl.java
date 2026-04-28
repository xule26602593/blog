package com.blog.service.impl.ai;

import com.blog.service.ai.AiService;
import com.blog.service.ai.WritingAssistantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class WritingAssistantServiceImpl implements WritingAssistantService {

    private final AiService aiService;

    @Override
    public SseEmitter generateOutline(String title, String description, String style) {
        String templateKey = "tech".equals(style) ? "outline_tech" : "outline_tutorial";
        return aiService.generateStream(templateKey, Map.of(
            "title", title != null ? title : "",
            "description", description != null ? description : "无"
        ));
    }

    @Override
    public SseEmitter continueWriting(String context, String direction) {
        return aiService.generateStream("continue_logic", Map.of(
            "context", context != null ? context : "",
            "direction", direction != null ? direction : "继续写"
        ));
    }

    @Override
    public SseEmitter polish(String content, String style) {
        String templateKey = "formal".equals(style) ? "polish_formal" : "polish_casual";
        return aiService.generateStream(templateKey, Map.of(
            "content", content != null ? content : ""
        ));
    }

    @Override
    public SseEmitter generateTitles(String content, int count) {
        return aiService.generateStream("titles_generate", Map.of(
            "content", truncate(content, 3000),
            "count", String.valueOf(Math.max(1, Math.min(count, 10)))
        ));
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) : text;
    }
}
