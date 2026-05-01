---
title: StringTemplate Parsing Error with JSON in Spring AI Prompt Templates
date: 2026-05-01
category: runtime-errors
module: ai
problem_type: runtime_error
component: service_object
severity: medium
tags:
  - spring-ai
  - stringtemplate
  - prompt-template
  - json-escaping
  - template-rendering
symptoms:
  - Spring AI's StTemplateRenderer fails with parsing error when prompt template contains JSON examples with curly braces
  - Error message: "came as a complete surprise to me" at template rendering time
  - AI features dependent on the prompt template are non-functional
root_cause: config_error
resolution_type: config_change
---

# StringTemplate Parsing Error with JSON in Spring AI Prompt Templates

## Problem
StringTemplate (ST4) parsing error occurred when Spring AI processed a prompt template containing JSON examples with curly braces, which were incorrectly interpreted as ST4 template expressions.

## Symptoms
- Error message: `o.s.ai.template.st.StTemplateRenderer: 8:18: '"推荐的写作角度（一句话概括）"' came as a complete surprise to me`
- Prompt template rendering failed when Spring AI attempted to process templates containing JSON examples
- AI features dependent on the prompt template were non-functional

## What Didn't Work
- Keeping JSON format examples directly in the user template with curly braces

## Solution

**1. Refactored template to separate JSON format specification from user template:**

```sql
-- Original (Broken)
user_template: '<user_content>
话题标题：{title}
话题描述：{description}
</user_content>

请以JSON格式返回以下字段：
{
  "writingAngle": "推荐的写作角度（一句话概括）",
  "targetAudience": "目标受众描述",
  "difficulty": "BEGINNER 或 INTERMEDIATE 或 ADVANCED",
  ...
}'

-- Fixed
system_prompt: '你是内容策划专家。请分析以下话题的创作价值。请以JSON格式返回分析结果，不要包含其他内容。返回格式：{"writingAngle":"推荐的写作角度","targetAudience":"目标受众描述",...}'

user_template: '<user_content>
话题标题：{title}
话题描述：{description}
</user_content>

请按JSON格式返回分析结果。'
```

**2. Cleared cached prompt from Redis:**
```bash
docker exec blog-redis redis-cli -a redis123456 DEL "ai:prompt"
```

**3. Added markdown code block stripping in Java code:**
```java
String json = topic.getAnalysis().trim();
// 去掉 markdown 代码块包裹
if (json.startsWith("```json")) {
    json = json.substring(7);
} else if (json.startsWith("```")) {
    json = json.substring(3);
}
if (json.endsWith("```")) {
    json = json.substring(0, json.length() - 3);
}
json = json.trim();
vo.setAiAnalysisResult(objectMapper.readValue(json, Object.class));
```

## Why This Works
Spring AI uses StringTemplate (ST4) as its template engine, which interprets `{...}` as template expressions. When JSON examples containing curly braces were placed directly in the template, ST4 attempted to parse them as expressions and failed. By moving the JSON format specification to the system prompt (which doesn't undergo ST4 variable substitution with user input) and using a compact single-line JSON example without spaces, the curly braces are no longer misinterpreted as template syntax.

## Prevention
1. **Avoid curly braces in prompt templates**: When using Spring AI with ST4, keep JSON format examples out of user templates that contain `{variable}` placeholders
2. **Use system prompts for format specification**: Place structural examples and format requirements in the system prompt rather than user templates
3. **Use compact JSON examples**: If JSON must be included, use single-line compact format to reduce parsing ambiguity
4. **Clear template cache after changes**: Remember to clear Redis cache when updating prompt templates in the database
5. **Handle AI response variations**: Always strip potential markdown code block wrappers from AI responses before JSON parsing

## Related Files
- `blog-server/src/main/java/com/blog/service/ai/PromptTemplateService.java`
- `blog-server/src/main/java/com/blog/service/impl/TopicServiceImpl.java`
- `prompt_template` database table
