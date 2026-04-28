package com.blog.service.ai;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface WritingAssistantService {

    /**
     * 生成大纲
     */
    SseEmitter generateOutline(String title, String description, String style);

    /**
     * 续写
     */
    SseEmitter continueWriting(String context, String direction);

    /**
     * 润色
     */
    SseEmitter polish(String content, String style);

    /**
     * 生成标题
     */
    SseEmitter generateTitles(String content, int count);
}
