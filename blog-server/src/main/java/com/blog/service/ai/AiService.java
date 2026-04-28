package com.blog.service.ai;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

public interface AiService {

    /**
     * 同步生成
     */
    String generate(String templateKey, Map<String, Object> params);

    /**
     * 流式生成
     */
    SseEmitter generateStream(String templateKey, Map<String, Object> params);
}
