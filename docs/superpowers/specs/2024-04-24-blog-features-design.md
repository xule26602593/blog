# 博客系统功能扩展设计文档

> 创建日期: 2026-04-24
> 状态: 待审核

## 概述

本文档描述博客系统的功能扩展设计，共 16 个功能，分 5 个阶段实现。系统定位为**个人博客**，支持**高互动**体验（访客可互相关注、私信）。

---

## 第一阶段：基础功能补全

### 1.1 留言板

**描述**: 独立的留言板页面，访客可以留言互动。

**数据库**: 已有 `message` 表，结构如下：
- `id` - 主键
- `user_id` - 用户ID（登录用户）
- `nickname` - 昵称（游客）
- `email` - 邮箱（游客）
- `content` - 留言内容
- `status` - 状态 0:待审核 1:已通过 2:已拒绝
- `ip_address` - IP地址
- `create_time`, `update_time`

**后端 API**:
| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/portal/messages` | GET | 分页获取留言列表（已通过审核） |
| `/api/portal/messages` | POST | 提交留言（登录用户或游客） |
| `/api/admin/messages` | GET | 管理员获取所有留言（支持按状态筛选） |
| `/api/admin/messages/{id}/audit` | PUT | 审核留言（通过/拒绝） |
| `/api/admin/messages/{id}` | DELETE | 删除留言 |

**前端页面**:
- `views/portal/MessageBoard.vue` - 留言板页面
- 支持登录用户和游客留言
- 留言展示：头像、昵称、内容、时间
- 支持分页加载

**业务规则**:
- 留言需要审核后才能公开显示
- 敏感词过滤（与评论共用敏感词库）
- 登录用户自动填充用户信息，游客需手动填写昵称和邮箱

---

### 1.2 用户管理

**描述**: 后台管理注册用户，包括查看、禁用、删除等操作。

**数据库**: 使用现有 `sys_user` 表，可能需要扩展：
- 添加 `bio` 字段 - 个人简介
- 添加 `website` 字段 - 个人网站
- 已有 `follower_count`、`following_count` 字段

**后端 API**:
| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/admin/users` | GET | 分页获取用户列表 |
| `/api/admin/users/{id}` | GET | 获取用户详情 |
| `/api/admin/users/{id}/status` | PUT | 更新用户状态（启用/禁用） |
| `/api/admin/users/{id}/role` | PUT | 修改用户角色 |
| `/api/admin/users/{id}` | DELETE | 删除用户（软删除） |

**前端页面**:
- `views/admin/UserManage.vue` - 用户管理页面
- 用户列表：头像、用户名、昵称、邮箱、角色、状态、注册时间
- 搜索：按用户名、昵称、邮箱搜索
- 筛选：按角色、状态筛选
- 操作：查看详情、禁用/启用、删除

**业务规则**:
- 不能删除或禁用自己
- 不能修改自己的角色
- 删除用户为软删除，保留数据完整性

---

### 1.3 敏感词过滤

**描述**: 评论、留言等内容自动过滤敏感词。

