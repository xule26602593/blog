# 创作工具增强系统设计文档

> 创建日期: 2026-04-30
> 状态: 已确认
> 实施计划: [创作工具增强系统实施计划](../plans/2026-04-30-creation-tools-enhancement.md)

## 一、概述

### 1.1 功能定位

为博客系统的创作者提供强大的写作辅助工具，包括 AI 写作助手增强、写作模板系统和智能排版工具三大模块，提升内容创作效率和质量。

### 1.2 核心价值

| 用户痛点 | 解决方案 |
|----------|----------|
| 写作卡顿，不知道如何继续 | AI续写、扩写功能 |
| 表达不够专业流畅 | AI润色、改写功能 |
| 文章结构混乱 | 写作模板提供规范框架 |
| 排版格式不统一 | 一键智能排版 |
| 错别字、语法错误 | AI纠错检测 |

### 1.3 技术栈

| 层级 | 技术选型 |
|------|----------|
| 后端 | Spring Boot 3.5.14 + Spring AI 1.1.5 + MyBatis Plus |
| 前端 | Vue 3.5 + Vant 4 + Pinia |
| 数据库 | MySQL 8.0 |
| 缓存 | Redis |
| AI | 复用现有 Spring AI 配置 |

---

## 二、功能模块设计

### 2.1 AI写作助手增强

#### 2.1.1 功能清单

| 功能 | 描述 | 输入 | 输出 | 流式 |
|------|------|------|------|------|
| AI续写 | 根据上下文智能续写 | 选中文本/光标前内容 | 续写内容 | ✓ |
| AI扩写 | 将简短内容扩展为详细段落 | 选中文本 | 扩写内容 | ✓ |
| AI改写 | 换一种表达方式重写 | 选中文本 + 风格 | 改写内容 | ✓ |
| AI润色 | 优化语言表达 | 选中文本 + 风格 | 润色后内容 | ✓ |
| AI摘要 | 一键生成文章摘要 | 全文内容 | 摘要文本 | ✗ |
| AI标题 | 智能生成候选标题 | 全文内容 | 标题列表(3-5个) | ✗ |
| AI大纲 | 根据主题生成大纲 | 标题 + 主题描述 | 大纲结构 | ✓ |
| AI纠错 | 语法错误、错别字检测 | 选中文本/全文 | 错误列表 + 修改建议 | ✗ |

#### 2.1.2 功能详细设计

**AI续写**

```
输入参数：
- context: String - 上下文内容（光标前500字或选中文本）
- direction: String - 续写方向
  - continue: 继续当前思路
  - expand: 展开详细说明
  - example: 添加示例/代码
  - summarize: 总结归纳

输出：
- 流式文本内容
```

**AI扩写**

```
输入参数：
- text: String - 待扩写的文本
- expandType: String - 扩写类型
  - detail: 增加细节描述
  - reason: 添加原因分析
  - example: 添加实例说明
  - comparison: 添加对比分析

输出：
- 流式扩写内容
```

**AI改写**

```
输入参数：
- text: String - 待改写的文本
- style: String - 改写风格
  - formal: 正式专业
  - casual: 轻松通俗
  - academic: 学术严谨
  - concise: 精简干练

输出：
- 流式改写内容
```

**AI润色**

```
输入参数：
- text: String - 待润色的文本
- style: String - 润色风格
  - formal: 正式风格
  - casual: 轻松风格
  - tech: 技术文档风格

输出：
- 流式润色后内容
```

**AI纠错**

```
输入参数：
- text: String - 待检测的文本

输出格式：
{
  "success": true,
  "data": {
    "errorCount": 3,
    "errors": [
      {
        "type": "spelling",  // spelling: 错别字, grammar: 语法, punctuation: 标点
        "original": "原始文本",
        "suggestion": "修改建议",
        "reason": "错误原因",
        "position": {
          "start": 10,
          "end": 15
        }
      }
    ]
  }
}
```

#### 2.1.3 交互设计

**工具栏按钮**

```
文章编辑页工具栏：
┌─────────────────────────────────────────────────────┐
│ [保存] [发布] [预览] | [✨AI助手 ▼] [📄模板 ▼] [📏排版 ▼] │
└─────────────────────────────────────────────────────┘
```

**右键菜单**

```
选中文本后右键：
┌─────────────────────┐
│ ✨ AI续写           │
│ 📝 AI扩写           │
│ 🔄 AI改写           │
│ 💅 AI润色           │
├─────────────────────┤
│ ✅ AI纠错           │
├─────────────────────┤
│ 复制                │
│ 剪切                │
│ 粘贴                │
└─────────────────────┘
```

**侧边面板（续写/扩写/改写/润色场景）**

```
┌─────────────────────────────────────┐
│ AI写作助手                    [×]   │
├─────────────────────────────────────┤
│ 📝 续写设置：                       │
│                                     │
│ 续写方向：                          │
│ ○ 继续当前思路                      │
│ ● 展开详细说明                      │
│ ○ 添加示例代码                      │
│ ○ 总结归纳                          │
├─────────────────────────────────────┤
│ 生成结果：                          │
│ ┌─────────────────────────────────┐ │
│ │ 这将使应用程序更加模块化，便于    │ │
│ │ 后期维护和扩展。具体来说...█     │ │
│ │                                 │ │
│ └─────────────────────────────────┘ │
├─────────────────────────────────────┤
│ [重新生成]        [应用到编辑器]     │
└─────────────────────────────────────┘
```

