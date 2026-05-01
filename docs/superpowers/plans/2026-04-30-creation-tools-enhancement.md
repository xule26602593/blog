# 创作工具增强系统实施计划

> 创建日期: 2026-04-30
> 状态: 待实施
> 设计文档: [创作工具增强系统设计文档](../specs/2026-04-30-creation-tools-enhancement-design.md)

---

## 一、实施概述

### 1.1 项目目标

为博客系统创作者提供三大核心功能：
1. **AI写作助手增强** - 续写、扩写、改写、润色、纠错等智能写作辅助
2. **写作模板系统** - 预设模板快速创作，支持自定义模板
3. **智能排版工具** - 一键排版、链接检查、格式规范化

### 1.2 技术栈确认

| 层级 | 现有技术 | 新增依赖 |
|------|----------|----------|
| 后端 | Spring Boot 3.5.14 + Spring AI 1.1.5 | 无 |
| 前端 | Vue 3.5 + Vant 4 + Pinia | 无 |
| 数据库 | MySQL 8.0 | 新增2张表 |

### 1.3 工时估算

| 阶段 | 功能模块 | 工时 |
|------|----------|------|
| 阶段一 | AI写作助手增强 | 1.5周 |
| 阶段二 | 写作模板系统 | 1.5周 |
| 阶段三 | 智能排版工具 | 1周 |
| **总计** | | **4周** |

---

## 二、阶段一：AI写作助手增强（1.5周）

### 2.1 任务分解

#### 任务 1.1：数据库 - 新增Prompt模板数据
**工时**: 0.5天
**优先级**: P0

**实施步骤**:
1. 在 `schema.sql` 中新增 prompt_template 表的数据插入语句
2. 新增4个写作相关的Prompt模板：
   - `writing_continue` - AI续写
   - `writing_expand` - AI扩写
   - `writing_rewrite` - AI改写
   - `writing_proofread` - AI纠错

**文件变更**:
```
blog-server/src/main/resources/db/schema.sql
```

**SQL脚本**:
```sql
-- AI续写模板
INSERT INTO `prompt_template` (`template_key`, `template_name`, `category`, `system_prompt`, `user_template`, `variables`, `is_default`) VALUES
('writing_continue', 'AI续写', 'writing',
 '你是一个专业的技术写作助手。根据上下文内容，自然地续写文章。要求：1.保持原有风格和语调 2.逻辑连贯 3.内容有价值 4.只返回续写内容，不要添加任何解释',
 '<user_content>\n上下文：\n{context}\n</user_content>\n\n请{direction}：',
 '{"variables": ["context", "direction"]}', 1),

-- AI扩写模板
('writing_expand', 'AI扩写', 'writing',
 '你是一个专业的技术写作助手。将简短内容扩展为详细段落。要求：1.增加必要的细节和说明 2.保持逻辑清晰 3.语言流畅 4.只返回扩写内容，不要添加任何解释',
 '<user_content>\n原文：\n{text}\n</user_content>\n\n请{expandType}：',
 '{"variables": ["text", "expandType"]}', 1),

-- AI改写模板
('writing_rewrite', 'AI改写', 'writing',
 '你是一个专业的技术写作助手。用不同的表达方式重写内容。要求：1.保持原意不变 2.改变表达方式 3.语言流畅 4.只返回改写内容，不要添加任何解释',
 '<user_content>\n原文：\n{text}\n</user_content>\n\n请用{style}风格改写：',
 '{"variables": ["text", "style"]}', 1),

-- AI纠错模板
('writing_proofread', 'AI纠错', 'writing',
 '你是一个专业的文字校对专家。检查文本中的错别字、语法错误和标点问题。返回JSON格式结果，格式如下：{"errorCount":数字,"errors":[{"type":"spelling或grammar或punctuation","original":"原文","suggestion":"建议","reason":"原因","position":{"start":数字,"end":数字}}]}。如果没有错误，返回{"errorCount":0,"errors":[]}',
 '<user_content>\n待检查文本：\n{text}\n</user_content>',
 '{"variables": ["text"]}', 1);
```

**验证方法**:
```bash
# 执行SQL后验证
SELECT * FROM prompt_template WHERE category = 'writing';
```

---

#### 任务 1.2：后端 - 扩展WritingAssistantService接口
**工时**: 0.5天
**优先级**: P0

**实施步骤**:
1. 在 `WritingAssistantService.java` 中新增接口方法
2. 新增4个方法：expandWriting, rewriteWriting, proofread

**文件变更**:
```
blog-server/src/main/java/com/blog/service/ai/WritingAssistantService.java
```

**代码变更**:
```java
package com.blog.service.ai;

import com.blog.domain.dto.ProofreadResult;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface WritingAssistantService {

    // ========== 现有方法 ==========
    
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

    // ========== 新增方法 ==========

    /**
     * 扩写
     * @param text 待扩写的文本
     * @param expandType 扩写类型：detail/reason/example/comparison
     * @return 流式响应
     */
    SseEmitter expandWriting(String text, String expandType);

    /**
     * 改写
     * @param text 待改写的文本
     * @param style 改写风格：formal/casual/academic/concise
     * @return 流式响应
     */
    SseEmitter rewriteWriting(String text, String style);

    /**
     * 纠错
     * @param text 待检测的文本
     * @return 纠错结果（非流式）
     */
    ProofreadResult proofread(String text);
}
```

---

#### 任务 1.3：后端 - 创建ProofreadResult DTO
**工时**: 0.25天
**优先级**: P0

**文件变更**:
```
blog-server/src/main/java/com/blog/domain/dto/ProofreadResult.java (新建)
blog-server/src/main/java/com/blog/domain/dto/ProofreadError.java (新建)
```

**ProofreadResult.java**:
```java
package com.blog.domain.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * AI纠错结果
 */
@Data
public class ProofreadResult {
    /**
     * 错误数量
     */
    private int errorCount;
    
    /**
     * 错误列表
     */
    private List<ProofreadError> errors = new ArrayList<>();
}
```

**ProofreadError.java**:
```java
package com.blog.domain.dto;

import lombok.Data;

/**
 * 纠错错误项
 */
@Data
public class ProofreadError {
    /**
     * 错误类型：spelling(错别字)/grammar(语法)/punctuation(标点)
     */
    private String type;
    
    /**
     * 原始文本
     */
    private String original;
    
    /**
     * 修改建议
     */
    private String suggestion;
    
    /**
     * 错误原因
     */
    private String reason;
    
    /**
     * 位置信息
     */
    private Position position;
    
    @Data
    public static class Position {
        private int start;
        private int end;
    }
}
```

---

#### 任务 1.4：后端 - 实现WritingAssistantServiceImpl新增方法
**工时**: 1天
**优先级**: P0

**实施步骤**:
1. 实现 expandWriting 方法
2. 实现 rewriteWriting 方法
3. 实现 proofread 方法（需处理JSON解析）

**文件变更**:
```
blog-server/src/main/java/com/blog/service/impl/ai/WritingAssistantServiceImpl.java
```

**代码变更**:
```java
// 在 WritingAssistantServiceImpl 类中新增以下方法

@Override
public SseEmitter expandWriting(String text, String expandType) {
    String expandTypeDesc = switch (expandType) {
        case "detail" -> "增加细节描述";
        case "reason" -> "添加原因分析";
        case "example" -> "添加实例说明";
        case "comparison" -> "添加对比分析";
        default -> "增加细节描述";
    };
    
    return aiService.generateStream("writing_expand", Map.of(
        "text", text != null ? text : "",
        "expandType", expandTypeDesc
    ));
}

@Override
public SseEmitter rewriteWriting(String text, String style) {
    String styleDesc = switch (style) {
        case "formal" -> "正式专业";
        case "casual" -> "轻松通俗";
        case "academic" -> "学术严谨";
        case "concise" -> "精简干练";
        default -> "正式专业";
    };
    
    return aiService.generateStream("writing_rewrite", Map.of(
        "text", text != null ? text : "",
        "style", styleDesc
    ));
}

@Override
public ProofreadResult proofread(String text) {
    // 纠错是非流式的，需要完整获取AI响应
    String result = aiService.generateSync("writing_proofread", Map.of(
        "text", text != null ? text : ""
    ));
    
    try {
        // 解析JSON结果
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(result, ProofreadResult.class);
    } catch (Exception e) {
        log.error("解析纠错结果失败", e);
        ProofreadResult errorResult = new ProofreadResult();
        errorResult.setErrorCount(0);
        return errorResult;
    }
}
```

**依赖说明**:
- 需要在 `AiService` 中新增 `generateSync` 方法用于非流式调用
- 需要注入 `ObjectMapper` 和 `log`

---

#### 任务 1.5：后端 - 扩展AiService支持同步调用
**工时**: 0.5天
**优先级**: P0

**实施步骤**:
1. 在 `AiService` 接口新增 `generateSync` 方法
2. 在 `AiServiceImpl` 中实现同步调用

**文件变更**:
```
blog-server/src/main/java/com/blog/service/ai/AiService.java
blog-server/src/main/java/com/blog/service/impl/ai/AiServiceImpl.java
```

**AiService.java 新增方法**:
```java
/**
 * 同步生成内容（非流式）
 * @param templateKey 模板key
 * @param variables 变量
 * @return 生成的内容
 */
String generateSync(String templateKey, Map<String, String> variables);
```

---

#### 任务 1.6：后端 - 扩展AiAdminController API
**工时**: 0.5天
**优先级**: P0

**实施步骤**:
1. 新增扩写API接口
2. 新增改写API接口
3. 新增纠错API接口
4. 扩展现有 `/writing/stream` 接口支持新类型

**文件变更**:
```
blog-server/src/main/java/com/blog/controller/admin/AiAdminController.java
```

