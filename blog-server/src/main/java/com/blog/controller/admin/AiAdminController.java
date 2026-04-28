package com.blog.controller.admin;

import com.blog.common.exception.BusinessException;
import com.blog.common.result.Result;
import com.blog.domain.dto.AiSummaryRequest;
import com.blog.domain.dto.AiTagRequest;
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
     * 生成大纲（流式）
     */
    @GetMapping("/writing/outline")
    public SseEmitter generateOutline(
        @RequestParam String title,
        @RequestParam(required = false) String description,
        @RequestParam(defaultValue = "tech") String style
    ) {
        return writingAssistantService.generateOutline(title, description, style);
    }

    /**
     * 写作辅助（流式）
     */
    @PostMapping("/writing/stream")
    public SseEmitter writingStream(@RequestBody Map<String, Object> request) {
        String type = (String) request.get("type");
        switch (type) {
            case "continue":
                return writingAssistantService.continueWriting(
                    (String) request.get("context"),
                    (String) request.get("direction")
                );
            case "polish":
                return writingAssistantService.polish(
                    (String) request.get("content"),
                    (String) request.get("style")
                );
            case "titles":
                return writingAssistantService.generateTitles(
                    (String) request.get("content"),
                    request.get("count") != null ? ((Number) request.get("count")).intValue() : 5
                );
            default:
                SseEmitter emitter = new SseEmitter();
                emitter.completeWithError(new BusinessException("未知的写作辅助类型"));
                return emitter;
        }
    }
}
