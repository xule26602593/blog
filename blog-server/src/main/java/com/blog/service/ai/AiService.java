package com.blog.service.ai;

import java.util.Map;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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
