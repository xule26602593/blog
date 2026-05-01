---
title: 话题灵感库功能设计
date: 2026-05-01
category: feature
module: blog-server, blog-web
status: draft
---

# 话题灵感库功能设计

## 概述

话题灵感库是一个面向管理员的个人创作规划工具，帮助记录灵感、AI分析话题价值、追踪创作进度。

### 核心价值

- **灵感管理** - 快速记录零散灵感和热点话题
- **AI辅助决策** - 分析话题价值、写作角度、目标受众
- **进度追踪** - 可视化创作状态流转

### 与现有写作助手的关系

| 功能 | 话题灵感库 | 写作助手 |
|------|-----------|---------|
| **解决问题** | "写什么？" | "怎么写？" |
| **使用阶段** | 构思阶段 | 写作阶段 |
| **核心能力** | 灵感记录、AI分析、状态追踪 | 大纲生成、续写、润色、校对 |

两者是互补关系，话题库负责写作前的规划，写作助手负责写作中的执行。

---

## 功能设计

### 功能清单

| 功能 | 描述 | 优先级 |
|------|------|--------|
| 记录灵感 | 快速创建话题，记录标题、描述、来源 | P0 |
| AI分析 | 分析话题价值、写作角度、目标受众、关键词 | P0 |
| 状态管理 | 待写 → 写作中 → 已发布 / 放弃 | P0 |
| 优先级排序 | 高/中/低三级优先级 | P1 |
| 文章关联 | 发布后关联文章，自动更新状态 | P1 |

**不包含：**
- 大纲生成（由写作助手负责）
- 内容续写、润色等（由写作助手负责）

---

## 数据库设计

### 话题表

```sql
-- 话题灵感表
CREATE TABLE `topic` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title` VARCHAR(200) NOT NULL COMMENT '话题标题',
    `description` TEXT COMMENT '话题描述/灵感记录',
    `source` VARCHAR(100) COMMENT '来源(知乎/微博/掘金/灵感/读书/工作)',
    `source_url` VARCHAR(500) COMMENT '来源链接',

    -- AI分析结果
    `analysis` TEXT COMMENT 'AI分析结果(JSON)',
    `analysis_status` TINYINT DEFAULT 0 COMMENT '0:待分析 1:分析中 2:已完成 3:失败',

    -- 创作状态追踪
    `status` TINYINT DEFAULT 0 COMMENT '0:待写 1:写作中 2:已发布 3:放弃',
    `article_id` BIGINT COMMENT '关联文章ID',

    -- 优先级与时间
    `priority` TINYINT DEFAULT 2 COMMENT '优先级 1:高 2:中 3:低',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='话题灵感表';
```

### AI分析结果结构

```json
{
  "writingAngles": [
    "从新手角度讲解入门流程",
    "对比分析主流方案优缺点",
    "实战踩坑经验分享"
  ],
  "targetAudience": "Java开发者、技术团队负责人",
  "difficulty": 2,
  "estimatedReads": "3000-8000",
  "keywords": ["Spring Boot", "Java 17", "性能优化"],
  "value": "帮助开发者快速理解新特性并应用到实际项目",
  "suggestions": "建议结合实际案例讲解，增加代码示例"
}
```

**字段说明：**
- `writingAngles` - 推荐写作角度（3个）
- `targetAudience` - 目标受众画像
- `difficulty` - 内容难度（1:入门 2:进阶 3:高级）
- `estimatedReads` - 预期阅读量级
- `keywords` - SEO关键词建议
- `value` - 内容价值评估
- `suggestions` - 写作建议

---

## API设计

### 管理端接口

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/admin/topics` | 创建话题 |
| PUT | `/api/admin/topics/{id}` | 更新话题 |
| DELETE | `/api/admin/topics/{id}` | 删除话题 |
| GET | `/api/admin/topics` | 话题列表（分页） |
| GET | `/api/admin/topics/{id}` | 话题详情 |
| POST | `/api/admin/topics/{id}/analyze` | 触发AI分析 |
| PUT | `/api/admin/topics/{id}/status` | 更新状态 |
| POST | `/api/admin/topics/{id}/link` | 关联文章 |