**纠错结果展示**

```
┌─────────────────────────────────────┐
│ AI纠错结果                     [×]  │
├─────────────────────────────────────┤
│ 发现 3 处问题：                     │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ ❌ 错别字 (第10行)              │ │
│ │ 原文: "这里有一个重要的提意"     │ │
│ │ 建议: "这里有一个重要的提议"     │ │
│ │ [采纳] [忽略]                   │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ ⚠️ 语法问题 (第25行)            │ │
│ │ 原文: "他门的方案很优秀"         │ │
│ │ 建议: "他们的方案很优秀"         │ │
│ │ [采纳] [忽略]                   │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ 💡 标点建议 (第38行)            │ │
│ │ 原文: "主要包括A,B,C"           │ │
│ │ 建议: "主要包括A、B、C"          │ │
│ │ [采纳] [忽略]                   │ │
│ └─────────────────────────────────┘ │
├─────────────────────────────────────┤
│              [全部采纳]  [关闭]      │
└─────────────────────────────────────┘
```

---

### 2.2 写作模板系统

#### 2.2.1 模板分类

| 分类 | 图标 | 说明 | 适用场景 |
|------|------|------|----------|
| 技术教程 | 📚 | 步骤式教程写作 | 入门指南、实战教程 |
| 技术原理 | 🔬 | 深度解析技术原理 | 架构设计、源码分析 |
| 项目实战 | 🚀 | 完整项目开发流程 | 全栈项目、微服务搭建 |
| 经验分享 | 💡 | 开发经验、踩坑记录 | Bug修复、性能优化 |
| 读书笔记 | 📖 | 书籍学习笔记 | 技术书籍读后感 |
| 随笔日志 | ✨ | 个人感悟、生活记录 | 自由写作 |

#### 2.2.2 数据结构

**模板表 `writing_template`**

```sql
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
```

**章节结构JSON格式**

```json
{
  "sections": [
    {
      "title": "前言",
      "level": 2,
      "placeholder": "介绍本文要解决的问题和目标读者...",
      "tips": "说明学习前提、技术版本",
      "required": false,
      "content": ""
    },
    {
      "title": "环境准备",
      "level": 2,
      "placeholder": "列出所需工具和依赖版本...",
      "tips": "使用代码块展示配置",
      "required": true,
      "content": "",
      "children": [
        {
          "title": "开发环境",
          "level": 3,
          "placeholder": "JDK版本、IDE等...",
          "tips": "",
          "required": false,
          "content": ""
        },
        {
          "title": "依赖配置",
          "level": 3,
          "placeholder": "Maven/Gradle配置...",
          "tips": "使用代码块",
          "required": false,
          "content": ""
        }
      ]
    },
    {
      "title": "核心实现",
      "level": 2,
      "placeholder": "分步骤讲解实现过程...",
      "tips": "每步包含目标、操作、预期结果",
      "required": true,
      "content": "",
      "children": []
    },
    {
      "title": "常见问题",
      "level": 2,
      "placeholder": "记录可能遇到的问题和解决方案...",
      "tips": "Q&A格式",
      "required": false,
      "content": ""
    },
    {
      "title": "总结",
      "level": 2,
      "placeholder": "回顾要点，展望延伸...",
      "tips": "简洁有力",
      "required": true,
      "content": ""
    }
  ]
}
```

#### 2.2.3 内置模板数据

