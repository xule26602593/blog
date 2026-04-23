# 文章系列功能设计文档

## 概述

为博客系统添加文章系列（专栏）功能，支持将多篇文章组织成系列内容，便于读者系统性学习和阅读。

## 需求确认

- ✅ 文章专栏/系列功能
- ✅ 支持「有序」和「无序」两种模式
- ✅ 管理后台独立管理页面
- ✅ 前台：独立系列列表页 + 首页侧边栏 + 文章详情页显示所属系列

## 数据库设计

### series 表（文章系列表）

```sql
CREATE TABLE `series` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '系列名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '系列介绍',
    `cover_image` VARCHAR(255) DEFAULT NULL COMMENT '封面图片',
    `mode` TINYINT DEFAULT 0 COMMENT '模式 0:有序(章节式) 1:无序(主题式)',
    `article_count` INT DEFAULT 0 COMMENT '文章数量(冗余字段)',
    `view_count` BIGINT DEFAULT 0 COMMENT '浏览量',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态 0:禁用 1:启用',
    `author_id` BIGINT DEFAULT NULL COMMENT '创建者ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_status_sort` (`status`, `sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章系列表';
```

### series_article 表（系列文章关联表）

```sql
CREATE TABLE `series_article` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `series_id` BIGINT NOT NULL COMMENT '系列ID',
    `article_id` BIGINT NOT NULL COMMENT '文章ID',
    `chapter_order` INT DEFAULT 0 COMMENT '章节顺序(有序模式下使用)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_series_article` (`series_id`, `article_id`),
    KEY `idx_article_id` (`article_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系列文章关联表';
```

**设计要点：**
- `mode` 字段区分有序/无序模式
- `chapter_order` 仅在有序模式下有意义，用于排序
- `article_count` 冗余字段，避免频繁 COUNT 查询
- 唯一索引确保一篇文章在同一系列中只出现一次

## 后端 API 设计

### 管理端 API（需要 ADMIN 角色）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/admin/series` | 系列列表（分页、筛选） |
| GET | `/api/admin/series/{id}` | 系列详情（含关联文章列表） |
| POST | `/api/admin/series` | 创建系列 |
| PUT | `/api/admin/series/{id}` | 更新系列信息 |
| DELETE | `/api/admin/series/{id}` | 删除系列 |
| POST | `/api/admin/series/{id}/articles` | 添加文章到系列（批量） |
| DELETE | `/api/admin/series/{id}/articles/{articleId}` | 从系列移除文章 |
| PUT | `/api/admin/series/{id}/articles/order` | 调整文章顺序（有序模式） |

### 门户端 API（公开）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/portal/series` | 系列列表（分页） |
| GET | `/api/portal/series/{id}` | 系列详情（含文章列表） |
| GET | `/api/portal/series/hot` | 热门系列（首页侧边栏用） |

### 响应结构示例

**系列详情响应：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "Spring Boot 实战教程",
    "description": "从零开始学习 Spring Boot",
    "coverImage": "/uploads/xxx.jpg",
    "mode": 0,
    "articleCount": 10,
    "viewCount": 1234,
    "articles": [
      {
        "id": 101,
        "title": "第一章：环境搭建",
        "summary": "...",
        "coverImage": "/uploads/xxx.jpg",
        "viewCount": 100,
        "chapterOrder": 1
      }
    ]
  }
}
```

## 前端页面设计

### 管理后台

**1. 系列管理页面（新增）**
- 路由：`/admin/series`
- 功能：系列列表、搜索、新建、编辑、删除
- 列表显示：名称、模式（有序/无序）、文章数、状态、操作按钮

**2. 系列编辑页面（新增）**
- 路由：`/admin/series/edit/:id` 或 `/admin/series/create`
- 表单字段：名称、描述、封面、模式选择、状态
- 文章管理区：
  - 搜索并添加文章到系列
  - 拖拽或上下箭头调整顺序（有序模式）
  - 从系列移除文章

**3. 文章编辑页修改**
- 新增「所属系列」选择器
- 可选择将文章加入某个系列
- 若系列为有序模式，自动分配下一个章节序号

### 门户前端

**1. 系列列表页（新增）**
- 路由：`/series`
- 展示：系列卡片（封面、名称、描述、文章数）
- 支持分页加载

**2. 系列详情页（新增）**
- 路由：`/series/:id`
- 顶部：系列封面、名称、介绍
- 文章列表：
  - 有序模式：显示章节编号（第1章、第2章...）
  - 无序模式：显示为普通文章列表

**3. 首页侧边栏**
- 新增「热门系列」模块，显示 3-5 个热门系列

**4. 文章详情页**
- 文章标题下方显示「所属系列：xxx」
- 点击跳转到系列详情页

## 后端实现要点

### 包结构（遵循现有风格）

```
com.blog
├── controller/
│   ├── admin/AdminSeriesController.java
│   └── portal/PortalSeriesController.java
├── domain/
│   ├── entity/Series.java
│   ├── entity/SeriesArticle.java
│   ├── dto/SeriesDTO.java
│   └── vo/SeriesVO.java
├── repository/
│   ├── SeriesMapper.java
│   └── SeriesArticleMapper.java
└── service/
    ├── SeriesService.java
    └── impl/SeriesServiceImpl.java
```

### 业务逻辑要点

- 添加文章到系列时，自动更新 `article_count`
- 有序模式：新文章自动分配 `chapter_order = 当前最大值 + 1`
- 删除系列时，级联删除 `series_article` 关联记录
- 文章删除时，同步更新系列的 `article_count`

### 查询优化

- 系列详情查询：一次 JOIN 查询获取系列信息和文章列表
- 热门系列：按 `view_count` 排序，Redis 缓存

## 前端实现要点

### API 模块

- 新建 `src/api/series.js`

### 状态管理

- 不需要 Pinia store，各页面独立管理数据

### 组件复用

- 文章选择器组件：用于系列编辑页添加文章
- 系列卡片组件：用于列表页和侧边栏

## 注意事项

- 系列删除为逻辑删除，关联记录保留
- 文章可同时属于多个系列
- 有序模式下章节顺序可手动调整
- 门户端只显示 `status=1` 且有已发布文章的系列
