package com.blog.service.impl.ai;

import com.blog.domain.dto.ProofreadError;
import com.blog.domain.dto.ProofreadResult;
import com.blog.service.ai.AiService;
import com.blog.service.ai.WritingAssistantService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class WritingAssistantServiceImpl implements WritingAssistantService {

    private final AiService aiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

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

    @Override
    public SseEmitter expandWriting(String content, String direction) {
        return aiService.generateStream("expand_writing", Map.of(
            "content", content != null ? content : "",
            "direction", direction != null ? direction : "丰富内容细节"
        ));
    }

    @Override
    public SseEmitter rewriteWriting(String content, String style) {
        String styleText = switch (style) {
            case "formal" -> "正式专业";
            case "casual" -> "轻松活泼";
            case "concise" -> "简洁精炼";
            default -> "通俗易懂";
        };
        return aiService.generateStream("rewrite_writing", Map.of(
            "content", content != null ? content : "",
            "style", styleText
        ));
    }

    @Override
    public ProofreadResult proofread(String content) {
        if (content == null || content.trim().isEmpty()) {
            return new ProofreadResult(new ArrayList<>(), content);
        }

        String result = aiService.generate("proofread_writing", Map.of(
            "content", truncate(content, 5000)
        ));

        try {
            int jsonStart = result.indexOf('{');
            int jsonEnd = result.lastIndexOf('}');
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                String json = result.substring(jsonStart, jsonEnd + 1);
                return objectMapper.readValue(json, ProofreadResult.class);
            }
        } catch (Exception e) {
            log.warn("Failed to parse proofread result: {}", e.getMessage());
        }

        return new ProofreadResult(new ArrayList<>(), content);
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) : text;
    }
}