```sql
-- 技术教程模板
INSERT INTO `writing_template` (`name`, `category`, `icon`, `description`, `sections`, `is_builtin`, `is_public`) VALUES
('Spring Boot 入门教程', 'tutorial', '📚', '适合Spring Boot入门教程类文章', 
 '[{"title":"前言","level":2,"placeholder":"介绍本文要解决的问题，目标读者需要具备的基础知识...","tips":"说明学习前提、Spring Boot版本"},{"title":"环境准备","level":2,"placeholder":"列出所需工具和依赖版本...","tips":"使用代码块展示pom.xml配置","required":true,"children":[{"title":"开发环境","level":3,"placeholder":"JDK版本、IDE、Maven版本..."},{"title":"项目创建","level":3,"placeholder":"创建Spring Boot项目步骤..."}]},{"title":"核心实现","level":2,"placeholder":"分步骤讲解实现过程...","tips":"每步包含目标、代码、说明","required":true},{"title":"运行测试","level":2,"placeholder":"启动项目并验证功能...","tips":"截图展示运行结果"},{"title":"常见问题","level":2,"placeholder":"记录可能遇到的问题...","tips":"Q&A格式"},{"title":"总结","level":2,"placeholder":"回顾要点，推荐延伸阅读...","tips":"简洁有力","required":true}]',
 1, 1),

('Vue3 实战教程', 'tutorial', '📚', '适合Vue3实战类文章',
 '[{"title":"前言","level":2,"placeholder":"介绍项目背景和要实现的功能..."},{"title":"技术选型","level":2,"placeholder":"说明技术栈选择和原因...","children":[{"title":"前端框架","level":3,"placeholder":"Vue3 + Vite + Pinia..."},{"title":"UI组件库","level":3,"placeholder":"Element Plus / Ant Design Vue..."}]},{"title":"项目搭建","level":2,"placeholder":"从零开始搭建项目...","required":true},{"title":"核心功能","level":2,"placeholder":"实现主要功能的步骤...","required":true},{"title":"优化部署","level":2,"placeholder":"性能优化和部署方案..."},{"title":"总结","level":2,"placeholder":"项目收获和改进方向...","required":true}]',
 1, 1),

-- 技术原理模板
('架构设计解析', 'principle', '🔬', '适合架构设计和原理分析类文章',
 '[{"title":"背景","level":2,"placeholder":"问题背景，为什么需要这个设计..."},{"title":"整体架构","level":2,"placeholder":"系统整体架构图和说明...","tips":"使用架构图+文字说明","required":true},{"title":"核心模块","level":2,"placeholder":"关键模块的设计思路...","required":true,"children":[]},{"title":"技术选型","level":2,"placeholder":"关键技术选型的考量..."},{"title":"性能考量","level":2,"placeholder":"性能优化策略..."},{"title":"总结","level":2,"placeholder":"设计亮点和改进空间...","required":true}]',
 1, 1),

-- 经验分享模板
('踩坑记录', 'experience', '💡', '适合记录开发中遇到的问题和解决方案',
 '[{"title":"问题背景","level":2,"placeholder":"什么场景下遇到的问题...","required":true},{"title":"问题现象","level":2,"placeholder":"描述问题的具体表现...","required":true},{"title":"排查过程","level":2,"placeholder":"如何一步步定位问题...","required":true},{"title":"解决方案","level":2,"placeholder":"最终如何解决...","required":true},{"title":"经验总结","level":2,"placeholder":"学到了什么，如何避免...","required":true}]',
 1, 1),

-- 读书笔记模板
('技术书籍笔记', 'note', '📖', '适合技术书籍的阅读笔记',
 '[{"title":"书籍信息","level":2,"placeholder":"书名、作者、阅读时间..."},{"title":"核心内容","level":2,"placeholder":"书中的核心知识点...","required":true,"children":[]},{"title":"精彩摘录","level":2,"placeholder":"印象深刻的内容摘录..."},{"title":"实践应用","level":2,"placeholder":"如何将知识应用到实际..."},{"title":"推荐理由","level":2,"placeholder":"为什么推荐这本书..."}]',
 1, 1);
```

#### 2.2.4 交互设计

**模板选择入口**

```
文章编辑页：
┌─────────────────────────────────────────────────────┐
│ [保存] [发布] [预览] | [✨AI助手 ▼] [📄模板 ▼] [📏排版 ▼] │
└─────────────────────────────────────────────────────┘

点击 [模板 ▼]：
┌─────────────────────┐
│ 📄 从模板创建       │
│ 💾 保存为模板       │
│ 📚 我的模板         │
└─────────────────────┘
```

**模板选择弹窗**

```
┌─────────────────────────────────────────────────────┐
│ 选择写作模板                                  [×]   │
├─────────────────────────────────────────────────────┤
│ 🔍 搜索模板...                                      │
├─────────────────────────────────────────────────────┤
│ 📚 技术教程 (2)                                     │
│ ┌─────────────────┐ ┌─────────────────┐            │
│ │ 📚 Spring Boot  │ │ 📚 Vue3实战     │            │
│ │ 入门教程        │ │ 教程            │            │
│ │ 使用 128 次     │ │ 使用 86 次      │            │
│ └─────────────────┘ └─────────────────┘            │
│                                                     │
│ 🔬 技术原理 (1)                                     │
│ ┌─────────────────┐                                │
│ │ 🔬 架构设计解析  │                                │
│ │ 使用 45 次       │                                │
│ └─────────────────┘                                │
│                                                     │
│ 💡 经验分享 (1)                                     │
│ ┌─────────────────┐                                │
│ │ 💡 踩坑记录     │                                │
│ │ 使用 67 次       │                                │
│ └─────────────────┘                                │
│                                                     │
│ ☐ 使用空白模板（自由写作）                          │
├─────────────────────────────────────────────────────┤
│                                   [取消]  [使用模板] │
└─────────────────────────────────────────────────────┘
```

**模板应用后编辑器**

```
┌─────────────────────────────────────────────────────┐
│ # （在此输入文章标题）                               │
│                                                     │
│ ## 前言                                             │
│ > 💡 提示：介绍本文要解决的问题，目标读者需要具备的基│
│ > 础知识...                                         │
│                                                     │
│                                                     │
│ ## 环境准备                                         │
│ > 💡 提示：列出所需工具和依赖版本...                 │
│ > 使用代码块展示pom.xml配置                         │
│                                                     │
│                                                     │
│ ### 开发环境                                        │
│ > 💡 提示：JDK版本、IDE、Maven版本...               │
│                                                     │
│                                                     │
│ ### 项目创建                                        │
│ > 💡 提示：创建Spring Boot项目步骤...               │
│                                                     │
│                                                     │
│ ## 核心实现                                         │
│ > 💡 提示：分步骤讲解实现过程...                     │
│ > 每步包含目标、代码、说明                          │
│                                                     │
│                                                     │
│ ...（其他章节）                                     │
└─────────────────────────────────────────────────────┘

提示信息：
- 💡 提示内容显示为灰色引用块
- 用户在提示下方输入实际内容
- 可随时删除不需要的章节
```