**代码变更**:
```java
// 在 AiAdminController 类中新增以下方法

/**
 * 扩写（流式）
 */
@PostMapping("/writing/expand")
public SseEmitter expandWriting(@RequestBody Map<String, Object> request) {
    return writingAssistantService.expandWriting(
        (String) request.get("text"),
        (String) request.getOrDefault("expandType", "detail")
    );
}

/**
 * 改写（流式）
 */
@PostMapping("/writing/rewrite")
public SseEmitter rewriteWriting(@RequestBody Map<String, Object> request) {
    return writingAssistantService.rewriteWriting(
        (String) request.get("text"),
        (String) request.getOrDefault("style", "formal")
    );
}

/**
 * 纠错
 */
@PostMapping("/writing/proofread")
public Result<ProofreadResult> proofread(@RequestBody Map<String, Object> request) {
    ProofreadResult result = writingAssistantService.proofread(
        (String) request.get("text")
    );
    return Result.success(result);
}
```

同时更新 `/writing/stream` 接口：
```java
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
        case "expand":
            return writingAssistantService.expandWriting(
                (String) request.get("text"),
                (String) request.getOrDefault("expandType", "detail")
            );
        case "rewrite":
            return writingAssistantService.rewriteWriting(
                (String) request.get("text"),
                (String) request.getOrDefault("style", "formal")
            );
        default:
            SseEmitter emitter = new SseEmitter();
            emitter.completeWithError(new BusinessException("未知的写作辅助类型"));
            return emitter;
    }
}
```

---

#### 任务 1.7：前端 - 新增AI API调用方法
**工时**: 0.5天
**优先级**: P0

**文件变更**:
```
blog-web/src/api/ai.js
```

**代码变更**:
```javascript
/**
 * AI扩写（流式）
 */
export function expandWriting(data, onMessage, onComplete, onError) {
  return streamRequest(
    '/api/admin/ai/writing/expand',
    data,
    onMessage,
    onComplete,
    onError
  )
}

/**
 * AI改写（流式）
 */
export function rewriteWriting(data, onMessage, onComplete, onError) {
  return streamRequest(
    '/api/admin/ai/writing/rewrite',
    data,
    onMessage,
    onComplete,
    onError
  )
}

/**
 * AI纠错
 */
export function proofread(text) {
  return request.post('/api/admin/ai/writing/proofread', { text })
}
```

---

#### 任务 1.8：前端 - 扩展AiWritingPanel组件
**工时**: 2天
**优先级**: P0

**实施步骤**:
1. 在现有 `AiWritingPanel.vue` 中新增"扩写"、"改写"、"纠错"标签页
2. 实现各功能的设置选项和结果展示
3. 纠错功能需要特殊处理（非流式，结果列表展示）

**文件变更**:
```
blog-web/src/components/AiWritingPanel.vue (修改)
```

**主要修改点**:

1. 新增标签页：
```vue
<van-tab title="扩写" name="expand">
  <div class="tab-content">
    <van-field
      v-model="inputText"
      type="textarea"
      rows="4"
      placeholder="请输入需要扩写的简短内容"
    />
    <van-radio-group v-model="expandType" direction="horizontal">
      <van-radio name="detail">增加细节</van-radio>
      <van-radio name="reason">添加原因</van-radio>
      <van-radio name="example">添加实例</van-radio>
      <van-radio name="comparison">对比分析</van-radio>
    </van-radio-group>
  </div>
</van-tab>

<van-tab title="改写" name="rewrite">
  <div class="tab-content">
    <van-field
      v-model="inputText"
      type="textarea"
      rows="4"
      placeholder="请输入需要改写的内容"
    />
    <van-radio-group v-model="rewriteStyle" direction="horizontal">
      <van-radio name="formal">正式专业</van-radio>
      <van-radio name="casual">轻松通俗</van-radio>
      <van-radio name="academic">学术严谨</van-radio>
      <van-radio name="concise">精简干练</van-radio>
    </van-radio-group>
  </div>
</van-tab>

<van-tab title="纠错" name="proofread">
  <div class="tab-content">
    <van-field
      v-model="inputText"
      type="textarea"
      rows="6"
      placeholder="请输入需要检测的文本，AI将检查错别字、语法错误和标点问题"
    />
  </div>
</van-tab>
```

2. 新增纠错结果展示区域：
```vue
<!-- 纠错结果展示（特殊处理） -->
<div v-if="activeTab === 'proofread' && proofreadResult" class="proofread-result">
  <div class="result-header">
    <span>发现 {{ proofreadResult.errorCount }} 处问题</span>
  </div>
  <div v-if="proofreadResult.errorCount === 0" class="no-error">
    ✅ 未发现问题，内容很棒！
  </div>
  <div v-else class="error-list">
    <div v-for="(error, index) in proofreadResult.errors" :key="index" class="error-item">
      <div class="error-type">
        <van-tag :type="getErrorTagType(error.type)">{{ getErrorTypeName(error.type) }}</van-tag>
      </div>
      <div class="error-content">
        <div class="error-original">
          <span class="label">原文：</span>
          <span class="text">{{ error.original }}</span>
        </div>
        <div class="error-suggestion">
          <span class="label">建议：</span>
          <span class="text highlight">{{ error.suggestion }}</span>
        </div>
        <div class="error-reason">{{ error.reason }}</div>
      </div>
      <div class="error-actions">
        <van-button size="small" @click="applyProofreadFix(error)">采纳</van-button>
      </div>
    </div>
  </div>
</div>
```

3. 新增方法：
```javascript
const expandType = ref('detail')
const rewriteStyle = ref('formal')
const proofreadResult = ref(null)

// 纠错处理
const handleProofread = async () => {
  streamState.start()
  try {
    const { data } = await proofread(inputText.value)
    proofreadResult.value = data
    streamState.complete()
  } catch (error) {
    streamState.error(error.message)
  }
}

// 采纳纠错建议
const applyProofreadFix = (error) => {
  // 将修改建议应用到编辑器
  emit('apply-proofread', error)
}

// 获取错误类型标签样式
const getErrorTagType = (type) => {
  const types = {
    spelling: 'danger',
    grammar: 'warning',
    punctuation: 'primary'
  }
  return types[type] || 'default'
}

// 获取错误类型名称
const getErrorTypeName = (type) => {
  const names = {
    spelling: '错别字',
    grammar: '语法问题',
    punctuation: '标点建议'
  }
  return names[type] || '问题'
}
```

---

#### 任务 1.9：前端 - 集成到ArticleEdit页面
**工时**: 1天
**优先级**: P0

**实施步骤**:
1. 修改 `ArticleEdit.vue` 中的AI写作助手按钮，支持多种AI操作
2. 新增选中文本后右键菜单（可选，优先级P1）
3. 处理AI生成内容的应用到编辑器

**文件变更**:
```
blog-web/src/views/admin/ArticleEdit.vue
```

**主要修改点**:

1. 修改AI按钮为下拉菜单：
```vue
<div class="content-toolbar">
  <van-dropdown-menu>
    <van-dropdown-item ref="aiDropdown">
      <template #title>
        <van-button type="primary" size="small">✨ AI助手</van-button>
      </template>
      <div class="ai-menu">
        <van-cell title="续写" icon="edit" @click="openAiPanel('continue')" />
        <van-cell title="扩写" icon="orders-o" @click="openAiPanel('expand')" />
        <van-cell title="改写" icon="replay" @click="openAiPanel('rewrite')" />
        <van-cell title="润色" icon="brush-o" @click="openAiPanel('polish')" />
        <van-cell title="纠错" icon="checked" @click="openAiPanel('proofread')" />
        <van-divider />
        <van-cell title="生成摘要" icon="description" @click="handleGenerateSummary" />
        <van-cell title="生成标题" icon="label" @click="openAiPanel('titles')" />
      </div>
    </van-dropdown-item>
  </van-dropdown-menu>
</div>
```

2. 新增方法：
```javascript
const aiDefaultTab = ref('continue')

// 打开AI面板并设置默认标签
const openAiPanel = (tab) => {
  aiDefaultTab.value = tab
  showWritingPanel.value = true
}

// 处理AI内容应用到编辑器
const handleApplyAiContent = (content) => {
  // 在光标位置插入内容
  const editor = editorRef.value
  if (editor) {
    editor.insertText(content)
  }
}

// 处理纠错建议应用
const handleApplyProofread = (error) => {
  const editor = editorRef.value
  if (editor) {
    // 替换错误文本为建议文本
    editor.replaceText(error.original, error.suggestion)
  }
}
```

---

#### 任务 1.10：测试与优化
**工时**: 1天
**优先级**: P0

**测试清单**:

| 测试项 | 测试内容 | 预期结果 |
|--------|----------|----------|
| 续写功能 | 输入上下文，选择续写方向 | 流式输出续写内容 |
| 扩写功能 | 输入简短文本，选择扩写类型 | 流式输出扩写内容 |
| 改写功能 | 输入文本，选择改写风格 | 流式输出改写内容 |
| 润色功能 | 输入文本，选择润色风格 | 流式输出润色内容 |
| 纠错功能 | 输入带错误的文本 | 返回错误列表和修改建议 |
| 应用到编辑器 | 点击"应用到编辑器"按钮 | 内容正确插入编辑器 |
| 取消操作 | 流式输出中点击取消 | 正确中断请求 |
| 异常处理 | AI服务异常 | 显示错误提示，不崩溃 |

---

### 2.2 阶段一文件清单

