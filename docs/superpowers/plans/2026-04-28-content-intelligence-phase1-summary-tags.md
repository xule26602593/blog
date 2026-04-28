# 内容智能化系统 - 阶段一：智能摘要 + 标签

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现文章智能摘要和智能标签提取功能。

**Architecture:** Spring AI + Sentinel 熔断 + 流式响应

**Prerequisites:**
- 完成阶段零：基础设施升级
- Spring AI 和 Sentinel 已正确配置
- 数据库表已创建

---

## Task 1.1: 创建实体类

**Files:**
- Create: `blog-server/src/main/java/com/blog/domain/entity/PromptTemplate.java`
- Create: `blog-server/src/main/java/com/blog/domain/entity/ArticleAiMeta.java`
- Create: `blog-server/src/main/java/com/blog/domain/entity/UserReadingProfile.java`

- [ ] **Step 1: 创建 PromptTemplate 实体**

```java
package com.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("prompt_template")
public class PromptTemplate {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String templateKey;
    private String templateName;
    private String category;
    private String systemPrompt;
    private String userTemplate;
    private String variables;
    private Integer isDefault;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

- [ ] **Step 2: 创建 ArticleAiMeta 实体**

```java
package com.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("article_ai_meta")
public class ArticleAiMeta {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long articleId;
    private String aiSummary;
    private String aiTags;
    private Integer summaryVersion;
    private Integer tagsVersion;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

- [ ] **Step 3: 创建 UserReadingProfile 实体**

```java
package com.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_reading_profile")
public class UserReadingProfile {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    private String preferredTags;
    private String preferredCategories;
    private String readingPattern;
    private LocalDateTime updateTime;
}
```

- [ ] **Step 4: 提交变更**

```bash
git add src/main/java/com/blog/domain/entity/
git commit -m "feat: add AI entity classes"
```

---

## Task 1.2: 创建 Mapper 接口

**Files:**
- Create: `blog-server/src/main/java/com/blog/repository/mapper/PromptTemplateMapper.java`
- Create: `blog-server/src/main/java/com/blog/repository/mapper/ArticleAiMetaMapper.java`
- Create: `blog-server/src/main/java/com/blog/repository/mapper/UserReadingProfileMapper.java`

- [ ] **Step 1: 创建 PromptTemplateMapper**

```java
package com.blog.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.domain.entity.PromptTemplate;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PromptTemplateMapper extends BaseMapper<PromptTemplate> {
}
```

- [ ] **Step 2: 创建 ArticleAiMetaMapper**

```java
package com.blog.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.domain.entity.ArticleAiMeta;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ArticleAiMetaMapper extends BaseMapper<ArticleAiMeta> {
}
```

- [ ] **Step 3: 创建 UserReadingProfileMapper**

```java
package com.blog.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.domain.entity.UserReadingProfile;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserReadingProfileMapper extends BaseMapper<UserReadingProfile> {
}
```

- [ ] **Step 4: 提交变更**

```bash
git add src/main/java/com/blog/repository/mapper/
git commit -m "feat: add AI mapper interfaces"
```

---

## Task 1.3: 创建 DTO 类

**Files:**
- Create: `blog-server/src/main/java/com/blog/domain/dto/AiSummaryRequest.java`
- Create: `blog-server/src/main/java/com/blog/domain/dto/AiTagRequest.java`
- Create: `blog-server/src/main/java/com/blog/domain/dto/TagExtractResult.java`

- [ ] **Step 1: 创建请求 DTO**

```java
package com.blog.domain.dto;

import lombok.Data;

@Data
public class AiSummaryRequest {
    private Long articleId;
    private String title;
    private String content;
    private String templateKey;
}

@Data
public class AiTagRequest {
    private Long articleId;
    private String title;
    private String content;
}

@Data
public class TagExtractResult {
    private java.util.List<com.blog.domain.entity.Tag> existingTags;
    private java.util.List<String> newTagNames;
}
```

- [ ] **Step 2: 提交变更**

```bash
git add src/main/java/com/blog/domain/dto/
git commit -m "feat: add AI request DTOs"
```

---

## Task 1.4: 创建 Sentinel 配置

**Files:**
- Create: `blog-server/src/main/java/com/blog/config/SentinelConfig.java`

- [ ] **Step 1: 创建 Sentinel 配置类**

```java
package com.blog.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.Collections;

@Configuration
public class SentinelConfig {
    
    @PostConstruct
    public void initRules() {
        // 熔断规则：基于异常比例
        DegradeRule degradeRule = new DegradeRule("aiService")
            .setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO)
            .setCount(0.5)
            .setTimeWindow(10)
            .setMinRequestAmount(5)
            .setStatIntervalMs(1000);
        
        // 流控规则：QPS 限流
        FlowRule flowRule = new FlowRule("aiService")
            .setGrade(RuleConstant.FLOW_GRADE_QPS)
            .setCount(10);
        
        DegradeRuleManager.loadRules(Collections.singletonList(degradeRule));
        FlowRuleManager.loadRules(Collections.singletonList(flowRule));
    }
}
```

- [ ] **Step 2: 提交变更**

```bash
git add src/main/java/com/blog/config/SentinelConfig.java
git commit -m "feat: add Sentinel configuration"
```

---

## Task 1.5: 创建 AI 服务基类

**Files:**
- Create: `blog-server/src/main/java/com/blog/service/ai/AiService.java`
- Create: `blog-server/src/main/java/com/blog/service/ai/PromptTemplateService.java`
- Create: `blog-server/src/main/java/com/blog/service/impl/ai/AiServiceImpl.java`
- Create: `blog-server/src/main/java/com/blog/service/impl/ai/PromptTemplateServiceImpl.java`

- [ ] **Step 1: 创建 AiService 接口**

```java
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
```

- [ ] **Step 2: 创建 PromptTemplateService 接口**

```java
package com.blog.service.ai;

import com.blog.domain.entity.PromptTemplate;

import java.util.List;

public interface PromptTemplateService {
    
    PromptTemplate getByKey(String templateKey);
    
    List<PromptTemplate> listByCategory(String category);
    
    List<PromptTemplate> listAll();
    
    void save(PromptTemplate template);
    
    void updateById(PromptTemplate template);
    
    void removeById(Long id);
    
    void validateTemplate(PromptTemplate template);
}
```

- [ ] **Step 3: 创建 AiServiceImpl**

```java
package com.blog.service.impl.ai;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.blog.common.exception.BusinessException;
import com.blog.domain.entity.PromptTemplate;
import com.blog.service.ai.AiService;
import com.blog.service.ai.PromptTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiServiceImpl implements AiService {
    
    private final ChatClient.Builder chatClientBuilder;
    private final PromptTemplateService templateService;
    
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
        String sanitized = content
            .replace("<user_content>", "")
            .replace("</user_content>", "");
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
            return chatClient.prompt()
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
            Flux<String> contentFlux = chatClient.prompt()
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
                        emitter.send(SseEmitter.event().data(content));
                    } catch (Exception e) {
                        log.error("发送SSE事件失败", e);
                        emitter.completeWithError(e);
                    }
                },
                error -> {
                    log.error("AI流式调用失败", error);
                    emitter.completeWithError(error);
                },
                () -> emitter.complete()
            );
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
```

- [ ] **Step 4: 创建 PromptTemplateServiceImpl**

```java
package com.blog.service.impl.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.exception.BusinessException;
import com.blog.domain.entity.PromptTemplate;
import com.blog.repository.mapper.PromptTemplateMapper;
import com.blog.service.ai.PromptTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromptTemplateServiceImpl implements PromptTemplateService {
    
    private static final int MAX_TEMPLATE_LENGTH = 2000;
    private static final Pattern DANGEROUS_PATTERNS = Pattern.compile(
        "(?i)(ignore|忽略|disregard).*(instruction|指令|prompt|提示|previous|之前)",
        Pattern.MULTILINE
    );
    
    private final PromptTemplateMapper promptTemplateMapper;
    
    @Override
    @Cacheable(value = "ai:prompt", key = "#templateKey")
    public PromptTemplate getByKey(String templateKey) {
        return promptTemplateMapper.selectOne(
            new LambdaQueryWrapper<PromptTemplate>()
                .eq(PromptTemplate::getTemplateKey, templateKey)
                .eq(PromptTemplate::getStatus, 1)
        );
    }
    
    @Override
    public List<PromptTemplate> listByCategory(String category) {
        return promptTemplateMapper.selectList(
            new LambdaQueryWrapper<PromptTemplate>()
                .eq(StringUtils.hasText(category), PromptTemplate::getCategory, category)
                .eq(PromptTemplate::getStatus, 1)
                .orderByAsc(PromptTemplate::getId)
        );
    }
    
    @Override
    public List<PromptTemplate> listAll() {
        return promptTemplateMapper.selectList(
            new LambdaQueryWrapper<PromptTemplate>()
                .eq(PromptTemplate::getStatus, 1)
                .orderByAsc(PromptTemplate::getId)
        );
    }
    
    @Override
    @CacheEvict(value = "ai:prompt", allEntries = true)
    public void save(PromptTemplate template) {
        validateTemplate(template);
        promptTemplateMapper.insert(template);
    }
    
    @Override
    @CacheEvict(value = "ai:prompt", allEntries = true)
    public void updateById(PromptTemplate template) {
        validateTemplate(template);
        promptTemplateMapper.updateById(template);
    }
    
    @Override
    @CacheEvict(value = "ai:prompt", allEntries = true)
    public void removeById(Long id) {
        promptTemplateMapper.deleteById(id);
    }
    
    @Override
    public void validateTemplate(PromptTemplate template) {
        // 1. 长度验证
        if (template.getSystemPrompt() != null && 
            template.getSystemPrompt().length() > MAX_TEMPLATE_LENGTH) {
            throw new BusinessException("系统提示词超过最大长度限制（" + MAX_TEMPLATE_LENGTH + "字）");
        }
        if (template.getUserTemplate() != null && 
            template.getUserTemplate().length() > MAX_TEMPLATE_LENGTH) {
            throw new BusinessException("用户模板超过最大长度限制（" + MAX_TEMPLATE_LENGTH + "字）");
        }
        
        // 2. 危险模式检测
        String content = template.getSystemPrompt() + " " + template.getUserTemplate();
        if (DANGEROUS_PATTERNS.matcher(content).find()) {
            throw new BusinessException("模板包含可疑指令模式，请检查内容");
        }
        
        // 3. 模板键名验证
        if (template.getTemplateKey() != null && 
            !template.getTemplateKey().matches("^[a-z][a-z0-9_]*$")) {
            throw new BusinessException("模板键名仅允许小写字母、数字和下划线，且必须以字母开头");
        }
    }
}
```

- [ ] **Step 5: 提交变更**

```bash
git add src/main/java/com/blog/service/
git commit -m "feat: add AI base service and prompt template service"
```

---

## Task 1.6: 创建摘要和标签服务

**Files:**
- Create: `blog-server/src/main/java/com/blog/service/ai/SummaryService.java`
- Create: `blog-server/src/main/java/com/blog/service/ai/TagExtractService.java`
- Create: `blog-server/src/main/java/com/blog/service/impl/ai/SummaryServiceImpl.java`
- Create: `blog-server/src/main/java/com/blog/service/impl/ai/TagExtractServiceImpl.java`

- [ ] **Step 1: 创建 SummaryService 接口**

```java
package com.blog.service.ai;

public interface SummaryService {
    
    /**
     * 生成摘要
     */
    String generateSummary(String title, String content);
    
    /**
     * 使用指定模板生成摘要
     */
    String generateSummary(String title, String content, String templateKey);
}
```

- [ ] **Step 2: 创建 TagExtractService 接口**

```java
package com.blog.service.ai;

import com.blog.domain.dto.TagExtractResult;

public interface TagExtractService {
    
    /**
     * 提取标签
     */
    TagExtractResult extractTags(String title, String content);
}
```

- [ ] **Step 3: 创建 SummaryServiceImpl**

```java
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
```

- [ ] **Step 4: 创建 TagExtractServiceImpl**

```java
package com.blog.service.impl.ai;

import com.blog.domain.dto.TagExtractResult;
import com.blog.domain.entity.Tag;
import com.blog.service.TagService;
import com.blog.service.ai.AiService;
import com.blog.service.ai.TagExtractService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TagExtractServiceImpl implements TagExtractService {
    
    private final AiService aiService;
    private final TagService tagService;
    private final ObjectMapper objectMapper;
    
    @Override
    public TagExtractResult extractTags(String title, String content) {
        String result = aiService.generate("tags_default", Map.of(
            "title", title != null ? title : "",
            "content", truncate(content, 6000)
        ));
        
        List<String> aiTags = parseTags(result);
        
        // 与现有标签库匹配
        List<Tag> existingTags = tagService.findByNames(aiTags);
        List<String> newTagNames = aiTags.stream()
            .filter(tag -> existingTags.stream().noneMatch(t -> t.getName().equals(tag)))
            .toList();
        
        TagExtractResult tagResult = new TagExtractResult();
        tagResult.setExistingTags(existingTags);
        tagResult.setNewTagNames(newTagNames);
        return tagResult;
    }
    
    private List<String> parseTags(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("解析标签失败: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
    
    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) : text;
    }
}
```

- [ ] **Step 5: 提交变更**

```bash
git add src/main/java/com/blog/service/
git commit -m "feat: add summary and tag extract services"
```

---

## Task 1.7: 创建 AI 管理端 Controller

**Files:**
- Create: `blog-server/src/main/java/com/blog/controller/admin/AiAdminController.java`

- [ ] **Step 1: 创建 AiAdminController**

```java
package com.blog.controller.admin;

import com.blog.common.result.Result;
import com.blog.domain.dto.AiSummaryRequest;
import com.blog.domain.dto.AiTagRequest;
import com.blog.domain.dto.TagExtractResult;
import com.blog.domain.entity.PromptTemplate;
import com.blog.service.ai.PromptTemplateService;
import com.blog.service.ai.SummaryService;
import com.blog.service.ai.TagExtractService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@RequestMapping("/api/admin/ai")
@RequiredArgsConstructor
public class AiAdminController {
    
    private final SummaryService summaryService;
    private final TagExtractService tagExtractService;
    private final PromptTemplateService promptTemplateService;
    
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
}
```

- [ ] **Step 2: 提交变更**

```bash
git add src/main/java/com/blog/controller/admin/AiAdminController.java
git commit -m "feat: add AI admin controller"
```

---

## Task 1.8: 创建前端 AI API 封装

**Files:**
- Create: `blog-web/src/api/ai.js`

- [ ] **Step 1: 创建 AI API 模块**

```javascript
import request from '@/utils/request'

/**
 * 获取认证令牌
 */
function getAuthToken() {
  return localStorage.getItem('token') || ''
}

/**
 * 生成摘要
 */
export function generateSummary(data) {
  return request.post('/api/admin/ai/summary', data)
}

/**
 * 提取标签
 */
export function extractTags(data) {
  return request.post('/api/admin/ai/tags', data)
}

/**
 * 流式请求（使用 fetch + ReadableStream）
 */
export function streamRequest(url, data, onMessage, onComplete, onError) {
  const controller = new AbortController()
  const token = getAuthToken()
  
  fetch(url, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': token ? `Bearer ${token}` : ''
    },
    body: JSON.stringify(data),
    signal: controller.signal
  })
    .then(response => {
      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`)
      }
      const reader = response.body.getReader()
      const decoder = new TextDecoder()
      
      function read() {
        reader.read().then(({ done, value }) => {
          if (done) {
            onComplete?.()
            return
          }
          const text = decoder.decode(value, { stream: true })
          const lines = text.split('\n')
          lines.forEach(line => {
            if (line.startsWith('data:')) {
              const content = line.slice(5).trim()
              if (content) {
                onMessage(content)
              }
            }
          })
          read()
        }).catch(error => {
          if (error.name !== 'AbortError') {
            onError?.(error)
          }
        })
      }
      read()
    })
    .catch(error => {
      if (error.name !== 'AbortError') {
        onError?.(error)
      }
    })
  
  return controller
}