### 请求/响应结构

**创建话题请求：**
```json
{
  "title": "Spring Boot 3.0新特性深度解析",
  "description": "Spring Boot 3.0带来了很多重大更新...",
  "source": "掘金热门",
  "sourceUrl": "https://juejin.cn/...",
  "priority": 1,
  "autoAnalyze": true
}
```

**话题列表响应：**
```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 1,
        "title": "Spring Boot 3.0新特性深度解析",
        "description": "Spring Boot 3.0带来了很多重大更新...",
        "source": "掘金热门",
        "status": 0,
        "analysisStatus": 2,
        "priority": 1,
        "createTime": "2026-05-01 10:00:00"
      }
    ],
    "total": 15
  }
}
```

**话题详情响应：**
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "title": "Spring Boot 3.0新特性深度解析",
    "description": "Spring Boot 3.0带来了很多重大更新...",
    "source": "掘金热门",
    "sourceUrl": "https://juejin.cn/...",
    "status": 0,
    "analysisStatus": 2,
    "priority": 1,
    "analysis": {
      "writingAngles": ["角度1", "角度2", "角度3"],
      "targetAudience": "Java开发者",
      "difficulty": 2,
      "estimatedReads": "3000-8000",
      "keywords": ["Spring Boot", "Java 17"],
      "value": "内容价值说明",
      "suggestions": "写作建议"
    },
    "articleId": null,
    "createTime": "2026-05-01 10:00:00"
  }
}
```

---

## 后端架构

### 新增文件

```
blog-server/src/main/java/com/blog/
├── domain/
│   └── entity/
│       └── Topic.java                    # 话题实体
├── domain/
│   └── dto/
│       ├── TopicCreateRequest.java       # 创建请求
│       ├── TopicUpdateRequest.java       # 更新请求
│       ├── TopicQueryRequest.java        # 查询请求
│       └── TopicAnalysisResult.java      # AI分析结果
├── repository/
│   └── TopicMapper.java                  # MyBatis Mapper
├── service/
│   ├── TopicService.java                 # 服务接口
│   └── impl/
│       └── TopicServiceImpl.java         # 服务实现
└── controller/
    └── admin/
        └── TopicController.java          # 管理端Controller
```

### 核心服务方法

```java
public interface TopicService {

    // 创建话题
    void createTopic(TopicCreateRequest request);

    // 更新话题
    void updateTopic(Long id, TopicUpdateRequest request);

    // 删除话题
    void deleteTopic(Long id);

    // 分页查询
    PageResult<Topic> listTopics(TopicQueryRequest request);

    // 获取详情
    Topic getTopicDetail(Long id);

    // 触发AI分析
    void analyzeTopic(Long id);

    // 更新状态
    void updateStatus(Long id, Integer status);

    // 关联文章
    void linkArticle(Long id, Long articleId);
}
```

### AI分析实现

**Prompt模板：**
```
你是内容策划专家。请分析以下话题的创作价值。

话题标题：{title}
话题描述：{description}

请以JSON格式返回分析结果，包含以下字段：
- writingAngles: 推荐的3个写作角度（数组）
- targetAudience: 目标受众描述
- difficulty: 内容难度（1-3，1入门2进阶3高级）
- estimatedReads: 预期阅读量级（如"1000-3000"）
- keywords: SEO关键词建议（数组，5-8个）
- value: 内容价值评估（一句话）
- suggestions: 写作建议（一句话）

只返回JSON，不要其他内容。
```

**分析流程：**
1. 用户创建话题或点击"重新分析"
2. 后端调用 `AiService` 发送Prompt
3. 解析返回的JSON，存储到 `analysis` 字段
4. 更新 `analysis_status` 为已完成

---

## 前端架构

### 新增文件

```
blog-web/src/
├── api/
│   └── topic.js              # API请求
└── views/
    └── admin/
        ├── TopicList.vue     # 话题列表页
        └── TopicDetail.vue   # 话题详情/编辑页