---

### 2.3 智能排版工具

#### 2.3.1 排版规则

| 规则类型 | 说明 | 实现方式 |
|----------|------|----------|
| 标题规范化 | 确保标题层级连续（H1→H2→H3） | 正则检测 + 规则修正 |
| 段落合并 | 合并过短段落（<50字） | AI辅助合并 |
| 段落拆分 | 拆分过长段落（>500字） | AI辅助拆分 |
| 空行清理 | 删除多余空行（>2个连续空行） | 正则替换 |
| 代码格式化 | 添加语言标识、统一缩进 | highlight.js + 正则 |
| 列表规范化 | 统一列表格式（- 或 1.） | 正则替换 |
| 中英文间距 | 中文与英文/数字间添加空格 | 正则替换 |
| 标点规范 | 中文标点规范化 | 正则替换 |
| 链接检查 | 检测无效链接 | 异步HTTP请求 |

#### 2.3.2 数据结构

**排版规则配置表 `format_rule`**

```sql
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
```

**规则配置JSON示例**

```json
// 正则规则示例
{
  "ruleKey": "empty_line_cleanup",
  "ruleType": "regex",
  "config": {
    "pattern": "\\n{3,}",
    "replacement": "\\n\\n",
    "description": "将3个及以上连续换行替换为2个"
  }
}

// AI规则示例
{
  "ruleKey": "paragraph_merge",
  "ruleType": "ai",
  "config": {
    "minLength": 50,
    "prompt": "将以下过短的段落合并，保持语义连贯：\n{paragraphs}",
    "maxTokens": 500
  }
}

// HTTP规则示例
{
  "ruleKey": "link_check",
  "ruleType": "http",
  "config": {
    "timeout": 5000,
    "userAgent": "Mozilla/5.0 Blog System Link Checker",
    "allowedStatusCodes": [200, 301, 302]
  }
}
```

#### 2.3.3 初始化规则数据

```sql
INSERT INTO `format_rule` (`rule_key`, `rule_name`, `description`, `rule_type`, `rule_config`, `priority`, `is_default`) VALUES
('heading_normalize', '标题层级规范化', '确保标题层级连续，避免跳级', 'regex',
 '{"patterns":[{"pattern":"^#{1}\\s","replaceWith":"## "},{"pattern":"^#{4,}\\s","replaceWith":"### "}]}', 
 1, 1),

('empty_line_cleanup', '空行清理', '删除多余空行', 'regex',
 '{"pattern":"\\\\n{3,}","replacement":"\\\\n\\\\n"}', 
 10, 1),

('chinese_english_spacing', '中英文间距', '中文与英文/数字间添加空格', 'regex',
 '{"patterns":[{"pattern":"([\\u4e00-\\u9fa5])([a-zA-Z0-9])","replacement":"$1 $2"},{"pattern":"([a-zA-Z0-9])([\\u4e00-\\u9fa5])","replacement":"$1 $2"}]}',
 5, 1),

('punctuation_normalize', '标点规范化', '规范化中文标点', 'regex',
 '{"patterns":[{"pattern":",","replacement":"，"},{"pattern":".","replacement":"。"},{"pattern":"!","replacement":"！"},{"pattern":"?","replacement":"？"}],"skipInCode":true}',
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

#### 2.3.4 交互设计

**排版工具入口**

```
文章编辑页：
┌─────────────────────────────────────────────────────┐
│ [保存] [发布] [预览] | [✨AI助手 ▼] [📄模板 ▼] [📏排版 ▼] │
└─────────────────────────────────────────────────────┘

