package com.blog.service.impl.ai;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.blog.common.exception.BusinessException;
import com.blog.domain.entity.PromptTemplate;
import com.blog.service.ai.AiService;
import com.blog.service.ai.PromptTemplateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiServiceImpl implements AiService {

    private final ChatClient.Builder chatClientBuilder;
    private final PromptTemplateService templateService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ai.providers.primary.endpoint:}")
    private String primaryEndpoint;

    @Value("${ai.providers.secondary.endpoint:}")
    private String secondaryEndpoint;

    private volatile boolean usingBackup = false;

    /**
     * 使用 XML 标签隔离用户内容，防止提示注入
     */
    private String wrapUserContent(String content) {
        if (content == null) return "";
        String sanitized = content.replace("<user_content>", "").replace("</user_content>", "");
        return "<user_content>\n" + sanitized + "\n</user_content>";
    }

    @Override
    @SentinelResource(value = "aiService", blockHandler = "handleBlock", fallback = "handleFallback")
    public String generate(String templateKey, Map<String, Object> params) {
        PromptTemplate template = templateService.getByKey(templateKey);
        if (template == null) {
            throw new BusinessException("模板不存在: " + templateKey);
        }

        // 对所有用户输入进行隔离处理
        Map<String, Object> sanitizedParams = new HashMap<>();
        params.forEach((key, value) -> {
            if (value instanceof String) {
                sanitizedParams.put(key, wrapUserContent((String) value));
            } else {
                sanitizedParams.put(key, value);
            }
        });

        try {
            ChatClient chatClient = chatClientBuilder.build();
            return chatClient
                    .prompt()
                    .system(template.getSystemPrompt())
                    .user(u -> u.text(template.getUserTemplate())
                            .param("title", sanitizedParams.getOrDefault("title", ""))
                            .param("content", sanitizedParams.getOrDefault("content", ""))
                            .param("description", sanitizedParams.getOrDefault("description", ""))
                            .param("context", sanitizedParams.getOrDefault("context", ""))
                            .param("direction", sanitizedParams.getOrDefault("direction", ""))
                            .param("question", sanitizedParams.getOrDefault("question", ""))
                            .param("count", sanitizedParams.getOrDefault("count", "5")))
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("AI调用失败", e);
            throw new BusinessException("AI服务暂时不可用");
        }
    }

    @Override
    @SentinelResource(value = "aiService", blockHandler = "handleBlockStream", fallback = "handleFallbackStream")
    public SseEmitter generateStream(String templateKey, Map<String, Object> params) {
        PromptTemplate template = templateService.getByKey(templateKey);
        if (template == null) {
            SseEmitter emitter = new SseEmitter();
            emitter.completeWithError(new BusinessException("模板不存在: " + templateKey));
            return emitter;
        }

        // 对所有用户输入进行隔离处理
        Map<String, Object> sanitizedParams = new HashMap<>();
        params.forEach((key, value) -> {
            if (value instanceof String) {
                sanitizedParams.put(key, wrapUserContent((String) value));
            } else {
                sanitizedParams.put(key, value);
            }
        });

        SseEmitter emitter = new SseEmitter(180_000L);

        try {
            ChatClient chatClient = chatClientBuilder.build();
            Flux<String> contentFlux = chatClient
                    .prompt()
                    .system(template.getSystemPrompt())
                    .user(u -> u.text(template.getUserTemplate())
                            .param("title", sanitizedParams.getOrDefault("title", ""))
                            .param("content", sanitizedParams.getOrDefault("content", ""))
                            .param("description", sanitizedParams.getOrDefault("description", ""))
                            .param("context", sanitizedParams.getOrDefault("context", ""))
                            .param("direction", sanitizedParams.getOrDefault("direction", ""))
                            .param("question", sanitizedParams.getOrDefault("question", ""))
                            .param("count", sanitizedParams.getOrDefault("count", "5")))
                    .stream()
                    .content();

            contentFlux.subscribe(
                    content -> {
                        try {
                            Map<String, String> delta = Map.of("type", "delta", "text", content);
                            emitter.send(SseEmitter.event().data(objectMapper.writeValueAsString(delta)));
                        } catch (Exception e) {
                            log.error("发送SSE事件失败", e);
                            emitter.completeWithError(e);
                        }
                    },
                    error -> {
                        log.error("AI流式调用失败", error);
                        try {
                            Map<String, String> err = Map.of("type", "error", "message", error.getMessage());
                            emitter.send(SseEmitter.event().data(objectMapper.writeValueAsString(err)));
                        } catch (Exception ignored) {
                        }
                        emitter.completeWithError(error);
                    },
                    () -> {
                        try {
                            emitter.send(SseEmitter.event().data("[DONE]"));
                            emitter.complete();
                        } catch (Exception e) {
                            emitter.completeWithError(e);
                        }
                    });
        } catch (Exception e) {
            log.error("创建流式响应失败", e);
            emitter.completeWithError(e);
        }

        emitter.onTimeout(() -> {
            log.warn("SSE连接超时");
            emitter.complete();
        });

        emitter.onError(e -> log.error("SSE连接错误", e));

        return emitter;
    }

    // BlockHandler
    public String handleBlock(String templateKey, Map<String, Object> params, BlockException ex) {
        log.warn("AI服务被熔断或限流: {}", ex.getClass().getSimpleName());
        throw new BusinessException("AI服务繁忙，请稍后重试");
    }

    public SseEmitter handleBlockStream(String templateKey, Map<String, Object> params, BlockException ex) {
        log.warn("AI服务被熔断或限流: {}", ex.getClass().getSimpleName());
        SseEmitter emitter = new SseEmitter();
        emitter.completeWithError(new BusinessException("AI服务繁忙，请稍后重试"));
        return emitter;
    }

    // Fallback
    public String handleFallback(String templateKey, Map<String, Object> params, Throwable ex) {
        log.error("AI服务异常: {}", ex.getMessage());
        throw new BusinessException("AI服务暂时不可用");
    }

    public SseEmitter handleFallbackStream(String templateKey, Map<String, Object> params, Throwable ex) {
        log.error("AI服务异常: {}", ex.getMessage());
        SseEmitter emitter = new SseEmitter();
        emitter.completeWithError(new BusinessException("AI服务暂时不可用"));
        return emitter;
    }
}