**数据库**: 新建 `sensitive_word` 表：
```sql
CREATE TABLE `sensitive_word` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `word` VARCHAR(50) NOT NULL COMMENT '敏感词',
    `category` VARCHAR(50) DEFAULT NULL COMMENT '分类',
    `replace_word` VARCHAR(50) DEFAULT '*' COMMENT '替换字符',
    `status` TINYINT DEFAULT 1 COMMENT '状态 0:禁用 1:启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_word` (`word`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='敏感词表';
```

**后端实现**:
- 使用 DFA（确定有限状态自动机）算法进行敏感词检测
- 支持三种处理方式：替换、拦截、标记待审核
- 提供敏感词管理 API

**API**:
| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/admin/sensitive-words` | GET | 获取敏感词列表 |
| `/api/admin/sensitive-words` | POST | 添加敏感词 |
| `/api/admin/sensitive-words/batch` | POST | 批量导入敏感词 |
| `/api/admin/sensitive-words/{id}` | PUT | 更新敏感词 |
| `/api/admin/sensitive-words/{id}` | DELETE | 删除敏感词 |

**前端页面**:
- `views/admin/SensitiveWordManage.vue` - 敏感词管理页面
- 支持单个添加和批量导入
- 分组管理敏感词

**应用场景**:
- 评论内容：自动替换敏感词为 `***`
- 留言内容：同上
- 私信内容：同上

---

### 1.4 缓存优化

**描述**: 对热点数据实施缓存策略，提升性能。

**缓存策略**:

| 数据类型 | 缓存Key | 过期时间 | 更新策略 |
|----------|---------|----------|----------|
| 文章详情 | `article:{id}` | 1小时 | 文章更新时删除 |
| 文章列表（首页） | `articles:page:{page}` | 10分钟 | 文章发布/删除时清除相关 |
| 热门文章 | `articles:hot` | 30分钟 | 定时刷新 |
| 分类列表 | `categories` | 1小时 | 分类变更时删除 |
| 标签列表 | `tags` | 1小时 | 标签变更时删除 |
| 系统配置 | `sys:config` | 永久 | 配置变更时更新 |
| 敏感词库 | `sensitive:words` | 永久 | 敏感词变更时重建 |

**技术实现**:
- 使用 Redis 作为缓存层
- 封装 `CacheService` 统一管理缓存操作
- 支持 Cache-Aside 模式：先查缓存，未命中再查数据库
- 支持分布式锁防止缓存击穿

**代码结构**:
```
com.blog.service
├── CacheService.java           # 缓存服务接口
└── impl/CacheServiceImpl.java  # 缓存服务实现
```

---

## 第二阶段：社交基础与阅读体验

### 2.1 访客互相关注

**描述**: 扩展现有关注功能，支持访客之间相互关注。

**数据库**: 使用现有 `user_follow` 表，但需调整业务逻辑：
- 当前关注是"关注文章作者"
- 扩展为通用的"用户间关注"

**关注类型**:
- `following` - 我关注的人（我关注的作者和其他访客）
- `follower` - 关注我的人（粉丝）

**后端 API**: 现有 API 基本可用，需要调整：
| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/follow/{userId}` | POST | 关注/取消关注用户 |
| `/api/follow/check/{userId}` | GET | 检查是否关注 |
| `/api/follow/following/{userId}` | GET | 获取关注列表 |
| `/api/follow/followers/{userId}` | GET | 获取粉丝列表 |

**前端页面**:
- 已有 `UserFollowing.vue` 和 `UserFollowers.vue`，需要调整适配

**业务规则**:
- 不能关注自己
- 关注/取关需更新双方的计数（follower_count, following_count）
- 关注操作发送通知给被关注者

---

### 2.2 用户主页

**描述**: 访客公开主页，展示基本信息和最近评论。

**数据库**: 扩展 `sys_user` 表：
```sql
ALTER TABLE `sys_user` ADD COLUMN `bio` VARCHAR(200) DEFAULT NULL COMMENT '个人简介';
ALTER TABLE `sys_user` ADD COLUMN `website` VARCHAR(255) DEFAULT NULL COMMENT '个人网站';
```

**后端 API**:
| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/portal/users/{id}` | GET | 获取用户公开信息 |
| `/api/portal/users/{id}/comments` | GET | 获取用户最近评论 |
| `/api/user/profile` | GET | 获取当前用户完整资料 |
| `/api/user/profile` | PUT | 更新个人资料 |

**前端页面**:
- `views/portal/UserProfilePublic.vue` - 公开主页
- 展示内容：
  - 头像、昵称、个人简介、个人网站
  - 关注数、粉丝数
  - 最近5条评论（可展开查看更多）
  - 关注/取消关注按钮（已登录用户可见）

**业务规则**:
- 只展示已通过审核的评论
- 访客可查看任何注册用户的公开主页

---

### 2.3 阅读模式

**描述**: 文章页面支持字体大小、背景色、阅读进度设置。

**功能点**:
- 字体大小调整：小、中、大、特大
- 背景色切换：默认、护眼（米黄）、夜间（深色）
- 阅读进度：显示当前阅读百分比
- 阅读进度记忆：下次打开从上次位置继续

**前端实现**:
- 在 `ArticleDetail.vue` 添加阅读设置面板
- 使用 localStorage 存储用户偏好设置
- 阅读进度通过滚动事件计算并存储

**代码位置**:
- 组件: `components/ReadingSettings.vue`
- 存储: Pinia store `stores/reading.js`

---

### 2.4 目录导航

**描述**: 长文章侧边目录导航。

**现状**: 已有 `TocNavigation.vue` 组件，需确认功能完整性。

**功能确认**:
- 自动提取 Markdown 标题生成目录
- 点击目录项滚动到对应位置
- 滚动时高亮当前目录项
- 移动端适配：折叠按钮或抽屉展示

**如需完善**:
- 添加目录项激活状态的平滑过渡
- 移动端添加目录抽屉组件

---

## 第三阶段：社交增强与内容分发

### 3.1 富媒体私信

**描述**: 用户间一对一私信，支持图片、表情、代码片段。

**数据库**: 新建表
```sql
-- 私信会话表
CREATE TABLE `conversation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user1_id` BIGINT NOT NULL COMMENT '用户1 ID（较小ID）',
    `user2_id` BIGINT NOT NULL COMMENT '用户2 ID（较大ID）',
    `last_message_id` BIGINT DEFAULT NULL COMMENT '最后一条消息ID',
    `last_message_time` DATETIME DEFAULT NULL COMMENT '最后消息时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_users` (`user1_id`, `user2_id`),
    KEY `idx_user1` (`user1_id`),
    KEY `idx_user2` (`user2_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='私信会话表';

-- 私信消息表
CREATE TABLE `private_message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `conversation_id` BIGINT NOT NULL COMMENT '会话ID',
    `sender_id` BIGINT NOT NULL COMMENT '发送者ID',
    `receiver_id` BIGINT NOT NULL COMMENT '接收者ID',
    `content` TEXT NOT NULL COMMENT '消息内容',
    `message_type` TINYINT DEFAULT 1 COMMENT '消息类型 1:文本 2:图片 3:代码',
    `is_read` TINYINT DEFAULT 0 COMMENT '是否已读',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_conversation` (`conversation_id`),
    KEY `idx_sender` (`sender_id`),
    KEY `idx_receiver` (`receiver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='私信消息表';
```

**后端 API**:
| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/messages/conversations` | GET | 获取会话列表 |
| `/api/messages/conversations/{userId}` | GET | 获取与某用户的会话 |
| `/api/messages/{conversationId}` | GET | 获取会话消息（分页） |
| `/api/messages` | POST | 发送私信 |
| `/api/messages/{id}/read` | PUT | 标记消息已读 |
| `/api/messages/unread-count` | GET | 获取未读消息数 |

**前端页面**:
- `views/portal/MessageInbox.vue` - 私信收件箱
- `views/portal/MessageChat.vue` - 聊天界面
- 支持富文本编辑器（图片上传、代码高亮、表情）

**业务规则**:
- 私信内容敏感词过滤
- 发送私信时创建或更新会话
- 接收者收到新私信通知

---

### 3.2 社交分享优化

**描述**: Open Graph 标签、各平台分享适配。

**实现内容**:

**Open Graph 标签** (文章页面):
```html
<meta property="og:title" content="文章标题">
<meta property="og:description" content="文章摘要">
<meta property="og:image" content="封面图片URL">
<meta property="og:url" content="文章URL">
<meta property="og:type" content="article">
<meta property="og:site_name" content="博客名称">
```

**分享功能** (已有 `SharePanel.vue`，需确认和完善):
- 复制链接
- 微信二维码分享
- 微博分享
- Twitter 分享

**前端实现**:
- 在文章详情页添加分享按钮
- 生成各平台分享链接
- 微信分享生成二维码

---

### 3.3 推荐阅读

**描述**: 基于标签或阅读历史推荐相关文章。

**推荐算法** (简单版):
1. 基于标签相似度：查找相同标签最多的文章
2. 基于分类相同：同分类下的其他文章
3. 热门文章补充

**后端 API**:
| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/portal/articles/{id}/related` | GET | 获取相关推荐文章 |

**后端实现**:
```java
// 推荐逻辑
1. 获取当前文章的所有标签
2. 查询包含这些标签的其他文章，按标签重合数排序
3. 如果数量不足，补充同分类文章
4. 如果仍不足，补充热门文章
5. 最多返回 5 篇
```

**前端位置**:
- 文章详情页底部
- 侧边栏（PC端）

**缓存策略**:
- 推荐结果缓存 10 分钟
- Key: `article:related:{articleId}`

---

## 第四阶段：编辑器增强

### 4.1 图片管理

**描述**: 编辑器内图片上传、图库管理。

**数据库**: 新建 `media` 表
```sql
CREATE TABLE `media` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `filename` VARCHAR(255) NOT NULL COMMENT '文件名',
    `original_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
    `file_path` VARCHAR(500) NOT NULL COMMENT '存储路径',
    `file_url` VARCHAR(500) NOT NULL COMMENT '访问URL',
    `file_size` BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
    `file_type` VARCHAR(50) DEFAULT NULL COMMENT '文件类型',
    `uploader_id` BIGINT DEFAULT NULL COMMENT '上传者ID',
    `use_count` INT DEFAULT 0 COMMENT '引用次数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_uploader` (`uploader_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='媒体文件表';
```

**后端 API**:
| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/admin/media/upload` | POST | 上传图片 |
| `/api/admin/media` | GET | 获取媒体列表 |
| `/api/admin/media/{id}` | DELETE | 删除媒体 |

**前端实现**:
- 文章编辑页集成图库选择器
- 支持拖拽上传
- 支持图片预览和删除
- 图片插入到 Markdown 光标位置

---

### 4.2 文章模板

**描述**: 预设文章模板，快速创建特定类型文章。

**数据库**: 新建 `article_template` 表
```sql
CREATE TABLE `article_template` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL COMMENT '模板名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '模板描述',
    `content` LONGTEXT NOT NULL COMMENT '模板内容(Markdown)',
    `category_id` BIGINT DEFAULT NULL COMMENT '默认分类',
    `tags` VARCHAR(255) DEFAULT NULL COMMENT '默认标签(JSON数组)',
    `is_default` TINYINT DEFAULT 0 COMMENT '是否默认模板',
    `status` TINYINT DEFAULT 1 COMMENT '状态',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章模板表';
```

**预设模板**:
- 技术教程：标题、简介、环境说明、步骤、总结
- 问题解决：问题描述、分析、解决方案、参考
- 学习笔记：主题、要点、代码示例、总结
- 生活随笔：自由格式

**后端 API**:
| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/admin/templates` | GET | 获取模板列表 |
| `/api/admin/templates` | POST | 创建模板 |
| `/api/admin/templates/{id}` | PUT | 更新模板 |
| `/api/admin/templates/{id}` | DELETE | 删除模板 |

**前端实现**:
- 新建文章时选择模板
- 模板管理页面（CRUD）

---

### 4.3 版本历史

**描述**: 文章修订历史，可回退到之前的版本。

**数据库**: 新建 `article_revision` 表
```sql
CREATE TABLE `article_revision` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `article_id` BIGINT NOT NULL COMMENT '文章ID',
    `version` INT NOT NULL COMMENT '版本号',
    `title` VARCHAR(200) NOT NULL COMMENT '标题快照',
    `content` LONGTEXT NOT NULL COMMENT '内容快照',
    `summary` VARCHAR(500) DEFAULT NULL COMMENT '摘要快照',
    `editor_id` BIGINT NOT NULL COMMENT '编辑者ID',
    `change_note` VARCHAR(200) DEFAULT NULL COMMENT '修改说明',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_article_version` (`article_id`, `version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章版本历史表';
```

**后端 API**:
| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/admin/articles/{id}/revisions` | GET | 获取文章版本历史 |
| `/api/admin/articles/{id}/revisions/{version}` | GET | 获取特定版本详情 |
| `/api/admin/articles/{id}/revisions/{version}/restore` | POST | 回退到特定版本 |

**后端实现**:
- 每次文章更新前保存当前版本快照
- 版本号递增
- 保留最近 20 个版本（可配置）

**前端实现**:
- 文章编辑页添加"历史版本"按钮
- 版本列表：版本号、修改时间、修改说明、操作
- 版本对比（可选）：显示两个版本的差异
- 回退操作需确认

---

## 第五阶段：高级功能

### 5.1 知识图谱

**描述**: 可视化展示标签之间的关系、文章关联。

**实现方案**:
- 使用力导向图（Force-Directed Graph）
- 节点：标签，大小表示文章数量
- 边：标签共现关系（同一文章有多个标签时建立关联）
- 交互：点击标签跳转到标签文章列表

**前端技术选型**:
- D3.js 或 ECharts 图表库
- 推荐使用 ECharts（已在 Vue 生态中有良好支持）

**后端 API**:
| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/portal/knowledge-graph` | GET | 获取知识图谱数据 |

**后端数据结构**:
```json
{
  "nodes": [
    { "id": 1, "name": "Java", "count": 15 },
    { "id": 2, "name": "Spring Boot", "count": 10 }
  ],
  "links": [
    { "source": 1, "target": 2, "weight": 8 }
  ]
}
```

**前端页面**:
- `views/portal/KnowledgeGraph.vue` - 知识图谱页面
- 全屏展示图谱
- 支持缩放、拖拽
- 搜索标签高亮

---

### 5.2 访问统计

**描述**: 详细的访问日志、PV/UV 统计、热门文章排行。

**数据库**: 已有 `visit_log` 表，可能需要扩展统计表：
```sql
-- 每日统计表
CREATE TABLE `daily_statistics` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `date` DATE NOT NULL COMMENT '日期',
    `pv` INT DEFAULT 0 COMMENT '页面访问量',
    `uv` INT DEFAULT 0 COMMENT '独立访客数',
    `ip_count` INT DEFAULT 0 COMMENT '独立IP数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_date` (`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日统计表';

-- 文章统计表（按天）
CREATE TABLE `article_daily_stats` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `article_id` BIGINT NOT NULL COMMENT '文章ID',
    `date` DATE NOT NULL COMMENT '日期',
    `view_count` INT DEFAULT 0 COMMENT '浏览量',
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    `comment_count` INT DEFAULT 0 COMMENT '评论数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_date` (`article_id`, `date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章每日统计表';
```

**后端 API**:
| 接口 | 方法 | 描述 |
|------|------|------|
| `/api/admin/statistics/overview` | GET | 总览数据（今日PV/UV、总文章数等） |
| `/api/admin/statistics/trend` | GET | 趋势数据（按天/周/月） |
| `/api/admin/statistics/hot-articles` | GET | 热门文章排行 |
| `/api/admin/statistics/visit-logs` | GET | 访问日志列表 |

**后端实现**:
- 定时任务：每日凌晨统计前一天数据
- 访问拦截器：记录每次请求到 visit_log
- UV 统计基于用户ID或 Cookie

**前端页面**:
- `views/admin/Statistics.vue` - 统计页面
- 数据卡片：今日PV/UV、总文章数、总评论数
- 趋势图表：近7天/30天访问趋势
- 热门文章排行榜
- 访问日志列表

---

## 技术要点总结

### 数据库变更
| 表名 | 操作 | 阶段 |
|------|------|------|
| `message` | 已存在，前端缺失 | 一 |
| `sys_user` | 添加 bio, website 字段 | 二 |
| `sensitive_word` | 新建 | 一 |
| `conversation` | 新建 | 三 |
| `private_message` | 新建 | 三 |
| `media` | 新建 | 四 |
| `article_template` | 新建 | 四 |
| `article_revision` | 新建 | 四 |
| `daily_statistics` | 新建 | 五 |
| `article_daily_stats` | 新建 | 五 |

### 后端新增模块
- `SensitiveWordService` - 敏感词服务
- `CacheService` - 缓存服务
- `MessageService` - 私信服务
- `MediaService` - 媒体管理服务
- `TemplateService` - 模板服务
- `RevisionService` - 版本历史服务
- `StatisticsService` - 统计服务

### 前端新增页面
- `MessageBoard.vue` - 留言板
- `UserManage.vue` - 用户管理（后台）
- `SensitiveWordManage.vue` - 敏感词管理（后台）
- `UserProfilePublic.vue` - 用户公开主页
- `MessageInbox.vue` - 私信收件箱
- `MessageChat.vue` - 私信聊天
- `Statistics.vue` - 统计页面（后台）
- `KnowledgeGraph.vue` - 知识图谱
- `TemplateManage.vue` - 模板管理（后台）

### Redis 缓存 Key 设计
```
article:{id}                    - 文章详情
articles:page:{page}            - 文章列表分页
articles:hot                    - 热门文章
categories                      - 分类列表
tags                            - 标签列表
sys:config                      - 系统配置
sensitive:words                 - 敏感词库
article:related:{id}            - 相关推荐
user:profile:{id}               - 用户资料
```

---

## 风险与注意事项

1. **私信实时性**: 当前架构不支持 WebSocket，私信需要手动刷新。如需实时推送，后续可考虑引入 WebSocket 或轮询机制。

2. **缓存一致性**: 文章更新时需及时清除相关缓存，避免脏数据。

3. **版本历史存储**: 文章内容可能较长，版本历史表会快速增长。建议限制保留版本数量，或定期归档旧版本。

4. **知识图谱性能**: 标签和文章数量增长后，图谱计算可能变慢。考虑缓存图谱数据，定时刷新。

5. **敏感词更新**: DFA 树需要重建，高并发下可能有性能影响。考虑异步更新机制。

---

## 后续扩展建议

完成以上 16 个功能后，可考虑：
- 搜索引擎集成（Elasticsearch）提升搜索体验
- CDN 集成加速静态资源访问
- 邮件订阅功能
- PWA 支持
- 多语言国际化