点击 [排版 ▼]：
┌─────────────────────┐
│ ⚡ 一键排版         │
│ ⚙️ 排版设置        │
│ 🔍 检查链接         │
└─────────────────────┘
```

**排版设置面板**

```
┌─────────────────────────────────────┐
│ 智能排版设置                   [×] │
├─────────────────────────────────────┤
│ 快捷操作：                          │
│ ┌─────────────────────────────────┐ │
│ │ [⚡ 一键排版] [🗑️ 清理空行]      │ │
│ │ [📝 格式化代码] [🔗 检查链接]    │ │
│ └─────────────────────────────────┘ │
├─────────────────────────────────────┤
│ 排版规则：                          │
│                                     │
│ ☑️ 标题层级规范化                   │
│    确保标题层级连续，避免跳级        │
│                                     │
│ ☑️ 空行清理                         │
│    删除多余空行（超过2个）           │
│                                     │
│ ☑️ 中英文间距                       │
│    中文与英文/数字间添加空格         │
│                                     │
│ ☑️ 标点规范化                       │
│    规范化中文标点（不在代码中执行）  │
│                                     │
│ ☑️ 代码块格式化                     │
│    为代码块添加语言标识              │
│                                     │
│ ☐ 段落合并（AI）                    │
│    合并过短段落（<50字）             │
│                                     │
│ ☐ 段落拆分（AI）                    │
│    拆分过长段落（>500字）            │
│                                     │
│ ☑️ 链接检查                         │
│    检测无效链接                      │
├─────────────────────────────────────┤
│              [重置]  [应用排版]      │
└─────────────────────────────────────┘
```

**排版预览确认**

```
┌─────────────────────────────────────┐
│ 排版变更预览                   [×] │
├─────────────────────────────────────┤
│ 将执行以下变更：                    │
│                                     │
│ 📝 标题层级规范化：                 │
│   - 第15行: # → ##                  │
│   - 第28行: #### → ###              │
│                                     │
│ 🗑️ 空行清理：                       │
│   - 删除第5-7行多余空行             │
│   - 删除第32-34行多余空行           │
│                                     │
│ 📝 中英文间距：                     │
│   - 第10行: 添加3处空格             │
│   - 第25行: 添加2处空格             │
│                                     │
│ 🔗 链接检查：                       │
│   - 发现1个无效链接（第40行）       │
│   - URL: https://example.com/broken │
│                                     │
│ 共影响 5 处变更                      │
├─────────────────────────────────────┤
│              [取消]  [确认应用]      │
└─────────────────────────────────────┘
```

**链接检查结果**

```
┌─────────────────────────────────────┐
│ 链接检查结果                   [×] │
├─────────────────────────────────────┤
│ ✅ 有效链接：5 个                   │
│ ❌ 无效链接：1 个                   │
│                                     │
│ 无效链接详情：                      │
│ ┌─────────────────────────────────┐ │
│ │ 第40行                          │ │
│ │ URL: https://example.com/broken │ │
│ │ 状态: 404 Not Found             │ │
│ │                                 │ │
│ │ [删除链接] [编辑链接] [忽略]    │ │
│ └─────────────────────────────────┘ │
├─────────────────────────────────────┤
│                          [关闭]     │
└─────────────────────────────────────┘
```

---

## 三、数据库设计

### 3.1 新增表

```sql
-- 写作模板表（见 2.2.2）
CREATE TABLE `writing_template` (...);

-- 排版规则配置表（见 2.3.2）
CREATE TABLE `format_rule` (...);
```

### 3.2 扩展现有表

**prompt_template 表新增模板**

```sql
-- AI续写模板
INSERT INTO `prompt_template` (`template_key`, `template_name`, `category`, `system_prompt`, `user_template`, `variables`, `is_default`) VALUES
('writing_continue', 'AI续写', 'writing',
 '你是一个专业的技术写作助手。根据上下文内容，自然地续写文章。要求：1.保持原有风格和语调 2.逻辑连贯 3.内容有价值 4.只返回续写内容',
 '<user_content>\n上下文：\n{context}\n</user_content>\n\n请{direction}：',
 '{"variables": ["context", "direction"]}', 1),

-- AI扩写模板
('writing_expand', 'AI扩写', 'writing',
 '你是一个专业的技术写作助手。将简短内容扩展为详细段落。要求：1.增加必要的细节和说明 2.保持逻辑清晰 3.语言流畅 4.只返回扩写内容',
 '<user_content>\n原文：\n{text}\n</user_content>\n\n请{expandType}：',
 '{"variables": ["text", "expandType"]}', 1),

-- AI改写模板
('writing_rewrite', 'AI改写', 'writing',
 '你是一个专业的技术写作助手。用不同的表达方式重写内容。要求：1.保持原意不变 2.改变表达方式 3.语言流畅 4.只返回改写内容',
 '<user_content>\n原文：\n{text}\n</user_content>\n\n请用{style}风格改写：',
 '{"variables": ["text", "style"]}', 1),

-- AI纠错模板
('writing_proofread', 'AI纠错', 'writing',
 '你是一个专业的文字校对专家。检查文本中的错别字、语法错误和标点问题。返回JSON格式结果：{"errors":[{"type":"spelling/grammar/punctuation","original":"原文","suggestion":"建议","reason":"原因","position":{"start":0,"end":0}}]}',
 '<user_content>\n待检查文本：\n{text}\n</user_content>',
 '{"variables": ["text"]}', 1);
```

---

## 四、后端API设计

### 4.1 AI写作助手API

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | /api/admin/ai/writing/continue | AI续写（流式） | ADMIN |
| POST | /api/admin/ai/writing/expand | AI扩写（流式） | ADMIN |
| POST | /api/admin/ai/writing/rewrite | AI改写（流式） | ADMIN |
| POST | /api/admin/ai/writing/polish | AI润色（流式） | ADMIN |
| POST | /api/admin/ai/writing/proofread | AI纠错 | ADMIN |
| GET | /api/admin/ai/writing/summary | 生成摘要 | ADMIN |
| GET | /api/admin/ai/writing/titles | 生成标题 | ADMIN |
| GET | /api/admin/ai/writing/outline | 生成大纲（流式） | ADMIN |

**请求/响应示例**

```java
// AI续写请求
POST /api/admin/ai/writing/continue
Content-Type: application/json

{
  "context": "Spring Boot是一个优秀的Java框架...",
  "direction": "expand"  // continue/expand/example/summarize
}

// 流式响应（SSE）
data: 这将使应用程序更加
data: 模块化，便于后期维护
data: 和扩展。具体来说...
data: [DONE]

// AI纠错请求
POST /api/admin/ai/writing/proofread
Content-Type: application/json

{
  "text": "这里有一个重要的提意，他门的方案很优秀。"
}