/**
 * 获取 Prompt 模板列表
 */
export function getPromptTemplates(category) {
  return request.get('/api/admin/ai/prompts', { params: { category } })
}

/**
 * 保存 Prompt 模板
 */
export function savePromptTemplate(data) {
  return data.id 
    ? request.put(`/api/admin/ai/prompts/${data.id}`, data)
    : request.post('/api/admin/ai/prompts', data)
}

/**
 * 删除 Prompt 模板
 */
export function deletePromptTemplate(id) {
  return request.delete(`/api/admin/ai/prompts/${id}`)
}
```

- [ ] **Step 2: 提交变更**

```bash
git add blog-web/src/api/ai.js
git commit -m "feat: add frontend AI API module"
```

---

## Task 1.9: 创建流式状态机 composable

**Files:**
- Create: `blog-web/src/composables/useStreamState.js`

- [ ] **Step 1: 创建流式状态机**

```javascript
import { ref, computed } from 'vue'

/**
 * 流式响应状态机
 * 状态: idle | loading | streaming | paused | complete | error | cancelled | retrying
 */
export function useStreamState() {
  const state = ref('idle')
  const content = ref('')
  const errorMessage = ref('')
  
  // 状态判断
  const isIdle = computed(() => state.value === 'idle')
  const isLoading = computed(() => state.value === 'loading' || state.value === 'retrying')
  const isStreaming = computed(() => state.value === 'streaming')
  const isPaused = computed(() => state.value === 'paused')
  const isComplete = computed(() => state.value === 'complete')
  const isError = computed(() => state.value === 'error')
  const isCancelled = computed(() => state.value === 'cancelled')
  const canRetry = computed(() => ['error', 'cancelled', 'complete'].includes(state.value))
  const canCancel = computed(() => ['loading', 'streaming', 'paused', 'retrying'].includes(state.value))
  const canPause = computed(() => state.value === 'streaming')
  const canContinue = computed(() => state.value === 'paused')
  
  // 状态转换方法
  const start = () => {
    if (isIdle.value || canRetry.value) {
      state.value = 'loading'
      content.value = ''
      errorMessage.value = ''
    }
  }
  
  const firstByte = () => {
    if (isLoading.value) {
      state.value = 'streaming'
    }
  }
  
  const append = (chunk) => {
    if (isStreaming.value) {
      content.value += chunk
    }
  }
  
  const complete = () => {
    if (isStreaming.value) {
      state.value = 'complete'
    }
  }
  
  const error = (msg) => {
    state.value = 'error'
    errorMessage.value = msg
  }
  
  const cancel = () => {
    if (canCancel.value) {
      state.value = 'cancelled'
    }
  }
  
  const pause = () => {
    if (canPause.value) {
      state.value = 'paused'
    }
  }
  
  const continue_ = () => {
    if (canContinue.value) {
      state.value = 'streaming'
    }
  }
  
  const retry = () => {
    if (canRetry.value) {
      state.value = 'retrying'
      content.value = ''
      errorMessage.value = ''
    }
  }
  
  const reset = () => {
    state.value = 'idle'
    content.value = ''
    errorMessage.value = ''
  }
  
  return {
    state,
    content,
    errorMessage,
    // 状态判断
    isIdle,
    isLoading,
    isStreaming,
    isPaused,
    isComplete,
    isError,
    isCancelled,
    canRetry,
    canCancel,
    canPause,
    canContinue,
    // 状态转换
    start,
    firstByte,
    append,
    complete,
    error,
    cancel,
    pause,
    continue: continue_,
    retry,
    reset
  }
}
```

- [ ] **Step 2: 提交变更**

```bash
git add blog-web/src/composables/useStreamState.js
git commit -m "feat: add stream state machine composable"
```

---

## Task 1.10: 创建 AI 结果弹窗组件

**Files:**
- Create: `blog-web/src/components/AiResultDialog.vue`

- [ ] **Step 1: 创建 AiResultDialog 组件**

```vue
<template>
  <van-dialog
    v-model:show="visible"
    :title="title"
    :show-confirm-button="false"
    close-on-click-overlay
  >
    <div class="ai-result-dialog">
      <!-- 预览/编辑区域 -->
      <div class="content-area">
        <template v-if="!isEditing">
          <div class="preview-content" v-html="renderedContent"></div>
        </template>
        <template v-else>
          <van-field
            v-model="editedContent"
            type="textarea"
            rows="6"
            autosize
            placeholder="请编辑内容"
          />
        </template>
      </div>
      
      <!-- 操作按钮 -->
      <div class="action-buttons">
        <div class="left-buttons">
          <van-button v-if="!isEditing" size="small" @click="isEditing = true">
            编辑
          </van-button>
          <van-button v-else size="small" @click="isEditing = false">
            取消编辑
          </van-button>
          <van-button size="small" @click="handleCopy">
            复制
          </van-button>
        </div>
        <van-button type="primary" size="small" @click="handleApply">
          应用
        </van-button>
      </div>
    </div>
  </van-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { showToast } from 'vant'