```

### 路由配置

在 `router/index.js` 添加：

```javascript
{
  path: '/admin/topics',
  component: () => import('@/views/admin/TopicList.vue'),
  meta: { title: '话题灵感', requiresAuth: true, requiresAdmin: true }
},
{
  path: '/admin/topics/:id',
  component: () => import('@/views/admin/TopicDetail.vue'),
  meta: { title: '话题详情', requiresAuth: true, requiresAdmin: true }
}
```

### 导航配置

在 `admin/Layout.vue` 的内容管理分组中添加：

```vue
<router-link
  to="/admin/topics"
  class="nav-item"
  :class="{ active: isActive('/admin/topics') }"
>
  <svg class="nav-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
    <path stroke-linecap="round" stroke-linejoin="round" d="M12 18v-5.25m0 0a6.01 6.01 0 001.5-.189m-1.5.189a6.01 6.01 0 01-1.5-.189m3.75 7.478a12.06 12.06 0 01-4.5 0m3.75 2.383a14.406 14.406 0 01-3 0M14.25 18v-.192c0-.983.658-1.823 1.508-2.316a7.5 7.5 0 10-7.517 0c.85.493 1.509 1.333 1.509 2.316V18" />
  </svg>
  <span class="nav-label">话题灵感</span>
</router-link>
```

---

## UI设计

### 话题列表页

```
┌─────────────────────────────────────────────────────────┐
│  💡 话题灵感库                        [+ 新建话题]       │
├─────────────────────────────────────────────────────────┤
│  [全部(15)] [待写(8)] [写作中(2)] [已发布(4)] [放弃(1)] │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │ 🔥 Spring Boot 3.0新特性深度解析                │   │
│  │ 来源：掘金热门 | 优先级：高 | 状态：待写         │   │
│  │ 创建于 2026-05-01                               │   │
│  │ [AI分析 ✅] [开始写作] [编辑] [删除]            │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │ 📌 Vue 3.5响应式原理完全指南                    │   │
│  │ 来源：知乎 | 优先级：中 | 状态：写作中           │   │
│  │ 创建于 2026-04-28                               │   │
│  │ [AI分析 ✅] [继续写作] [编辑]                   │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
│  ┌─────────────────────────────────────────────────┐   │
│  │ ✅ MySQL索引优化实战                            │   │
│  │ 来源：工作实践 | 已发布 | 文章ID: 123           │   │
│  │ 创建于 2026-04-20                               │   │
│  │ [查看文章] [查看分析]                           │   │
│  └─────────────────────────────────────────────────┘   │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### 新建/编辑话题弹窗

```
┌─────────────────────────────────────────┐
│  新建话题                               │
├─────────────────────────────────────────┤
│                                         │
│  话题标题 *                             │
│  ┌───────────────────────────────────┐ │
│  │                                   │ │
│  └───────────────────────────────────┘ │
│                                         │
│  话题描述                               │
│  ┌───────────────────────────────────┐ │
│  │                                   │ │
│  │                                   │ │
│  └───────────────────────────────────┘ │
│                                         │
│  来源                                   │
│  [请选择 ▼]                            │
│   - 知乎                               │
│   - 微博                               │
│   - 掘金                               │
│   - 灵感                               │
│   - 读书                               │
│   - 工作                               │
│                                         │
│  来源链接（可选）                       │
│  ┌───────────────────────────────────┐ │
│  │                                   │ │
│  └───────────────────────────────────┘ │
│                                         │
│  优先级                                 │
│  ◉ 高  ○ 中  ○ 低                     │
│                                         │
│  ☐ 保存后自动进行AI分析                │
│                                         │
│     [取消]           [保存]            │
└─────────────────────────────────────────┘
```

### AI分析结果展示