| 文件 | 操作 | 说明 |
|------|------|------|
| `blog-server/src/main/resources/db/schema.sql` | 修改 | 新增Prompt模板数据 |
| `blog-server/src/main/java/com/blog/service/ai/WritingAssistantService.java` | 修改 | 新增接口方法 |
| `blog-server/src/main/java/com/blog/service/ai/AiService.java` | 修改 | 新增同步方法 |
| `blog-server/src/main/java/com/blog/service/impl/ai/WritingAssistantServiceImpl.java` | 修改 | 实现新方法 |
| `blog-server/src/main/java/com/blog/service/impl/ai/AiServiceImpl.java` | 修改 | 实现同步调用 |
| `blog-server/src/main/java/com/blog/controller/admin/AiAdminController.java` | 修改 | 新增API接口 |
| `blog-server/src/main/java/com/blog/domain/dto/ProofreadResult.java` | 新建 | 纠错结果DTO |
| `blog-server/src/main/java/com/blog/domain/dto/ProofreadError.java` | 新建 | 纠错错误项DTO |
| `blog-web/src/api/ai.js` | 修改 | 新增API调用方法 |
| `blog-web/src/components/AiWritingPanel.vue` | 修改 | 扩展功能标签页 |
| `blog-web/src/views/admin/ArticleEdit.vue` | 修改 | 集成AI助手功能 |

---

## 三、阶段二：写作模板系统（1.5周）

### 3.1 任务分解

#### 任务 2.1：数据库 - 创建writing_template表
**工时**: 0.5天
**优先级**: P0

**文件变更**:
```
blog-server/src/main/resources/db/schema.sql
```

**SQL脚本**:
```sql
-- =============================================
-- 写作模板表
-- =============================================
DROP TABLE IF EXISTS `writing_template`;
CREATE TABLE `writing_template` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '模板名称',
    `category` VARCHAR(50) NOT NULL COMMENT '分类：tutorial/principle/project/experience/note/essay',
    `icon` VARCHAR(10) DEFAULT '📄' COMMENT '图标emoji',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '模板描述',
    `sections` JSON NOT NULL COMMENT '章节结构JSON',
    `is_builtin` TINYINT DEFAULT 0 COMMENT '是否内置模板 0:否 1:是',
    `is_public` TINYINT DEFAULT 1 COMMENT '是否公开 0:否 1:是',
    `creator_id` BIGINT DEFAULT NULL COMMENT '创建者ID',
    `use_count` INT DEFAULT 0 COMMENT '使用次数',
    `status` TINYINT DEFAULT 1 COMMENT '状态 0:禁用 1:启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category`),
    KEY `idx_creator` (`creator_id`),
    KEY `idx_use_count` (`use_count` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='写作模板表';

-- 内置模板数据
INSERT INTO `writing_template` (`name`, `category`, `icon`, `description`, `sections`, `is_builtin`, `is_public`) VALUES
('Spring Boot 入门教程', 'tutorial', '📚', '适合Spring Boot入门教程类文章', 
 '[{"title":"前言","level":2,"placeholder":"介绍本文要解决的问题，目标读者需要具备的基础知识...","tips":"说明学习前提、Spring Boot版本"},{"title":"环境准备","level":2,"placeholder":"列出所需工具和依赖版本...","tips":"使用代码块展示pom.xml配置","required":true,"children":[{"title":"开发环境","level":3,"placeholder":"JDK版本、IDE、Maven版本..."},{"title":"项目创建","level":3,"placeholder":"创建Spring Boot项目步骤..."}]},{"title":"核心实现","level":2,"placeholder":"分步骤讲解实现过程...","tips":"每步包含目标、代码、说明","required":true},{"title":"运行测试","level":2,"placeholder":"启动项目并验证功能...","tips":"截图展示运行结果"},{"title":"常见问题","level":2,"placeholder":"记录可能遇到的问题...","tips":"Q&A格式"},{"title":"总结","level":2,"placeholder":"回顾要点，推荐延伸阅读...","tips":"简洁有力","required":true}]',
 1, 1),

('Vue3 实战教程', 'tutorial', '📚', '适合Vue3实战类文章',
 '[{"title":"前言","level":2,"placeholder":"介绍项目背景和要实现的功能..."},{"title":"技术选型","level":2,"placeholder":"说明技术栈选择和原因...","children":[{"title":"前端框架","level":3,"placeholder":"Vue3 + Vite + Pinia..."},{"title":"UI组件库","level":3,"placeholder":"Element Plus / Ant Design Vue..."}]},{"title":"项目搭建","level":2,"placeholder":"从零开始搭建项目...","required":true},{"title":"核心功能","level":2,"placeholder":"实现主要功能的步骤...","required":true},{"title":"优化部署","level":2,"placeholder":"性能优化和部署方案..."},{"title":"总结","level":2,"placeholder":"项目收获和改进方向...","required":true}]',
 1, 1),

('架构设计解析', 'principle', '🔬', '适合架构设计和原理分析类文章',
 '[{"title":"背景","level":2,"placeholder":"问题背景，为什么需要这个设计..."},{"title":"整体架构","level":2,"placeholder":"系统整体架构图和说明...","tips":"使用架构图+文字说明","required":true},{"title":"核心模块","level":2,"placeholder":"关键模块的设计思路...","required":true,"children":[]},{"title":"技术选型","level":2,"placeholder":"关键技术选型的考量..."},{"title":"性能考量","level":2,"placeholder":"性能优化策略..."},{"title":"总结","level":2,"placeholder":"设计亮点和改进空间...","required":true}]',
 1, 1),

('踩坑记录', 'experience', '💡', '适合记录开发中遇到的问题和解决方案',
 '[{"title":"问题背景","level":2,"placeholder":"什么场景下遇到的问题...","required":true},{"title":"问题现象","level":2,"placeholder":"描述问题的具体表现...","required":true},{"title":"排查过程","level":2,"placeholder":"如何一步步定位问题...","required":true},{"title":"解决方案","level":2,"placeholder":"最终如何解决...","required":true},{"title":"经验总结","level":2,"placeholder":"学到了什么，如何避免...","required":true}]',
 1, 1),

('技术书籍笔记', 'note', '📖', '适合技术书籍的阅读笔记',
 '[{"title":"书籍信息","level":2,"placeholder":"书名、作者、阅读时间..."},{"title":"核心内容","level":2,"placeholder":"书中的核心知识点...","required":true,"children":[]},{"title":"精彩摘录","level":2,"placeholder":"印象深刻的内容摘录..."},{"title":"实践应用","level":2,"placeholder":"如何将知识应用到实际..."},{"title":"推荐理由","level":2,"placeholder":"为什么推荐这本书..."}]',
 1, 1);
```

---

#### 任务 2.2：后端 - 创建WritingTemplate实体类
**工时**: 0.25天
**优先级**: P0

**文件变更**:
```
blog-server/src/main/java/com/blog/domain/entity/WritingTemplate.java (新建)
```

**代码**:
```java
package com.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 写作模板实体
 */
@Data
@TableName("writing_template")
public class WritingTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 分类：tutorial/principle/project/experience/note/essay
     */
    private String category;

    /**
     * 图标emoji
     */
    private String icon;

    /**
     * 模板描述
     */
    private String description;

    /**
     * 章节结构JSON
     */
    private String sections;

    /**
     * 是否内置模板
     */
    private Integer isBuiltin;

    /**
     * 是否公开
     */
    private Integer isPublic;

    /**
     * 创建者ID
     */
    private Long creatorId;

    /**
     * 使用次数
     */
    private Integer useCount;

    /**
     * 状态 0:禁用 1:启用
     */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer deleted;
}
```

---

#### 任务 2.3：后端 - 创建Repository层
**工时**: 0.25天
**优先级**: P0

**文件变更**:
```
blog-server/src/main/java/com/blog/repository/WritingTemplateMapper.java (新建)
```

**代码**:
```java
package com.blog.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.domain.entity.WritingTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface WritingTemplateMapper extends BaseMapper<WritingTemplate> {

    /**
     * 增加使用次数
     */
    @Update("UPDATE writing_template SET use_count = use_count + 1 WHERE id = #{id}")
    int incrementUseCount(Long id);
}
```

---

#### 任务 2.4：后端 - 创建Service层
**工时**: 0.5天
**优先级**: P0

**文件变更**:
```
blog-server/src/main/java/com/blog/service/WritingTemplateService.java (新建)
blog-server/src/main/java/com/blog/service/impl/WritingTemplateServiceImpl.java (新建)
```

**WritingTemplateService.java**:
```java
package com.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.domain.entity.WritingTemplate;
import java.util.List;

public interface WritingTemplateService {

    /**
     * 分页查询模板
     */
    Page<WritingTemplate> page(int pageNum, int pageSize, String category, Long creatorId);

    /**
     * 获取所有启用的模板（公开+自己的）
     */
    List<WritingTemplate> listAvailable(String category, Long currentUserId);

    /**
     * 获取模板详情
     */
    WritingTemplate getById(Long id);

    /**
     * 创建模板
     */
    void create(WritingTemplate template);

    /**
     * 更新模板
     */
    void update(WritingTemplate template);

    /**
     * 删除模板
     */
    void delete(Long id);

    /**
     * 使用模板（增加使用次数）
     */
    void useTemplate(Long id);

    /**
     * 获取模板分类列表
     */
    List<CategoryInfo> getCategories();

