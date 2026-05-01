package com.blog.controller.admin;

import com.blog.common.exception.BusinessException;
import com.blog.common.result.Result;
import com.blog.domain.dto.AiSummaryRequest;
import com.blog.domain.dto.AiTagRequest;
import com.blog.domain.dto.ProofreadResult;
import com.blog.domain.dto.TagExtractResult;
import com.blog.domain.entity.PromptTemplate;
import com.blog.service.ai.PromptTemplateService;
import com.blog.service.ai.SummaryService;
import com.blog.service.ai.TagExtractService;
import com.blog.service.ai.WritingAssistantService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/ai")
@RequiredArgsConstructor
public class AiAdminController {

    private final SummaryService summaryService;
    private final TagExtractService tagExtractService;
    private final PromptTemplateService promptTemplateService;
    private final WritingAssistantService writingAssistantService;

    /**
     * 生成文章摘要
     */
    @PostMapping("/summary")
    public Result<String> generateSummary(@RequestBody AiSummaryRequest request) {
        String summary = summaryService.generateSummary(
            request.getTitle(),
            request.getContent(),
            request.getTemplateKey() != null ? request.getTemplateKey() : "summary_default"
        );
        return Result.success(summary);
    }

    /**
     * 提取文章标签
     */
    @PostMapping("/tags")
    public Result<TagExtractResult> extractTags(@RequestBody AiTagRequest request) {
        TagExtractResult result = tagExtractService.extractTags(
            request.getTitle(),
            request.getContent()
        );
        return Result.success(result);
    }

    /**
     * 获取 Prompt 模板列表
     */
    @GetMapping("/prompts")
    public Result<List<PromptTemplate>> listPrompts(@RequestParam(required = false) String category) {
        List<PromptTemplate> templates = promptTemplateService.listByCategory(category);
        return Result.success(templates);
    }

    /**
     * 创建 Prompt 模板
     */
    @PostMapping("/prompts")
    public Result<Void> createPrompt(@RequestBody PromptTemplate template) {
        promptTemplateService.save(template);
        return Result.success();
    }

    /**
     * 更新 Prompt 模板
     */
    @PutMapping("/prompts/{id}")
    public Result<Void> updatePrompt(@PathVariable Long id, @RequestBody PromptTemplate template) {
        template.setId(id);
        promptTemplateService.updateById(template);
        return Result.success();
    }

    /**
     * 删除 Prompt 模板
     */
    @DeleteMapping("/prompts/{id}")
    public Result<Void> deletePrompt(@PathVariable Long id) {
        promptTemplateService.removeById(id);
        return Result.success();
    }

    // ========== 写作助手接口 ==========

    /**
     * 写作辅助（流式）
     */
    @PostMapping("/writing/stream")
    public SseEmitter writingStream(@RequestBody Map<String, Object> request) {
        String type = (String) request.get("type");
        if (type == null || type.isBlank()) {
            SseEmitter emitter = new SseEmitter();
            emitter.completeWithError(new BusinessException("类型不能为空"));
            return emitter;
        }

        switch (type) {
            case "outline":
                String title = (String) request.get("title");
                if (title == null || title.isBlank()) {
                    SseEmitter emitter = new SseEmitter();
                    emitter.completeWithError(new BusinessException("标题不能为空"));
                    return emitter;
                }
                return writingAssistantService.generateOutline(
                    title,
                    (String) request.get("description"),
                    (String) request.getOrDefault("style", "tech")
                );
            case "continue":
                String context = (String) request.get("context");
                if (context == null || context.isBlank()) {
                    SseEmitter emitter = new SseEmitter();
                    emitter.completeWithError(new BusinessException("上下文内容不能为空"));
                    return emitter;
                }
                return writingAssistantService.continueWriting(
                    context,
                    (String) request.get("direction")
                );
            case "polish":
            case "titles":
            case "expand":
            case "rewrite":
                String content = (String) request.get("content");
                if (content == null || content.isBlank()) {
                    SseEmitter emitter = new SseEmitter();
                    emitter.completeWithError(new BusinessException("内容不能为空"));
                    return emitter;
                }
                return switch (type) {
                    case "polish" -> writingAssistantService.polish(
                        content,
                        (String) request.get("style")
                    );
                    case "titles" -> writingAssistantService.generateTitles(
                        content,
                        request.get("count") != null ? ((Number) request.get("count")).intValue() : 5
                    );
                    case "expand" -> writingAssistantService.expandWriting(
                        content,
                        (String) request.getOrDefault("direction", "丰富内容细节")
                    );
                    case "rewrite" -> writingAssistantService.rewriteWriting(
                        content,
                        (String) request.getOrDefault("style", "default")
                    );
                    default -> {
                        SseEmitter emitter = new SseEmitter();
                        emitter.completeWithError(new BusinessException("未知的写作辅助类型"));
                        yield emitter;
                    }
                };
            default:
                SseEmitter emitter = new SseEmitter();
                emitter.completeWithError(new BusinessException("未知的写作辅助类型"));
                return emitter;
        }
    }

    @PostMapping("/writing/expand")
    public SseEmitter expandWriting(@RequestBody Map<String, String> request) {
        String content = request.get("content");
        if (content == null || content.isBlank()) {
            SseEmitter emitter = new SseEmitter();
            emitter.completeWithError(new BusinessException("内容不能为空"));
            return emitter;
        }
        return writingAssistantService.expandWriting(
            content,
            request.getOrDefault("direction", "丰富内容细节")
        );
    }

    @PostMapping("/writing/rewrite")
    public SseEmitter rewriteWriting(@RequestBody Map<String, String> request) {
        String content = request.get("content");
        if (content == null || content.isBlank()) {
            SseEmitter emitter = new SseEmitter();
            emitter.completeWithError(new BusinessException("内容不能为空"));
            return emitter;
        }
        return writingAssistantService.rewriteWriting(
            content,
            request.getOrDefault("style", "default")
        );
    }

    @PostMapping("/writing/proofread")
    public Result<ProofreadResult> proofread(@RequestBody Map<String, String> request) {
        String content = request.get("content");
        if (content == null || content.isBlank()) {
            throw new BusinessException("内容不能为空");
        }
        ProofreadResult result = writingAssistantService.proofread(content);
        return Result.success(result);
    }
}