const props = defineProps({
  show: Boolean,
  title: {
    type: String,
    default: 'AI生成结果'
  },
  content: String,
  type: {
    type: String,
    default: 'other'
  }
})

const emit = defineEmits(['apply', 'update:show'])

const visible = computed({
  get: () => props.show,
  set: (val) => emit('update:show', val)
})

const isEditing = ref(false)
const editedContent = ref('')

watch(() => props.content, (newVal) => {
  editedContent.value = newVal || ''
  isEditing.value = false
}, { immediate: true })

// 简单的 Markdown 渲染（实际项目可使用 md-editor-v3）
const renderedContent = computed(() => {
  return props.content?.replace(/\n/g, '<br>') || ''
})

const handleCopy = async () => {
  try {
    await navigator.clipboard.writeText(props.content)
    showToast({ type: 'success', message: '已复制' })
  } catch {
    showToast('复制失败')
  }
}

const handleApply = () => {
  const finalContent = isEditing.value ? editedContent.value : props.content
  emit('apply', finalContent)
  visible.value = false
}
</script>

<style scoped>
.ai-result-dialog {
  padding: 16px;
}

.content-area {
  min-height: 150px;
  max-height: 400px;
  overflow-y: auto;
  margin-bottom: 16px;
}

.preview-content {
  padding: 8px;
  background: #f7f8fa;
  border-radius: 4px;
  line-height: 1.6;
}