    @lombok.Data
    class CategoryInfo {
        private String key;
        private String label;
        private Integer count;
    }
}
```

**WritingTemplateServiceImpl.java**:
```java
package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.domain.entity.WritingTemplate;
import com.blog.repository.WritingTemplateMapper;
import com.blog.service.WritingTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WritingTemplateServiceImpl implements WritingTemplateService {

    private final WritingTemplateMapper templateMapper;

    @Override
    public Page<WritingTemplate> page(int pageNum, int pageSize, String category, Long creatorId) {
        Page<WritingTemplate> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<WritingTemplate> wrapper = new LambdaQueryWrapper<>();
        
        if (category != null && !category.isEmpty()) {
            wrapper.eq(WritingTemplate::getCategory, category);
        }
        if (creatorId != null) {
            wrapper.eq(WritingTemplate::getCreatorId, creatorId);
        }
        
        wrapper.orderByDesc(WritingTemplate::getUseCount);
        return templateMapper.selectPage(page, wrapper);
    }

    @Override
    public List<WritingTemplate> listAvailable(String category, Long currentUserId) {
        LambdaQueryWrapper<WritingTemplate> wrapper = new LambdaQueryWrapper<>();
        
        // 公开模板 或 自己的模板
        wrapper.and(w -> w
            .eq(WritingTemplate::getIsPublic, 1)
            .or()
            .eq(WritingTemplate::getCreatorId, currentUserId)
        );
        
        // 启用状态
        wrapper.eq(WritingTemplate::getStatus, 1);
        
        if (category != null && !category.isEmpty()) {
            wrapper.eq(WritingTemplate::getCategory, category);
        }
        
        wrapper.orderByDesc(WritingTemplate::getUseCount);
        return templateMapper.selectList(wrapper);
    }

    @Override
    public WritingTemplate getById(Long id) {
        return templateMapper.selectById(id);
    }

    @Override
    public void create(WritingTemplate template) {
        template.setIsBuiltin(0);
        template.setUseCount(0);
        template.setStatus(1);
        templateMapper.insert(template);
    }

    @Override
    public void update(WritingTemplate template) {
        // 内置模板不允许修改
        WritingTemplate existing = templateMapper.selectById(template.getId());
        if (existing != null && existing.getIsBuiltin() == 1) {
            throw new RuntimeException("内置模板不允许修改");
        }
        templateMapper.updateById(template);
    }

    @Override
    public void delete(Long id) {
        // 内置模板不允许删除
        WritingTemplate existing = templateMapper.selectById(id);
        if (existing != null && existing.getIsBuiltin() == 1) {
            throw new RuntimeException("内置模板不允许删除");
        }
        templateMapper.deleteById(id);
    }

    @Override
    public void useTemplate(Long id) {
        templateMapper.incrementUseCount(id);
    }

    @Override
    public List<CategoryInfo> getCategories() {
        List<CategoryInfo> categories = new ArrayList<>();
        
        String[][] categoryDefs = {
            {"tutorial", "技术教程"},
            {"principle", "技术原理"},
            {"project", "项目实战"},
            {"experience", "经验分享"},
            {"note", "读书笔记"},
            {"essay", "随笔日志"}
        };
        
        for (String[] def : categoryDefs) {
            CategoryInfo info = new CategoryInfo();
            info.setKey(def[0]);
            info.setLabel(def[1]);
            
            Long count = templateMapper.selectCount(
                new LambdaQueryWrapper<WritingTemplate>()
                    .eq(WritingTemplate::getCategory, def[0])
                    .eq(WritingTemplate::getStatus, 1)
                    .eq(WritingTemplate::getIsPublic, 1)
            );
            info.setCount(count.intValue());
            
            categories.add(info);
        }
        
        return categories;
    }
}
```

---

#### 任务 2.5：后端 - 创建Controller层
**工时**: 0.5天
**优先级**: P0

**文件变更**:
```
blog-server/src/main/java/com/blog/controller/admin/WritingTemplateController.java (新建)
```

**代码**:
```java
package com.blog.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.Result;
import com.blog.domain.entity.WritingTemplate;
import com.blog.service.WritingTemplateService;
import com.blog.security.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/templates")
@RequiredArgsConstructor
public class WritingTemplateController {

    private final WritingTemplateService templateService;

    /**
     * 获取模板列表
     */
    @GetMapping
    public Result<List<WritingTemplate>> list(
        @RequestParam(required = false) String category
    ) {
        Long currentUserId = SecurityUtil.getCurrentUserId();
        List<WritingTemplate> templates = templateService.listAvailable(category, currentUserId);
        return Result.success(templates);
    }

    /**
     * 分页查询模板（管理用）
     */
    @GetMapping("/page")
    public Result<Page<WritingTemplate>> page(
        @RequestParam(defaultValue = "1") int pageNum,
        @RequestParam(defaultValue = "10") int pageSize,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) Long creatorId
    ) {
        Page<WritingTemplate> page = templateService.page(pageNum, pageSize, category, creatorId);
        return Result.success(page);
    }

    /**
     * 获取模板详情
     */
    @GetMapping("/{id}")
    public Result<WritingTemplate> getById(@PathVariable Long id) {
        WritingTemplate template = templateService.getById(id);
        return Result.success(template);
    }

    /**
     * 获取模板分类
     */
    @GetMapping("/categories")
    public Result<List<WritingTemplateService.CategoryInfo>> getCategories() {
        List<WritingTemplateService.CategoryInfo> categories = templateService.getCategories();
        return Result.success(categories);
    }

    /**
     * 创建模板
     */
    @PostMapping
    public Result<Void> create(@RequestBody WritingTemplate template) {
        template.setCreatorId(SecurityUtil.getCurrentUserId());
        templateService.create(template);
        return Result.success();
    }

    /**
     * 更新模板
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody WritingTemplate template) {
        template.setId(id);
        templateService.update(template);
        return Result.success();
    }

    /**
     * 删除模板
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        templateService.delete(id);
        return Result.success();
    }

    /**
     * 使用模板（增加使用次数）
     */
    @PostMapping("/{id}/use")
    public Result<Void> useTemplate(@PathVariable Long id) {
        templateService.useTemplate(id);
        return Result.success();
    }
}
```

---

#### 任务 2.6：前端 - 创建模板API
**工时**: 0.25天
**优先级**: P0

**文件变更**:
```
blog-web/src/api/template.js (新建)
```

**代码**:
```javascript
import request from '@/utils/request'

/**
 * 获取模板列表
 */
export function getTemplates(category) {
  return request.get('/api/admin/templates', { params: { category } })
}

/**
 * 分页查询模板
 */
export function getTemplatePage(params) {
  return request.get('/api/admin/templates/page', { params })
}

/**
 * 获取模板详情
 */
export function getTemplate(id) {
  return request.get(`/api/admin/templates/${id}`)
}

/**
 * 获取模板分类
 */
export function getTemplateCategories() {
  return request.get('/api/admin/templates/categories')
}

/**
 * 创建模板
 */
export function createTemplate(data) {
  return request.post('/api/admin/templates', data)
}

/**
 * 更新模板
 */
export function updateTemplate(id, data) {
  return request.put(`/api/admin/templates/${id}`, data)
}

/**
 * 删除模板
 */
export function deleteTemplate(id) {
  return request.delete(`/api/admin/templates/${id}`)
}

/**
 * 使用模板（增加使用次数）
 */
export function useTemplate(id) {
  return request.post(`/api/admin/templates/${id}/use`)
}
```

---

#### 任务 2.7：前端 - 创建模板状态管理
**工时**: 0.5天
**优先级**: P1

**文件变更**:
```
blog-web/src/stores/template.js (新建)
```

**代码**:
```javascript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getTemplates, getTemplateCategories } from '@/api/template'

export const useTemplateStore = defineStore('template', () => {
  const templates = ref([])
  const categories = ref([])
  const loading = ref(false)
  const currentCategory = ref('')

  // 按分类分组
  const groupedTemplates = computed(() => {
    const groups = {}
    categories.value.forEach(cat => {
      groups[cat.key] = {
        label: cat.label,
        items: templates.value.filter(t => t.category === cat.key)
      }
    })
    return groups
  })

  // 热门模板
  const hotTemplates = computed(() => {
    return [...templates.value]
      .sort((a, b) => b.useCount - a.useCount)
      .slice(0, 6)
  })

  async function fetchTemplates(category) {
    loading.value = true
    try {
      const { data } = await getTemplates(category)
      templates.value = data
    } finally {
      loading.value = false
    }
  }

  async function fetchCategories() {
    const { data } = await getTemplateCategories()
    categories.value = data
  }

  return {
    templates,
    categories,
    loading,
    currentCategory,
    groupedTemplates,
    hotTemplates,
    fetchTemplates,
    fetchCategories
  }
})
```

---

#### 任务 2.8：前端 - 创建模板选择器组件
**工时**: 1.5天
**优先级**: P0

**文件变更**:
```
blog-web/src/components/writing/TemplateSelector.vue (新建)
blog-web/src/components/writing/TemplateCard.vue (新建)
```

**TemplateCard.vue**:
```vue
<template>
  <div class="template-card" @click="$emit('select', template)">
    <div class="card-icon">{{ template.icon }}</div>
    <div class="card-content">
      <div class="card-name">{{ template.name }}</div>
      <div class="card-desc">{{ template.description }}</div>
      <div class="card-meta">
        <span class="use-count">使用 {{ template.useCount }} 次</span>
        <van-tag v-if="template.isBuiltin" size="small" type="primary">内置</van-tag>
      </div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  template: {
    type: Object,
    required: true
  }
})

defineEmits(['select'])
</script>

<style scoped>
.template-card {
  display: flex;
  gap: 12px;
  padding: 12px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #eee;
  cursor: pointer;
  transition: all 0.2s;
}

