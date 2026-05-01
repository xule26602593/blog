package com.blog.service.ai;

import com.blog.domain.dto.ProofreadResult;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface WritingAssistantService {

    SseEmitter generateOutline(String title, String description, String style);

    SseEmitter continueWriting(String context, String direction);

    SseEmitter polish(String content, String style);

    SseEmitter generateTitles(String content, int count);

    SseEmitter expandWriting(String content, String direction);

    SseEmitter rewriteWriting(String content, String style);

    ProofreadResult proofread(String content);
}
