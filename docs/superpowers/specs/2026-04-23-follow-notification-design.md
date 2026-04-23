# 用户关注与站内通知系统设计文档

## 概述

为博客系统添加用户关注系统和站内通知系统，实现用户社交互动能力。

## 一、用户关注系统

### 1.1 数据模型

**新增表：user_follow**

```sql
CREATE TABLE `user_follow` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `follower_id` BIGINT NOT NULL COMMENT '关注者ID',
    `following_id` BIGINT NOT NULL COMMENT '被关注者ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_follower_following` (`follower_id`, `following_id`),
    KEY `idx_follower` (`follower_id`),
    KEY `idx_following` (`following_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户关注表';
```

**修改表：sys_user**

新增冗余字段用于快速展示关注/粉丝数：
```sql
ALTER TABLE `sys_user` ADD COLUMN `follower_count` INT DEFAULT 0 COMMENT '粉丝数';
ALTER TABLE `sys_user` ADD COLUMN `following_count` INT DEFAULT 0 COMMENT '关注数';
```

### 1.2 核心功能

1. **关注/取关作者** - 在用户中心、文章详情页可关注作者
2. **关注列表** - 个人中心展示「我的关注」和「我的粉丝」
3. **关注动态** - 首页可筛选「只看关注的作者」的文章列表

### 1.3 API 设计

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | /api/portal/follow/{userId} | 关注用户 | 登录用户 |
| DELETE | /api/portal/follow/{userId} | 取关用户 | 登录用户 |
| GET | /api/portal/following/{userId} | 获取关注列表 | 公开 |
| GET | /api/portal/followers/{userId} | 获取粉丝列表 | 公开 |
| GET | /api/portal/follow/check/{userId} | 检查是否已关注 | 登录用户 |

### 1.4 请求/响应示例

**关注用户：**
```
POST /api/portal/follow/123
Response: { "code": 200, "message": "关注成功" }
```

**获取关注列表：**
```
GET /api/portal/following/123?page=1&size=10
Response: {
  "code": 200,
  "data": {
    "total": 25,
    "list": [
      { "id": 456, "username": "author1", "nickname": "作者一", "avatar": "...", "followTime": "2026-04-20" }
    ]
  }
}
```

## 二、站内通知系统

### 2.1 数据模型

**新增表：notification**

```sql
CREATE TABLE `notification` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '接收通知的用户ID',
    `type` TINYINT NOT NULL COMMENT '通知类型 1:关注动态 2:评论通知 3:回复通知 4:系统公告',
    `title` VARCHAR(200) NOT NULL COMMENT '通知标题',
    `content` VARCHAR(500) DEFAULT NULL COMMENT '通知内容',
    `related_id` BIGINT DEFAULT NULL COMMENT '关联ID(文章ID/评论ID/公告ID)',
    `sender_id` BIGINT DEFAULT NULL COMMENT '发送者ID(系统公告为NULL)',
    `is_read` TINYINT DEFAULT 0 COMMENT '是否已读 0:未读 1:已读',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_read` (`user_id`, `is_read`),
    KEY `idx_user_time` (`user_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';
```

**新增表：announcement**

```sql
CREATE TABLE `announcement` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title` VARCHAR(200) NOT NULL COMMENT '公告标题',
    `content` TEXT NOT NULL COMMENT '公告内容',
    `status` TINYINT DEFAULT 1 COMMENT '状态 0:草稿 1:已发布',
    `publish_time` DATETIME DEFAULT NULL COMMENT '发布时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_status_time` (`status`, `publish_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统公告表';
```

### 2.2 通知类型说明

| 类型值 | 类型名称 | 触发场景 | 通知内容示例 |
|--------|----------|----------|--------------|
| 1 | 关注动态 | 关注的作者发布新文章 | "你关注的【作者名】发布了新文章《文章标题》" |
| 2 | 评论通知 | 我的文章被评论 | "【用户名】评论了你的文章《文章标题》" |
| 3 | 回复通知 | 我的评论被回复 | "【用户名】回复了你的评论" |
| 4 | 系统公告 | 管理员发布/更新公告 | 公告标题 |

### 2.3 核心功能

1. **通知中心** - 顶部导航显示未读数角标，点击进入通知列表
2. **通知列表** - 按时间倒序展示所有通知，支持已读/未读状态
3. **一键已读** - 标记单条已读或全部已读
4. **公告管理** - 管理员可在后台发布/编辑/删除系统公告

### 2.4 API 设计

**用户端：**

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/portal/notifications | 获取通知列表（支持type筛选） |
| PUT | /api/portal/notifications/{id}/read | 标记单条已读 |
| PUT | /api/portal/notifications/read-all | 全部标记已读 |
| GET | /api/portal/notifications/unread-count | 获取未读数量 |

**管理端：**

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/admin/announcements | 公告列表 |
| POST | /api/admin/announcements | 发布公告 |
| PUT | /api/admin/announcements/{id} | 编辑公告 |
| DELETE | /api/admin/announcements/{id} | 删除公告 |

### 2.5 通知生成逻辑

**关注动态通知：**
- 文章发布时，查询所有关注该作者的粉丝
- 异步为每个粉丝创建 type=1 的通知
- 批量插入，避免阻塞发布流程

**评论通知：**
- 评论审核通过后，通知文章作者（type=2）
- 如果是回复评论，额外通知被回复者（type=3）

**系统公告通知：**
- 公告发布时，为所有用户创建 type=4 的通知
- 考虑用户量大时，可采用延迟加载策略：公告通知不预创建，用户查询时动态合并

## 三、前端页面

### 3.1 用户中心扩展

在 `UserCenter.vue` 新增 Tab：
- 「关注」- 展示关注列表
- 「粉丝」- 展示粉丝列表

### 3.2 文章详情页

在 `ArticleDetail.vue` 作者信息区添加：
- 关注按钮（未关注显示「+关注」，已关注显示「已关注」）
- 点击切换关注状态

### 3.3 首页筛选

在 `Home.vue` 添加筛选选项：
- 「全部文章」/「关注的作者」切换

### 3.4 通知中心

新建 `Notification.vue` 页面：
- 顶部 Tab 按类型筛选（全部/关注/评论/回复/公告）
- 列表展示通知，点击跳转对应文章
- 未读项高亮显示
- 提供「全部已读」按钮

### 3.5 顶部导航

修改 `Layout.vue`：
- 添加通知图标，显示未读数角标
- 点击进入通知中心

### 3.6 后台管理

新建 `AnnouncementManage.vue`：
- 公告列表（支持编辑、删除）
- 新建/编辑公告表单

## 四、技术要点

### 4.1 关注系统

- 关注/取关操作幂等性：利用唯一索引，重复关注自动忽略
- 关注数/粉丝数更新：关注成功时双方计数+1，取关时-1
- 首页筛选关注作者：JOIN user_follow 表过滤

### 4.2 通知系统

- 异步生成通知：使用 Spring @Async 或消息队列
- 未读数查询：`SELECT COUNT(*) FROM notification WHERE user_id = ? AND is_read = 0`
- 通知点击跳转：根据 type 和 related_id 构建跳转路径
- 系统公告特殊处理：用户量大时，公告通知采用虚拟通知方式，不预创建记录

### 4.3 性能考虑

- 关注表索引优化：follower_id、following_id 分别建索引
- 通知表索引优化：(user_id, is_read) 联合索引加速未读查询
- 用户计数字段：避免每次查询都 COUNT 关注数

## 五、实现顺序

1. 数据库表创建（user_follow、notification、announcement）
2. 后端关注系统实现（Entity、Mapper、Service、Controller）
3. 后端通知系统实现（Entity、Mapper、Service、Controller）
4. 前端关注功能（用户中心、文章详情页、首页筛选）
5. 前端通知功能（通知中心、顶部导航）
6. 后台公告管理
7. 测试验证