.template-card:hover {
  border-color: var(--van-primary-color);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.card-icon {
  font-size: 32px;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  border-radius: 8px;
}

.card-content {
  flex: 1;
  min-width: 0;
}

.card-name {
  font-weight: 500;
  margin-bottom: 4px;
}

.card-desc {
  font-size: 12px;
  color: #666;
  margin-bottom: 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #999;
}
</style>
```

**TemplateSelector.vue**:
```vue
<template>
  <van-popup
    v-model:show="visible"
    position="bottom"
    :style="{ height: '80%' }"
    round
  >
    <div class="template-selector">
      <!-- 头部 -->
      <div class="selector-header">
        <h3>选择写作模板</h3>
        <van-icon name="cross" @click="close" />
      </div>

      <!-- 搜索栏 -->
      <div class="selector-search">
        <van-search
          v-model="searchKeyword"
          placeholder="搜索模板..."
          shape="round"
        />
      </div>

      <!-- 分类标签 -->
      <div class="selector-categories">
        <van-tag
          v-for="cat in categories"
          :key="cat.key"
          :type="currentCategory === cat.key ? 'primary' : 'default'"
          round
          @click="currentCategory = cat.key"
        >
          {{ cat.label }} ({{ cat.count }})
        </van-tag>
        <van-tag
          :type="currentCategory === '' ? 'primary' : 'default'"
          round
          @click="currentCategory = ''"
        >
          全部
        </van-tag>
      </div>

      <!-- 模板列表 -->
      <div class="selector-content">
        <van-loading v-if="loading" size="24px" vertical>加载中...</van-loading>
        
        <template v-else>
          <!-- 按分类分组显示 -->
          <div v-for="(group, key) in groupedTemplates" :key="key" class="template-group">
            <template v-if="group.items.length > 0 && (currentCategory === '' || currentCategory === key)">
              <div class="group-title">{{ group.label }}</div>
              <div class="template-grid">
                <TemplateCard
                  v-for="template in group.items"
                  :key="template.id"
                  :template="template"
                  @select="handleSelect"
                />
              </div>
            </template>
          </div>

          <!-- 空状态 -->
          <van-empty v-if="filteredTemplates.length === 0" description="暂无模板" />
        </template>
      </div>

      <!-- 底部操作 -->
      <div class="selector-footer">
        <van-checkbox v-model="skipTemplate">使用空白模板（自由写作）</van-checkbox>
        <van-button type="primary" size="small" @click="confirmBlank" v-if="skipTemplate">
          开始写作
        </van-button>
      </div>
    </div>
  </van-popup>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useTemplateStore } from '@/stores/template'
import TemplateCard from './TemplateCard.vue'

const props = defineProps({
  show: Boolean
})

const emit = defineEmits(['update:show', 'select'])

const templateStore = useTemplateStore()

const visible = computed({
  get: () => props.show,
  set: (val) => emit('update:show', val)
})

const searchKeyword = ref('')
const currentCategory = ref('')
const skipTemplate = ref(false)

const { templates, categories, loading, groupedTemplates } = templateStore

// 过滤后的模板
const filteredTemplates = computed(() => {
  let result = templates.value
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(t => 
      t.name.toLowerCase().includes(keyword) ||
      t.description?.toLowerCase().includes(keyword)
    )
  }
  if (currentCategory.value) {
    result = result.filter(t => t.category === currentCategory.value)
  }
  return result
})

// 选择模板
const handleSelect = (template) => {
  emit('select', template)
  close()
}

// 使用空白模板
const confirmBlank = () => {
  emit('select', null)
  close()
}

const close = () => {
  visible.value = false
}

// 加载数据
onMounted(async () => {
  await Promise.all([
    templateStore.fetchTemplates(),
    templateStore.fetchCategories()
  ])
})

// 重置状态
watch(() => props.show, (val) => {
  if (val) {
    searchKeyword.value = ''
    currentCategory.value = ''
    skipTemplate.value = false
  }
})
</script>

<style scoped>
.template-selector {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.selector-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #eee;
}

.selector-header h3 {
  margin: 0;
  font-size: 16px;
}

.selector-search {
  padding: 8px 16px;
}

.selector-categories {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 0 16px 16px;
  border-bottom: 1px solid #eee;
}

.selector-content {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.template-group {
  margin-bottom: 24px;
}

.group-title {
  font-size: 14px;
  font-weight: 500;
  color: #666;
  margin-bottom: 12px;
}

.template-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 12px;
}

.selector-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-top: 1px solid #eee;
}
</style>
```

---

#### 任务 2.9：前端 - 集成模板功能到ArticleEdit
**工时**: 1天
**优先级**: P0

**实施步骤**:
1. 在 `ArticleEdit.vue` 中添加模板选择入口
2. 实现模板应用逻辑
3. 处理模板章节转换为编辑器内容

**文件变更**:
```
blog-web/src/views/admin/ArticleEdit.vue
```

**主要修改**:

1. 新增模板选择器：
```vue
<template>
  <!-- 在工具栏添加模板按钮 -->
  <div class="content-toolbar">
    <van-dropdown-menu>
      <!-- AI助手下拉 -->
      <van-dropdown-item ref="aiDropdown">
        <!-- AI菜单内容 -->
      </van-dropdown-item>
      
      <!-- 模板下拉 -->
      <van-dropdown-item ref="templateDropdown">
        <template #title>
          <van-button size="small">📄 模板</van-button>
        </template>
        <van-cell-group>
          <van-cell title="从模板创建" icon="description" @click="showTemplateSelector" />
          <van-cell title="保存为模板" icon="bookmark-o" @click="showSaveTemplateDialog" />
          <van-cell title="我的模板" icon="user-o" @click="showMyTemplates" />
        </van-cell-group>
      </van-dropdown-item>
    </van-dropdown-menu>
  </div>

  <!-- 模板选择器 -->
  <TemplateSelector
    v-model:show="showTemplateSelectorPopup"
    @select="handleTemplateSelect"
  />

  <!-- 保存模板对话框 -->
  <van-dialog
    v-model:show="showSaveTemplateDialog"
    title="保存为模板"
    show-cancel-button
    @confirm="saveAsTemplate"
  >
    <van-field v-model="templateName" label="模板名称" placeholder="请输入模板名称" />
    <van-field
      v-model="templateDescription"
      label="描述"
      type="textarea"
      rows="2"
      placeholder="请输入模板描述"
    />
    <van-field label="分类">
      <template #input>
        <van-radio-group v-model="templateCategory" direction="horizontal">
          <van-radio name="tutorial">教程</van-radio>
          <van-radio name="principle">原理</van-radio>
          <van-radio name="experience">经验</van-radio>
          <van-radio name="note">笔记</van-radio>
        </van-radio-group>
      </template>
    </van-field>
    <van-field name="isPublic" label="公开">
      <template #input>
        <van-switch v-model="templateIsPublic" />
      </template>
    </van-field>
  </van-dialog>
</template>
```

2. 新增方法：
```javascript
import { useTemplateStore } from '@/stores/template'
import { createTemplate, useTemplate } from '@/api/template'

const templateStore = useTemplateStore()

const showTemplateSelectorPopup = ref(false)
const showSaveTemplateDialog = ref(false)
const templateName = ref('')
const templateDescription = ref('')
const templateCategory = ref('tutorial')
const templateIsPublic = ref(true)

// 显示模板选择器
const showTemplateSelector = () => {
  showTemplateSelectorPopup.value = true
}

// 选择模板后的处理
const handleTemplateSelect = async (template) => {
  if (!template) {
    // 空白模板
    return
  }

  // 增加使用次数
  await useTemplate(template.id)

  // 解析模板章节
  const sections = JSON.parse(template.sections)
  
  // 转换为编辑器内容
  let content = ''
  sections.forEach(section => {
    content += `${'#'.repeat(section.level)} ${section.title}\n\n`
    if (section.tips) {
      content += `> 💡 提示：${section.placeholder}\n`
      if (section.tips) {
        content += `> ${section.tips}\n`
      }
      content += '\n'
    } else if (section.placeholder) {
      content += `> 💡 提示：${section.placeholder}\n\n`
    }
    
    // 处理子章节
    if (section.children && section.children.length > 0) {
      section.children.forEach(child => {
        content += `${'#'.repeat(child.level)} ${child.title}\n\n`
        if (child.placeholder) {
          content += `> 💡 提示：${child.placeholder}\n\n`
        }
      })
    }
  })

  // 应用到编辑器
  form.value.content = content
}

// 显示保存模板对话框
const showSaveTemplateDialog = () => {
  if (!form.value.content) {
    showToast('请先编写内容')
    return
  }
  templateName.value = form.value.title || ''
  showSaveTemplateDialog.value = true
}

// 保存为模板
const saveAsTemplate = async () => {
  if (!templateName.value) {
    showToast('请输入模板名称')
    return
  }

  try {
    // 解析当前内容为章节结构（简化处理）
    const sections = parseContentToSections(form.value.content)
    
    await createTemplate({
      name: templateName.value,
      category: templateCategory.value,
      icon: '📝',
      description: templateDescription.value,
      sections: JSON.stringify(sections),
      isPublic: templateIsPublic.value ? 1 : 0
    })
    
    showToast('保存成功')
    templateStore.fetchTemplates()
  } catch (error) {
    showToast('保存失败')
  }
}