// 纠错响应
{
  "code": 200,
  "data": {
    "errorCount": 2,
    "errors": [
      {
        "type": "spelling",
        "original": "提意",
        "suggestion": "提议",
        "reason": "错别字",
        "position": {"start": 10, "end": 12}
      },
      {
        "type": "spelling",
        "original": "他门",
        "suggestion": "他们",
        "reason": "错别字",
        "position": {"start": 15, "end": 17}
      }
    ]
  }
}
```

### 4.2 写作模板API

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/admin/templates | 获取模板列表 | ADMIN |
| GET | /api/admin/templates/{id} | 获取模板详情 | ADMIN |
| GET | /api/admin/templates/categories | 获取模板分类 | ADMIN |
| POST | /api/admin/templates | 创建模板 | ADMIN |
| PUT | /api/admin/templates/{id} | 更新模板 | ADMIN |
| DELETE | /api/admin/templates/{id} | 删除模板 | ADMIN |
| POST | /api/admin/templates/{id}/use | 使用模板（增加使用次数） | ADMIN |

**请求/响应示例**

```java
// 获取模板列表
GET /api/admin/templates?category=tutorial

{
  "code": 200,
  "data": {
    "total": 2,
    "list": [
      {
        "id": 1,
        "name": "Spring Boot 入门教程",
        "category": "tutorial",
        "icon": "📚",
        "description": "适合Spring Boot入门教程类文章",
        "useCount": 128,
        "isBuiltin": true
      },
      {
        "id": 2,
        "name": "Vue3 实战教程",
        "category": "tutorial",
        "icon": "📚",
        "description": "适合Vue3实战类文章",
        "useCount": 86,
        "isBuiltin": true
      }
    ]
  }
}

// 创建模板
POST /api/admin/templates
Content-Type: application/json

{
  "name": "我的自定义模板",
  "category": "tutorial",
  "icon": "📝",
  "description": "自定义教程模板",
  "sections": [
    {
      "title": "引言",
      "level": 2,
      "placeholder": "介绍文章主题...",
      "tips": "吸引读者注意",
      "required": true
    }
  ],
  "isPublic": false
}
```

### 4.3 智能排版API

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | /api/admin/format/preview | 预览排版变更 | ADMIN |
| POST | /api/admin/format/apply | 应用排版规则 | ADMIN |
| GET | /api/admin/format/rules | 获取排版规则列表 | ADMIN |
| PUT | /api/admin/format/rules/{id} | 更新规则状态 | ADMIN |
| POST | /api/admin/format/check-links | 检查链接有效性 | ADMIN |

**请求/响应示例**

```java
// 预览排版变更
POST /api/admin/format/preview
Content-Type: application/json

{
  "content": "# 文章内容\n## 前言\n...",
  "rules": ["heading_normalize", "empty_line_cleanup", "chinese_english_spacing"]
}

// 响应
{
  "code": 200,
  "data": {
    "changes": [
      {
        "rule": "heading_normalize",
        "description": "标题层级规范化",
        "count": 2,
        "details": [
          {"line": 15, "from": "# ", "to": "## "},
          {"line": 28, "from": "#### ", "to": "### "}
        ]
      },
      {
        "rule": "empty_line_cleanup",
        "description": "空行清理",
        "count": 5,
        "details": [
          {"line": 5, "action": "删除多余空行"}
        ]
      }
    ],
    "totalChanges": 7
  }
}

// 检查链接
POST /api/admin/format/check-links
Content-Type: application/json

{
  "content": "文章内容包含链接 https://example.com ..."
}

// 响应
{
  "code": 200,
  "data": {
    "total": 5,
    "valid": 4,
    "invalid": 1,
    "links": [
      {
        "url": "https://example.com",
        "line": 10,
        "status": 200,
        "valid": true
      },
      {
        "url": "https://example.com/broken",
        "line": 40,
        "status": 404,
        "valid": false,
        "error": "Not Found"
      }
    ]
  }
}
```

---

## 五、前端实现

### 5.1 目录结构

```
blog-web/src/
├── api/
│   ├── ai.js                    # AI相关API（扩展）
│   ├── template.js              # 模板API
│   └── format.js                # 排版API
├── components/
│   ├── writing/
│   │   ├── AiAssistantPanel.vue     # AI助手面板
│   │   ├── AiProofreadResult.vue    # AI纠错结果
│   │   ├── TemplateSelector.vue     # 模板选择器
│   │   ├── TemplateCard.vue         # 模板卡片
│   │   ├── FormatPanel.vue          # 排版面板
│   │   ├── FormatPreview.vue        # 排版预览
│   │   └── LinkCheckResult.vue      # 链接检查结果
│   └── editor/
│       └── EditorToolbar.vue        # 编辑器工具栏（扩展）
├── stores/
│   ├── ai.js                    # AI状态管理（扩展）
│   ├── template.js              # 模板状态管理
│   └── format.js                # 排版状态管理
└── views/admin/
    ├── ArticleEdit.vue          # 文章编辑页（修改）
    └── TemplateManage.vue       # 模板管理页（新增）