.action-buttons {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.left-buttons {
  display: flex;
  gap: 8px;
}
</style>
```

- [ ] **Step 2: 提交变更**

```bash
git add blog-web/src/components/AiResultDialog.vue
git commit -m "feat: add AI result dialog component"
```

---

## Task 1.11: 集成 AI 功能到文章编辑页

**Files:**
- Modify: `blog-web/src/views/admin/ArticleEdit.vue`

- [ ] **Step 1: 在 ArticleEdit.vue 中添加 AI 功能**

在 `<script setup>` 中添加导入和逻辑：

```javascript
// 添加导入
import { generateSummary, extractTags } from '@/api/ai'
import AiResultDialog from '@/components/AiResultDialog.vue'

// 添加状态
const showAiDialog = ref(false)
const aiDialogTitle = ref('')
const aiDialogContent = ref('')
const aiDialogType = ref('other')
const aiLoading = ref(false)

// 添加方法
const handleGenerateSummary = async () => {
  if (!form.title && !form.content) {
    showToast('请先填写标题或内容')
    return
  }
  
  aiLoading.value = true
  try {
    const res = await generateSummary({
      title: form.title,
      content: form.content
    })
    aiDialogTitle.value = 'AI生成的摘要'
    aiDialogContent.value = res.data
    aiDialogType.value = 'summary'
    showAiDialog.value = true
  } catch (error) {
    showToast('生成摘要失败')
  } finally {
    aiLoading.value = false
  }
}

const handleExtractTags = async () => {
  if (!form.title && !form.content) {
    showToast('请先填写标题或内容')
    return
  }
  
  aiLoading.value = true
  try {
    const res = await extractTags({
      title: form.title,
      content: form.content
    })
    // 将标签应用到表单
    if (res.data.newTagNames?.length > 0) {
      const newTagIds = res.data.newTagNames.map(name => {
        // 检查是否已存在同名标签
        const existing = tags.value.find(t => t.name === name)
        if (existing) return existing.id
        return null
      }).filter(Boolean)
      form.tagIds = [...new Set([...form.tagIds, ...newTagIds])]
    }
    showToast({ type: 'success', message: '标签提取成功' })
  } catch (error) {
    showToast('标签提取失败')
  } finally {
    aiLoading.value = false
  }
}

const handleApplyAiResult = (content) => {
  if (aiDialogType.value === 'summary') {
    form.summary = content
  }
}
```

在模板中添加 AI 按钮（在摘要字段后）：

```vue
<!-- 在摘要字段后添加 AI 按钮 -->
<van-field
  v-model="form.summary"
  label="摘要"
  type="textarea"
  rows="3"
  autosize
  placeholder="请输入文章摘要"
  maxlength="500"
  show-word-limit
>
  <template #button>
    <van-button size="small" :loading="aiLoading" @click="handleGenerateSummary">
      AI生成
    </van-button>
  </template>
</van-field>

<!-- 在标签字段后添加 AI 按钮 -->
<van-field
  v-model="tagNames"
  is-link
  readonly
  label="标签"
  placeholder="请选择标签"
  @click="showTagPicker = true"
>
  <template #button>
    <van-button size="small" :loading="aiLoading" @click.stop="handleExtractTags">
      AI提取
    </van-button>
  </template>
</van-field>

<!-- AI 结果弹窗 -->
<AiResultDialog
  v-model:show="showAiDialog"
  :title="aiDialogTitle"
  :content="aiDialogContent"
  :type="aiDialogType"
  @apply="handleApplyAiResult"
/>
```

- [ ] **Step 2: 提交变更**

```bash
git add blog-web/src/views/admin/ArticleEdit.vue
git commit -m "feat: integrate AI summary and tags in article edit"
```

---

## 完成检查

- [ ] 智能摘要生成功能正常
- [ ] 智能标签提取功能正常
- [ ] Sentinel 熔断降级生效
- [ ] Prompt 模板缓存工作正常
- [ ] 前端 AI 按钮交互正常
- [ ] 流式状态机工作正常

## 下一步

完成本阶段后，继续执行 `2026-04-28-content-intelligence-phase2-writing-assistant.md`