// 解析内容为章节结构（简化版）
const parseContentToSections = (content) => {
  const lines = content.split('\n')
  const sections = []
  
  lines.forEach(line => {
    const match = line.match(/^(#{1,6})\s+(.+)$/)
    if (match) {
      const level = match[1].length
      const title = match[2]
      sections.push({
        title,
        level,
        placeholder: '',
        tips: '',
        required: false
      })
    }
  })
  
  return sections
}
```

---

### 3.2 阶段二文件清单

| 文件 | 操作 | 说明 |
|------|------|------|
| `blog-server/src/main/resources/db/schema.sql` | 修改 | 新增模板表和初始数据 |
| `blog-server/src/main/java/com/blog/domain/entity/WritingTemplate.java` | 新建 | 模板实体类 |
| `blog-server/src/main/java/com/blog/repository/WritingTemplateMapper.java` | 新建 | 数据访问层 |
| `blog-server/src/main/java/com/blog/service/WritingTemplateService.java` | 新建 | 服务接口 |
| `blog-server/src/main/java/com/blog/service/impl/WritingTemplateServiceImpl.java` | 新建 | 服务实现 |
| `blog-server/src/main/java/com/blog/controller/admin/WritingTemplateController.java` | 新建 | API控制器 |
| `blog-web/src/api/template.js` | 新建 | 前端API |
| `blog-web/src/stores/template.js` | 新建 | 状态管理 |
| `blog-web/src/components/writing/TemplateCard.vue` | 新建 | 模板卡片组件 |
| `blog-web/src/components/writing/TemplateSelector.vue` | 新建 | 模板选择器组件 |
| `blog-web/src/views/admin/ArticleEdit.vue` | 修改 | 集成模板功能 |

---

## 四、阶段三：智能排版工具（1周）

### 4.1 任务分解

#### 任务 3.1：数据库 - 创建format_rule表
**工时**: 0.5天
**优先级**: P0

**文件变更**:
```
blog-server/src/main/resources/db/schema.sql
```

**SQL脚本**:
```sql
-- =============================================
-- 排版规则配置表
-- =============================================
DROP TABLE IF EXISTS `format_rule`;
CREATE TABLE `format_rule` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `rule_key` VARCHAR(50) NOT NULL COMMENT '规则标识',
    `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '规则描述',
    `rule_type` VARCHAR(50) NOT NULL COMMENT '类型：regex/ai/http',
    `rule_config` JSON NOT NULL COMMENT '规则配置JSON',
    `priority` INT DEFAULT 0 COMMENT '执行优先级（数字越小越先执行）',
    `is_default` TINYINT DEFAULT 1 COMMENT '是否默认启用',
    `status` TINYINT DEFAULT 1 COMMENT '状态 0:禁用 1:启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_rule_key` (`rule_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排版规则配置表';

-- 初始化规则数据
INSERT INTO `format_rule` (`rule_key`, `rule_name`, `description`, `rule_type`, `rule_config`, `priority`, `is_default`) VALUES
('heading_normalize', '标题层级规范化', '确保标题层级连续，避免跳级', 'regex',
 '{"patterns":[{"pattern":"^#{1}\\s","replaceWith":"## "},{"pattern":"^#{4,}\\s","replaceWith":"### "]}]', 
 1, 1),

('empty_line_cleanup', '空行清理', '删除多余空行', 'regex',
 '{"pattern":"\\\\n{3,}","replacement":"\\\\n\\\\n"}', 
 10, 1),

('chinese_english_spacing', '中英文间距', '中文与英文/数字间添加空格', 'regex',
 '{"patterns":[{"pattern":"([\\u4e00-\\u9fa5])([a-zA-Z0-9])","replacement":"$1 $2"},{"pattern":"([a-zA-Z0-9])([\\u4e00-\\u9fa5])","replacement":"$1 $2"}]}',
 5, 1),

('punctuation_normalize', '标点规范化', '规范化中文标点', 'regex',
 '{"patterns":[{"pattern":",","replacement":"，"},{"pattern":"\\. ","replacement":"。 "},{"pattern":"!","replacement":"！"},{"pattern":"\\?","replacement":"？"}],"skipInCode":true}',
 8, 1),

('code_block_format', '代码块格式化', '为代码块添加语言标识', 'regex',
 '{"patterns":[{"pattern":"```\\n","replacement":"```java\\n"},{"pattern":"```\\s*$","replacement":"```java\\n"}]}',
 20, 1),

('paragraph_merge', '段落合并', '合并过短段落', 'ai',
 '{"minLength":50,"prompt":"将以下过短的段落合理合并，保持语义连贯和自然过渡：\\n{paragraphs}"}',
 50, 0),

('paragraph_split', '段落拆分', '拆分过长段落', 'ai',
 '{"maxLength":500,"prompt":"将以下过长的段落合理拆分为2-3段，每段表达一个完整意思：\\n{paragraph}"}',
 51, 0),

('link_check', '链接检查', '检测无效链接', 'http',
 '{"timeout":5000,"allowedStatusCodes":[200,301,302]}',
 100, 1);
```

---

#### 任务 3.2：后端 - 创建FormatRule实体类
**工时**: 0.25天
**优先级**: P0

**文件变更**:
```
blog-server/src/main/java/com/blog/domain/entity/FormatRule.java (新建)
```

**代码**:
```java
package com.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 排版规则配置实体
 */
@Data
@TableName("format_rule")
public class FormatRule {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 规则标识
     */
    private String ruleKey;

    /**
     * 规则名称
     */
    private String ruleName;

    /**
     * 规则描述
     */
    private String description;

    /**
     * 规则类型：regex/ai/http
     */
    private String ruleType;

    /**
     * 规则配置JSON
     */
    private String ruleConfig;

    /**
     * 执行优先级
     */
    private Integer priority;

    /**
     * 是否默认启用
     */
    private Integer isDefault;

    /**
     * 状态
     */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

---

#### 任务 3.3：后端 - 创建排版服务
**工时**: 2天
**优先级**: P0

**文件变更**:
```
blog-server/src/main/java/com/blog/service/FormatService.java (新建)
blog-server/src/main/java/com/blog/service/impl/FormatServiceImpl.java (新建)
```

**FormatService.java**:
```java
package com.blog.service;

import com.blog.domain.dto.FormatPreviewResult;
import com.blog.domain.dto.LinkCheckResult;
import java.util.List;

public interface FormatService {

    /**
     * 预览排版变更
     */
    FormatPreviewResult preview(String content, List<String> ruleKeys);

    /**
     * 应用排版规则
     */
    String apply(String content, List<String> ruleKeys);

    /**
     * 获取所有排版规则
     */
    List<FormatRuleInfo> getRules();

    /**
     * 更新规则状态
     */
    void updateRuleStatus(Long id, Integer status);

    /**
     * 检查链接
     */
    LinkCheckResult checkLinks(String content);

    @lombok.Data
    class FormatRuleInfo {
        private Long id;
        private String ruleKey;
        private String ruleName;
        private String description;
        private String ruleType;
        private Integer priority;
        private Integer isDefault;
        private Integer status;
    }
}
```

**FormatServiceImpl.java** (核心实现):
```java
package com.blog.service.impl;

import com.blog.domain.dto.FormatPreviewResult;
import com.blog.domain.dto.LinkCheckResult;
import com.blog.domain.entity.FormatRule;
import com.blog.repository.FormatRuleMapper;
import com.blog.service.AiService;
import com.blog.service.FormatService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class FormatServiceImpl implements FormatService {

    private final FormatRuleMapper ruleMapper;
    private final AiService aiService;
    private final ObjectMapper objectMapper;

    @Override
    public FormatPreviewResult preview(String content, List<String> ruleKeys) {
        FormatPreviewResult result = new FormatPreviewResult();
        result.setChanges(new ArrayList<>());
        
        List<FormatRule> rules = getEnabledRules(ruleKeys);
        
        for (FormatRule rule : rules) {
            FormatPreviewResult.Change change = applyRulePreview(content, rule);
            if (change != null && change.getCount() > 0) {
                result.getChanges().add(change);
            }
        }
        
        result.setTotalChanges(result.getChanges().stream()
            .mapToInt(FormatPreviewResult.Change::getCount)
            .sum());
        
        return result;
    }

    @Override
    public String apply(String content, List<String> ruleKeys) {
        List<FormatRule> rules = getEnabledRules(ruleKeys);
        
        String result = content;
        for (FormatRule rule : rules) {
            result = applyRule(result, rule);
        }
        
        return result;
    }

    @Override
    public List<FormatRuleInfo> getRules() {
        List<FormatRule> rules = ruleMapper.selectList(null);
        return rules.stream().map(this::toRuleInfo).toList();
    }

    @Override
    public void updateRuleStatus(Long id, Integer status) {
        FormatRule rule = new FormatRule();
        rule.setId(id);
        rule.setStatus(status);
        ruleMapper.updateById(rule);
    }

    @Override
    public LinkCheckResult checkLinks(String content) {
        LinkCheckResult result = new LinkCheckResult();
        result.setLinks(new ArrayList<>());
        
        // 提取所有URL
        Pattern urlPattern = Pattern.compile("https?://[^\\s\\)]+");
        Matcher matcher = urlPattern.matcher(content);
        
        List<CompletableFuture<LinkCheckResult.LinkInfo>> futures = new ArrayList<>();
        int lineNum = 1;
        int lineStart = 0;
        
        while (matcher.find()) {
            String url = matcher.group();
            final int currentLine = findLineNumber(content, matcher.start());
            
            futures.add(checkSingleLink(url, currentLine));
        }
        
        // 等待所有检查完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        for (CompletableFuture<LinkCheckResult.LinkInfo> future : futures) {
            try {
                result.getLinks().add(future.get());
            } catch (Exception e) {
                log.error("获取链接检查结果失败", e);
            }
        }
        
        result.setTotal(result.getLinks().size());
        result.setValid((int) result.getLinks().stream().filter(LinkCheckResult.LinkInfo::isValid).count());
        result.setInvalid(result.getTotal() - result.getValid());
        
        return result;
    }

    // ========== 私有方法 ==========

    private List<FormatRule> getEnabledRules(List<String> ruleKeys) {
        if (ruleKeys == null || ruleKeys.isEmpty()) {
            return ruleMapper.selectList(
                new LambdaQueryWrapper<FormatRule>()
                    .eq(FormatRule::getStatus, 1)
                    .eq(FormatRule::getIsDefault, 1)
                    .orderByAsc(FormatRule::getPriority)
            );
        }
        
        return ruleMapper.selectList(
            new LambdaQueryWrapper<FormatRule>()
                .in(FormatRule::getRuleKey, ruleKeys)
                .eq(FormatRule::getStatus, 1)
                .orderByAsc(FormatRule::getPriority)
        );
    }

    private FormatPreviewResult.Change applyRulePreview(String content, FormatRule rule) {
        try {
            JsonNode config = objectMapper.readTree(rule.getRuleConfig());
            
            switch (rule.getRuleType()) {
                case "regex":
                    return applyRegexPreview(content, config, rule);
                case "ai":
                    // AI规则不提供预览
                    return null;
                default:
                    return null;
            }
        } catch (Exception e) {
            log.error("预览规则失败: {}", rule.getRuleKey(), e);
            return null;
        }
    }

    private FormatPreviewResult.Change applyRegexPreview(String content, JsonNode config, FormatRule rule) {
        FormatPreviewResult.Change change = new FormatPreviewResult.Change();
        change.setRule(rule.getRuleKey());
        change.setDescription(rule.getRuleName());
        change.setDetails(new ArrayList<>());
        
        if (config.has("pattern")) {
            // 单个模式
            String pattern = config.get("pattern").asText();
            String replacement = config.get("replacement").asText();
            
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(content);
            
            while (m.find()) {
                FormatPreviewResult.Change.Detail detail = new FormatPreviewResult.Change.Detail();
                detail.setLine(findLineNumber(content, m.start()));
                detail.setFrom(m.group());
                detail.setTo(replacement);
                change.getDetails().add(detail);
            }
        } else if (config.has("patterns")) {
            // 多个模式
            for (JsonNode patternNode : config.get("patterns")) {
                String pattern = patternNode.get("pattern").asText();
                String replaceWith = patternNode.get("replaceWith").asText();
                
                Pattern p = Pattern.compile(pattern, Pattern.MULTILINE);
                Matcher m = p.matcher(content);
                
                while (m.find()) {
                    FormatPreviewResult.Change.Detail detail = new FormatPreviewResult.Change.Detail();
                    detail.setLine(findLineNumber(content, m.start()));
                    detail.setFrom(m.group());
                    detail.setTo(replaceWith);
                    change.getDetails().add(detail);
                }
            }
        }
        
        change.setCount(change.getDetails().size());
        return change;
    }

    private String applyRule(String content, FormatRule rule) {
        try {
            JsonNode config = objectMapper.readTree(rule.getRuleConfig());
            
            switch (rule.getRuleType()) {
                case "regex":
                    return applyRegex(content, config);
                case "ai":
                    return applyAiRule(content, config, rule);
                default:
                    return content;
            }
        } catch (Exception e) {
            log.error("应用规则失败: {}", rule.getRuleKey(), e);
            return content;
        }
    }

    private String applyRegex(String content, JsonNode config) {
        String result = content;
        
        if (config.has("pattern")) {
            String pattern = config.get("pattern").asText();
            String replacement = config.get("replacement").asText();
            result = result.replaceAll(pattern, replacement);
        } else if (config.has("patterns")) {
            for (JsonNode patternNode : config.get("patterns")) {
                String pattern = patternNode.get("pattern").asText();
                String replacement = patternNode.has("replacement") 
                    ? patternNode.get("replacement").asText()
                    : patternNode.get("replaceWith").asText();
                result = result.replaceAll(pattern, replacement);
            }
        }
        
        return result;
    }

    private String applyAiRule(String content, JsonNode config, FormatRule rule) {
        // AI规则暂不实现，返回原内容
        log.warn("AI规则暂未实现: {}", rule.getRuleKey());
        return content;
    }

    private int findLineNumber(String content, int position) {
        return content.substring(0, position).split("\n").length;
    }

    @Async
    private CompletableFuture<LinkCheckResult.LinkInfo> checkSingleLink(String url, int line) {
        LinkCheckResult.LinkInfo info = new LinkCheckResult.LinkInfo();
        info.setUrl(url);
        info.setLine(line);
        
        try {
            // 使用HttpClient检查链接
            java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                .connectTimeout(java.time.Duration.ofSeconds(5))
                .followRedirects(java.net.http.HttpClient.Redirect.NORMAL)
                .build();
            
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(java.time.Duration.ofSeconds(5))
                .method("HEAD", java.net.http.HttpRequest.BodyPublishers.noBody())
                .build();
            
            java.net.http.HttpResponse<Void> response = client.send(request, 
                java.net.http.HttpResponse.BodyHandlers.discarding());
            
            info.setStatus(response.statusCode());
            info.setValid(response.statusCode() >= 200 && response.statusCode() < 400);
            
            if (!info.isValid()) {
                info.setError("HTTP " + response.statusCode());
            }
        } catch (Exception e) {
            info.setValid(false);
            info.setError(e.getMessage());
        }
        
        return CompletableFuture.completedFuture(info);
    }

    private FormatRuleInfo toRuleInfo(FormatRule rule) {
        FormatRuleInfo info = new FormatRuleInfo();
        info.setId(rule.getId());
        info.setRuleKey(rule.getRuleKey());
        info.setRuleName(rule.getRuleName());
        info.setDescription(rule.getDescription());
        info.setRuleType(rule.getRuleType());
        info.setPriority(rule.getPriority());
        info.setIsDefault(rule.getIsDefault());
        info.setStatus(rule.getStatus());
        return info;
    }
}
```

---

#### 任务 3.4：后端 - 创建DTO类
**工时**: 0.25天
**优先级**: P0

**文件变更**:
```
blog-server/src/main/java/com/blog/domain/dto/FormatPreviewResult.java (新建)
blog-server/src/main/java/com/blog/domain/dto/LinkCheckResult.java (新建)
```

**FormatPreviewResult.java**:
```java
package com.blog.domain.dto;

import lombok.Data;
import java.util.List;

@Data
public class FormatPreviewResult {
    private List<Change> changes;
    private int totalChanges;

    @Data
    public static class Change {
        private String rule;
        private String description;
        private int count;
        private List<Detail> details;
    }

    @Data
    public static class Detail {
        private int line;
        private String from;
        private String to;
        private String action;
    }
}
```

**LinkCheckResult.java**:
```java
package com.blog.domain.dto;

import lombok.Data;
import java.util.List;

@Data
public class LinkCheckResult {
    private int total;
    private int valid;
    private int invalid;
    private List<LinkInfo> links;

    @Data
    public static class LinkInfo {
        private String url;
        private int line;
        private int status;
        private boolean valid;
        private String error;
    }
}
```

---

#### 任务 3.5：后端 - 创建排版Controller
**工时**: 0.5天
**优先级**: P0

**文件变更**:
```
blog-server/src/main/java/com/blog/controller/admin/FormatController.java (新建)
```

**代码**:
```java
package com.blog.controller.admin;

import com.blog.common.result.Result;
import com.blog.domain.dto.FormatPreviewResult;
import com.blog.domain.dto.LinkCheckResult;
import com.blog.service.FormatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/format")
@RequiredArgsConstructor
public class FormatController {

    private final FormatService formatService;

    /**
     * 预览排版变更
     */
    @PostMapping("/preview")
    public Result<FormatPreviewResult> preview(@RequestBody Map<String, Object> request) {
        String content = (String) request.get("content");
        @SuppressWarnings("unchecked")
        List<String> rules = (List<String>) request.get("rules");
        
        FormatPreviewResult result = formatService.preview(content, rules);
        return Result.success(result);
    }

    /**
     * 应用排版规则
     */
    @PostMapping("/apply")
    public Result<String> apply(@RequestBody Map<String, Object> request) {
        String content = (String) request.get("content");
        @SuppressWarnings("unchecked")
        List<String> rules = (List<String>) request.get("rules");
        
        String result = formatService.apply(content, rules);
        return Result.success(result);
    }

    /**
     * 获取排版规则列表
     */
    @GetMapping("/rules")
    public Result<List<FormatService.FormatRuleInfo>> getRules() {
        List<FormatService.FormatRuleInfo> rules = formatService.getRules();
        return Result.success(rules);
    }

    /**
     * 更新规则状态
     */
    @PutMapping("/rules/{id}")
    public Result<Void> updateRuleStatus(
        @PathVariable Long id,
        @RequestBody Map<String, Integer> request
    ) {
        formatService.updateRuleStatus(id, request.get("status"));
        return Result.success();
    }

    /**
     * 检查链接
     */
    @PostMapping("/check-links")
    public Result<LinkCheckResult> checkLinks(@RequestBody Map<String, String> request) {
        String content = request.get("content");
        LinkCheckResult result = formatService.checkLinks(content);
        return Result.success(result);
    }
}
```

---

#### 任务 3.6：前端 - 创建排版API
**工时**: 0.25天
**优先级**: P0

**文件变更**:
```
blog-web/src/api/format.js (新建)
```

**代码**:
```javascript
import request from '@/utils/request'

/**
 * 预览排版变更
 */
export function previewFormat(content, rules) {
  return request.post('/api/admin/format/preview', { content, rules })
}

/**
 * 应用排版规则
 */
export function applyFormat(content, rules) {
  return request.post('/api/admin/format/apply', { content, rules })
}

/**
 * 获取排版规则列表
 */
export function getFormatRules() {
  return request.get('/api/admin/format/rules')
}

/**
 * 更新规则状态
 */
export function updateRuleStatus(id, status) {
  return request.put(`/api/admin/format/rules/${id}`, { status })
}

/**
 * 检查链接
 */
export function checkLinks(content) {
  return request.post('/api/admin/format/check-links', { content })
}
```

---

#### 任务 3.7：前端 - 创建排版面板组件
**工时**: 1.5天
**优先级**: P0

**文件变更**:
```
blog-web/src/components/writing/FormatPanel.vue (新建)
blog-web/src/components/writing/FormatPreview.vue (新建)
blog-web/src/components/writing/LinkCheckResult.vue (新建)
```

**FormatPanel.vue**:
```vue
<template>
  <van-popup
    v-model:show="visible"
    position="bottom"
    :style="{ height: '70%' }"
    round
  >
    <div class="format-panel">
      <!-- 头部 -->
      <div class="panel-header">
        <h3>智能排版设置</h3>
        <van-icon name="cross" @click="close" />
      </div>

      <!-- 快捷操作 -->
      <div class="quick-actions">
        <van-button size="small" @click="handleQuickFormat">⚡ 一键排版</van-button>
        <van-button size="small" @click="handleClearEmptyLines">🗑️ 清理空行</van-button>
        <van-button size="small" @click="handleFormatCode">📝 格式化代码</van-button>
        <van-button size="small" @click="handleCheckLinks">🔗 检查链接</van-button>
      </div>

      <!-- 规则列表 -->
      <div class="rules-section">
        <h4>排版规则</h4>
        <van-loading v-if="loading" size="24px" />
        
        <div v-else class="rules-list">
          <van-cell-group>
            <van-cell v-for="rule in rules" :key="rule.id">
              <template #title>
                <div class="rule-title">
                  <van-checkbox 
                    v-model="selectedRules[rule.ruleKey]"
                    :disabled="rule.ruleType === 'ai'"
                  >
                    {{ rule.ruleName }}
                  </van-checkbox>
                </div>
              </template>
              <template #label>
                <div class="rule-desc">{{ rule.description }}</div>
              </template>
              <template #right-icon>
                <van-tag v-if="rule.ruleType === 'ai'" type="warning" size="small">AI</van-tag>
              </template>
            </van-cell>
          </van-cell-group>
        </div>
      </div>

      <!-- 底部操作 -->
      <div class="panel-footer">
        <van-button @click="resetRules">重置</van-button>
        <van-button type="primary" @click="handlePreview">预览变更</van-button>
      </div>
    </div>

    <!-- 预览对话框 -->
    <FormatPreview
      v-model:show="showPreview"
      :preview-result="previewResult"
      @confirm="handleApply"
    />

    <!-- 链接检查结果 -->
    <LinkCheckResult
      v-model:show="showLinkCheck"
      :result="linkCheckResult"
    />
  </van-popup>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { showToast } from 'vant'
import { getFormatRules, previewFormat, applyFormat, checkLinks } from '@/api/format'
import FormatPreview from './FormatPreview.vue'
import LinkCheckResult from './LinkCheckResult.vue'

const props = defineProps({
  show: Boolean,
  content: String
})

const emit = defineEmits(['update:show', 'apply'])

const visible = computed({
  get: () => props.show,
  set: (val) => emit('update:show', val)
})

const loading = ref(false)
const rules = ref([])
const selectedRules = ref({})
const showPreview = ref(false)
const previewResult = ref(null)
const showLinkCheck = ref(false)
const linkCheckResult = ref(null)

// 加载规则
const loadRules = async () => {
  loading.value = true
  try {
    const { data } = await getFormatRules()
    rules.value = data
    
    // 初始化选中状态
    data.forEach(rule => {
      selectedRules.value[rule.ruleKey] = rule.isDefault === 1
    })
  } finally {
    loading.value = false
  }
}

// 获取选中的规则
const getSelectedRuleKeys = () => {
  return Object.entries(selectedRules.value)
    .filter(([, selected]) => selected)
    .map(([key]) => key)
}

// 快捷操作 - 一键排版
const handleQuickFormat = async () => {
  const selectedKeys = getSelectedRuleKeys()
  if (selectedKeys.length === 0) {
    showToast('请选择至少一个规则')
    return
  }
  
  showToast('正在排版...')
  const { data } = await applyFormat(props.content, selectedKeys)
  emit('apply', data)
  showToast('排版完成')
  close()
}

// 清理空行
const handleClearEmptyLines = async () => {
  const { data } = await applyFormat(props.content, ['empty_line_cleanup'])
  emit('apply', data)
  showToast('空行已清理')
}

// 格式化代码
const handleFormatCode = async () => {
  const { data } = await applyFormat(props.content, ['code_block_format'])
  emit('apply', data)
  showToast('代码已格式化')
}

// 检查链接
const handleCheckLinks = async () => {
  showToast('正在检查链接...')
  const { data } = await checkLinks(props.content)
  linkCheckResult.value = data
  showLinkCheck.value = true
}

// 预览变更
const handlePreview = async () => {
  const selectedKeys = getSelectedRuleKeys()
  if (selectedKeys.length === 0) {
    showToast('请选择至少一个规则')
    return
  }
  
  const { data } = await previewFormat(props.content, selectedKeys)
  previewResult.value = data
  showPreview.value = true
}

// 应用变更
const handleApply = async () => {
  const selectedKeys = getSelectedRuleKeys()
  const { data } = await applyFormat(props.content, selectedKeys)
  emit('apply', data)
  showPreview.value = false
  close()
}

// 重置规则
const resetRules = () => {
  rules.value.forEach(rule => {
    selectedRules.value[rule.ruleKey] = rule.isDefault === 1
  })
}

const close = () => {
  visible.value = false
}

onMounted(loadRules)

watch(() => props.show, (val) => {
  if (val) {
    loadRules()
  }
})
</script>

<style scoped>
.format-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #eee;
}

.panel-header h3 {
  margin: 0;
}

.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 16px;
  border-bottom: 1px solid #eee;
}