```

### 5.2 核心组件设计

**EditorToolbar.vue - 扩展工具栏**

```vue
<template>
  <div class="editor-toolbar">
    <!-- 原有按钮 -->
    <van-button @click="$emit('save')">保存</van-button>
    <van-button type="primary" @click="$emit('publish')">发布</van-button>
    
    <div class="toolbar-divider"></div>
    
    <!-- AI助手 -->
    <van-dropdown-menu>
      <van-dropdown-item ref="aiDropdown">
        <template #title>
          <span class="toolbar-btn">✨ AI助手</span>
        </template>
        <div class="ai-menu">
          <div class="menu-item" @click="handleAiAction('continue')">
            ✨ AI续写
          </div>
          <div class="menu-item" @click="handleAiAction('expand')">
            📝 AI扩写
          </div>
          <div class="menu-item" @click="handleAiAction('rewrite')">
            🔄 AI改写
          </div>
          <div class="menu-item" @click="handleAiAction('polish')">
            💅 AI润色
          </div>
          <div class="menu-divider"></div>
          <div class="menu-item" @click="handleAiAction('proofread')">
            ✅ AI纠错
          </div>
          <div class="menu-item" @click="handleAiAction('summary')">
            📋 生成摘要
          </div>
          <div class="menu-item" @click="handleAiAction('titles')">
            🏷️ 生成标题
          </div>
        </div>
      </van-dropdown-item>
    </van-dropdown-menu>
    
    <!-- 模板 -->
    <van-dropdown-menu>
      <van-dropdown-item ref="templateDropdown">
        <template #title>
          <span class="toolbar-btn">📄 模板</span>
        </template>
        <div class="template-menu">
          <div class="menu-item" @click="showTemplateSelector">
            📄 从模板创建
          </div>
          <div class="menu-item" @click="saveAsTemplate">
            💾 保存为模板
          </div>
          <div class="menu-item" @click="showMyTemplates">
            📚 我的模板
          </div>
        </div>
      </van-dropdown-item>
    </van-dropdown-menu>
    
    <!-- 排版 -->
    <van-dropdown-menu>
      <van-dropdown-item ref="formatDropdown">
        <template #title>
          <span class="toolbar-btn">📏 排版</span>
        </template>
        <div class="format-menu">
          <div class="menu-item" @click="quickFormat">
            ⚡ 一键排版
          </div>
          <div class="menu-item" @click="showFormatPanel">
            ⚙️ 排版设置
          </div>
          <div class="menu-item" @click="checkLinks">
            🔍 检查链接
          </div>
        </div>
      </van-dropdown-item>
    </van-dropdown-menu>
  </div>
</template>
```

**AiAssistantPanel.vue - AI助手面板**

```vue
<template>
  <van-popup
    v-model:show="visible"
    position="right"
    :style="{ width: '80%', height: '100%' }"
  >
    <div class="ai-assistant-panel">
      <!-- 头部 -->
      <div class="panel-header">
        <h3>AI写作助手</h3>
        <van-icon name="cross" @click="close" />
      </div>
      
      <!-- 设置区域 -->
      <div class="panel-settings">
        <!-- 续写方向选择 -->
        <div v-if="actionType === 'continue'" class="setting-group">
          <label>续写方向：</label>
          <van-radio-group v-model="settings.direction">
            <van-radio name="continue">继续当前思路</van-radio>
            <van-radio name="expand">展开详细说明</van-radio>
            <van-radio name="example">添加示例代码</van-radio>
            <van-radio name="summarize">总结归纳</van-radio>
          </van-radio-group>
        </div>
        
        <!-- 改写风格选择 -->
        <div v-if="actionType === 'rewrite' || actionType === 'polish'" class="setting-group">
          <label>风格选择：</label>
          <van-radio-group v-model="settings.style">
            <van-radio name="formal">正式专业</van-radio>
            <van-radio name="casual">轻松通俗</van-radio>
            <van-radio name="academic">学术严谨</van-radio>
            <van-radio name="concise">精简干练</van-radio>
          </van-radio-group>
        </div>
        
        <!-- 扩写类型选择 -->
        <div v-if="actionType === 'expand'" class="setting-group">
          <label>扩写类型：</label>
          <van-radio-group v-model="settings.expandType">
            <van-radio name="detail">增加细节描述</van-radio>
            <van-radio name="reason">添加原因分析</van-radio>
            <van-radio name="example">添加实例说明</van-radio>
            <van-radio name="comparison">添加对比分析</van-radio>
          </van-radio-group>
        </div>
      </div>
      
      <!-- 生成结果 -->
      <div class="panel-result">
        <div class="result-header">
          <span>生成结果：</span>
          <van-tag v-if="streamStatus === 'streaming'" type="primary">生成中...</van-tag>
        </div>
        <div class="result-content">
          <div v-if="streamStatus === 'idle'" class="result-placeholder">
            点击下方按钮开始生成
          </div>
          <div v-else class="result-text">
            {{ resultText }}<span v-if="streamStatus === 'streaming'" class="cursor">█</span>
          </div>
        </div>
      </div>
      
      <!-- 操作按钮 -->
      <div class="panel-actions">
        <van-button 
          v-if="streamStatus === 'idle' || streamStatus === 'complete' || streamStatus === 'error'"
          type="primary" 
          block 
          :loading="streamStatus === 'loading'"
          @click="generate"
        >
          {{ streamStatus === 'error' ? '重新生成' : '生成' }}
        </van-button>
        <van-button 
          v-if="streamStatus === 'streaming'"
          type="danger" 
          block 
          @click="cancel"
        >
          取消
        </van-button>
        <div v-if="streamStatus === 'complete'" class="secondary-actions">
          <van-button @click="copyResult">复制</van-button>
          <van-button type="primary" @click="applyToEditor">应用到编辑器</van-button>
        </div>
      </div>
    </div>
  </van-popup>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { showToast } from 'vant'