```
┌─────────────────────────────────────────────────────────┐
│  🤖 AI分析结果                            [重新分析]    │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  📌 推荐写作角度                                        │
│  1. 从Spring Boot 2.x迁移实战指南                      │
│  2. Native编译性能对比测评                              │
│  3. 新特性深度源码解析                                  │
│                                                         │
│  👥 目标受众                                            │
│  Java开发者、架构师、技术团队负责人                     │
│                                                         │
│  📊 难度与预期                                          │
│  难度：进阶 | 预期阅读量：3000-8000                     │
│                                                         │
│  🔍 关键词                                              │
│  Spring Boot 3.0, Java 17, Native编译                  │
│                                                         │
│  💡 价值评估                                            │
│  帮助开发者快速理解新特性并应用到实际项目               │
│                                                         │
│  📝 写作建议                                            │
│  建议结合实际案例讲解，增加代码示例                     │
│                                                         │
├─────────────────────────────────────────────────────────┤
│  [开始写作]  [标记为写作中]  [关闭]                     │
└─────────────────────────────────────────────────────────┘
```

---

## 核心交互流程

### 流程1：记录灵感

```
点击"新建话题"
    ↓
填写标题、描述、来源、优先级
    ↓
勾选"保存后自动进行AI分析"
    ↓
点击保存
    ↓
话题创建成功，跳转到详情页
    ↓
（如果勾选自动分析）AI开始分析，显示"分析中..."
    ↓
分析完成，展示分析结果
```

### 流程2：开始创作

```
查看话题列表/详情
    ↓
点击"开始写作"
    ↓
跳转到文章编辑器
    ↓
（可选）预填充话题标题作为文章标题
    ↓
使用写作助手完成创作
    ↓
文章发布
    ↓
自动关联话题，状态更新为"已发布"
```

### 流程3：状态管理

```
话题状态流转：

待写 ──→ 写作中 ──→ 已发布
  │          │
  │          └──→ 放弃
  │
  └──────→ 放弃
```

---

## 与现有系统的集成

### 与文章编辑器的集成

**方案1：简单跳转（推荐）**
- 点击"开始写作"跳转到 `/admin/articles/create`
- 预填充话题标题作为文章标题
- 在文章编辑页面的侧边栏显示关联话题的AI建议

**方案2：深度集成（后期优化）**
- 文章编辑器增加"关联话题"选择器
- 选择话题后，侧边栏显示AI分析结果
- 文章发布时自动关联话题

### 与仪表盘的集成

在 Dashboard 首页添加话题统计卡片：

```
┌─────────────────────┐
│ 💡 待写话题         │
│                     │
│      8 个           │
│                     │
│ 高优先级: 3         │
└─────────────────────┘
```

---

## 实现计划

### 第一阶段：核心功能（P0）

1. 数据库表创建
2. 后端实体、DTO、Mapper
3. 后端Service和Controller
4. 前端话题列表页
5. 前端新建/编辑弹窗
6. AI分析功能集成

### 第二阶段：状态追踪（P1）

1. 状态管理UI
2. 优先级排序
3. 文章关联功能
4. 仪表盘集成

### 第三阶段：体验优化（P2）

1. 分析结果优化展示
2. 批量操作
3. 搜索过滤
4. 导出功能

---

## 风险与注意事项

### AI分析可靠性

- **风险**：AI返回的JSON可能格式不正确
- **方案**：增加JSON解析异常处理，失败时记录错误并允许重新分析

### 性能考虑

- **风险**：AI分析耗时较长
- **方案**：使用异步处理，前端轮询或WebSocket推送状态

### 与写作助手的边界

- **注意**：话题库不生成大纲，用户需要使用写作助手生成大纲
- **方案**：在分析结果中提示"使用写作助手生成详细大纲"

---

## 总结

话题灵感库是一个轻量级的创作规划工具，定位清晰：

- **解决痛点**：不知道写什么
- **核心价值**：灵感管理 + AI分析 + 进度追踪
- **与写作助手互补**：话题库负责规划，写作助手负责执行

功能范围精简，实现快速，后续可根据使用反馈持续迭代。