.rules-section {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.rules-section h4 {
  margin: 0 0 12px;
}

.rule-title {
  display: flex;
  align-items: center;
}

.rule-desc {
  font-size: 12px;
  color: #666;
  margin-top: 4px;
}

.panel-footer {
  display: flex;
  gap: 12px;
  padding: 16px;
  border-top: 1px solid #eee;
}

.panel-footer .van-button {
  flex: 1;
}
</style>
```

**FormatPreview.vue** 和 **LinkCheckResult.vue** 参照设计文档中的交互设计实现。

---

#### 任务 3.8：前端 - 集成排版功能到ArticleEdit
**工时**: 0.5天
**优先级**: P0

**文件变更**:
```
blog-web/src/views/admin/ArticleEdit.vue
```

**主要修改**:
```vue
<!-- 在工具栏添加排版按钮 -->
<van-dropdown-menu>
  <!-- AI助手 -->
  <van-dropdown-item ref="aiDropdown">...</van-dropdown-item>
  
  <!-- 模板 -->
  <van-dropdown-item ref="templateDropdown">...</van-dropdown-item>
  
  <!-- 排版 -->
  <van-dropdown-item ref="formatDropdown">
    <template #title>
      <van-button size="small">📏 排版</van-button>
    </template>
    <van-cell-group>
      <van-cell title="一键排版" icon="font" @click="quickFormat" />
      <van-cell title="排版设置" icon="setting-o" @click="showFormatPanel" />
      <van-cell title="检查链接" icon="link-o" @click="quickCheckLinks" />
    </van-cell-group>
  </van-dropdown-item>
</van-dropdown-menu>

<!-- 排版面板 -->
<FormatPanel
  v-model:show="showFormatPanelPopup"
  :content="form.content"
  @apply="handleFormatApply"
/>
```

---

### 4.2 阶段三文件清单

| 文件 | 操作 | 说明 |
|------|------|------|
| `blog-server/src/main/resources/db/schema.sql` | 修改 | 新增排版规则表 |
| `blog-server/src/main/java/com/blog/domain/entity/FormatRule.java` | 新建 | 规则实体类 |
| `blog-server/src/main/java/com/blog/repository/FormatRuleMapper.java` | 新建 | 数据访问层 |
| `blog-server/src/main/java/com/blog/service/FormatService.java` | 新建 | 服务接口 |
| `blog-server/src/main/java/com/blog/service/impl/FormatServiceImpl.java` | 新建 | 服务实现 |
| `blog-server/src/main/java/com/blog/controller/admin/FormatController.java` | 新建 | API控制器 |
| `blog-server/src/main/java/com/blog/domain/dto/FormatPreviewResult.java` | 新建 | 预览结果DTO |
| `blog-server/src/main/java/com/blog/domain/dto/LinkCheckResult.java` | 新建 | 链接检查DTO |
| `blog-web/src/api/format.js` | 新建 | 前端API |
| `blog-web/src/components/writing/FormatPanel.vue` | 新建 | 排版面板组件 |
| `blog-web/src/components/writing/FormatPreview.vue` | 新建 | 预览对话框组件 |
| `blog-web/src/components/writing/LinkCheckResult.vue` | 新建 | 链接检查结果组件 |
| `blog-web/src/views/admin/ArticleEdit.vue` | 修改 | 集成排版功能 |

---

## 五、实施注意事项

### 5.1 技术风险与应对

| 风险 | 影响 | 应对措施 |
|------|------|----------|
| AI响应延迟 | 用户体验差 | 流式响应 + 加载提示 + 超时处理(30秒) |
| 排版规则冲突 | 结果不符合预期 | 规则优先级 + 预览机制 |
| 模板JSON解析失败 | 功能异常 | try-catch + 默认值处理 |
| 链接检查超时 | 等待时间过长 | 异步检查 + 5秒超时 |

### 5.2 安全考虑

| 安全点 | 措施 |
|--------|------|
| AI内容安全 | 复用现有XML标签隔离机制 |
| 模板XSS防护 | 输出时HTML编码 |
| API权限控制 | 所有接口需ADMIN权限 |
| 输入长度限制 | 内容最大100KB |

### 5.3 测试要点

| 测试类型 | 测试内容 |
|----------|----------|
| 单元测试 | 服务层方法测试 |
| 集成测试 | API接口测试 |
| E2E测试 | 用户操作流程测试 |
| 性能测试 | AI响应时间、排版处理时间 |

---

## 六、验收标准

### 6.1 功能验收

- [ ] AI续写、扩写、改写、润色、纠错功能正常
- [ ] 流式输出正确显示，可正常中断
- [ ] 模板创建、选择、应用功能正常
- [ ] 内置模板数据正确
- [ ] 排版预览、应用功能正常
- [ ] 链接检查功能正常
- [ ] 所有功能集成到文章编辑页

### 6.2 性能验收

- [ ] AI响应首字节时间 < 3秒
- [ ] 排版预览响应时间 < 1秒
- [ ] 链接检查单个链接超时 < 5秒

### 6.3 兼容性验收

- [ ] Chrome/Firefox/Safari 最新版正常
- [ ] 移动端浏览器正常显示

---

## 七、后续优化方向

1. **AI能力扩展**
   - 支持更多AI模型选择
   - AI翻译功能
   - AI生成配图建议

2. **模板系统增强**
   - 模板分享功能
   - 模板评分系统
   - 社区模板市场

3. **排版工具增强**
   - 更多排版规则
   - 自定义规则
   - 排版模板保存

---

*文档版本: v1.0*
*最后更新: 2026-04-30*