import { streamWriting } from '@/api/ai'

const props = defineProps({
  show: Boolean,
  actionType: String, // continue/expand/rewrite/polish
  selectedText: String,
  context: String
})

const emit = defineEmits(['update:show', 'apply'])

const visible = computed({
  get: () => props.show,
  set: (val) => emit('update:show', val)
})

const settings = ref({
  direction: 'continue',
  style: 'formal',
  expandType: 'detail'
})

const resultText = ref('')
const streamStatus = ref('idle') // idle/loading/streaming/complete/error
let abortController = null

const generate = async () => {
  if (!props.selectedText && !props.context) {
    showToast('请先选择文本或输入内容')
    return
  }
  
  resultText.value = ''
  streamStatus.value = 'loading'
  
  const data = {
    type: props.actionType,
    text: props.selectedText,
    context: props.context,
    ...settings.value
  }
  
  abortController = streamWriting(
    data,
    (content) => {
      streamStatus.value = 'streaming'
      resultText.value += content
    },
    () => {
      streamStatus.value = 'complete'
    },
    (error) => {
      streamStatus.value = 'error'
      showToast('生成失败：' + error.message)
    }
  )
}

const cancel = () => {
  if (abortController) {
    abortController.abort()
    abortController = null
  }
  streamStatus.value = 'idle'
}

const copyResult = async () => {
  try {
    await navigator.clipboard.writeText(resultText.value)
    showToast('已复制到剪贴板')
  } catch (e) {
    showToast('复制失败')
  }
}

const applyToEditor = () => {
  emit('apply', resultText.value)
  close()
}

const close = () => {
  cancel()
  visible.value = false
}

// 重置状态
watch(() => props.show, (val) => {
  if (val) {
    resultText.value = ''
    streamStatus.value = 'idle'
  }
})
</script>
```

### 5.3 状态管理

**stores/template.js**

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
      const { data } = await getTemplates({ category })
      templates.value = data.list
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

## 六、实施计划

### 阶段一：AI写作助手增强（1.5周）

| 任务 | 工作量 | 说明 |
|------|--------|------|
| 新增Prompt模板 | 0.5天 | 续写/扩写/改写/纠错模板 |
| 后端AI服务扩展 | 2天 | 新增API接口 |
| 前端AI助手面板组件 | 3天 | 设置面板、结果展示 |
| 编辑器集成 | 2天 | 工具栏、右键菜单、快捷键 |
| 测试与优化 | 1天 | 功能测试、性能优化 |

### 阶段二：写作模板系统（1.5周）

| 任务 | 工作量 | 说明 |
|------|--------|------|
| 数据库表设计与创建 | 0.5天 | writing_template表 |
| 后端模板CRUD API | 1.5天 | 完整CRUD接口 |
| 内置模板数据准备 | 1天 | 5-6个预设模板 |
| 前端模板选择弹窗 | 2天 | 分类展示、搜索、选择 |
| 模板应用到编辑器 | 2天 | 内容填充、提示显示 |
| 模板管理页面 | 1天 | 管理员管理模板 |

### 阶段三：智能排版工具（1周）

| 任务 | 工作量 | 说明 |
|------|--------|------|
| 数据库表设计与创建 | 0.5天 | format_rule表 |
| 排版规则引擎实现 | 2天 | 正则/AI/HTTP规则执行 |
| 后端排版API | 1.5天 | 预览、应用、链接检查 |
| 前端排版面板 | 2天 | 设置面板、预览确认 |
| 测试与优化 | 1天 | 规则测试、边界处理 |

### 总工时：4周

---

## 七、风险与注意事项

### 7.1 技术风险

| 风险 | 影响 | 应对措施 |
|------|------|----------|
| AI响应延迟 | 用户体验差 | 流式响应、加载提示、超时处理 |
| 排版规则冲突 | 结果不符合预期 | 规则优先级、预览机制 |
| 模板内容过长 | 编辑器性能 | 分段加载、懒加载 |
| 链接检查超时 | 等待时间过长 | 异步检查、后台任务 |

### 7.2 安全考虑

| 安全点 | 措施 |
|--------|------|
| AI内容安全 | 复用现有XML标签隔离机制 |
| 模板XSS防护 | 输出时HTML编码 |
| 排版规则注入 | 仅管理员可编辑规则 |
| API频率限制 | 复用现有限流机制 |

### 7.3 性能优化

| 优化点 | 方案 |
|--------|------|
| AI请求 | 流式响应、取消机制 |
| 模板缓存 | Redis缓存热门模板 |
| 排版预览 | 增量计算、差异展示 |
| 链接检查 | 并发检查、结果缓存 |

---

## 八、后续扩展

### 8.1 短期扩展

- [ ] 模板分享功能
- [ ] 自定义排版规则
- [ ] AI写作历史记录
- [ ] 模板评分系统

### 8.2 长期扩展

- [ ] 多人协作编辑（WebSocket）
- [ ] AI写作风格学习
- [ ] 智能内容推荐
- [ ] 版本对比功能
