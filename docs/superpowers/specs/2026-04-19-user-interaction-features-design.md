# 博客系统用户交互功能增强设计

## 概述

为技术博客系统添加三项用户交互功能：我的收藏列表、阅读历史记录、文章分享功能。采用渐进式实现策略，按功能独立迭代。

## 目标用户

个人技术博客，用于展示技术文章和个人作品，吸引潜在雇主或客户。

---

## 功能一：我的收藏列表

### 1.1 功能描述

用户在阅读文章时可以收藏文章，收藏列表让用户在个人中心查看所有已收藏的文章。

### 1.2 现有基础

- 数据库表 `user_action` 已支持收藏操作（`action_type = 2`）
- API `POST /api/portal/article/{id}/favorite` 已实现（切换收藏状态）

### 1.3 后端设计

#### 新增 API

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/portal/favorites` | 获取当前用户的收藏列表（分页） |

#### 请求参数

| 参数 | 类型 | 必填 | 描述 |
|------|------|------|------|
| pageNum | int | 否 | 页码，默认 1 |
| pageSize | int | 否 | 每页数量，默认 10 |
| keyword | string | 否 | 文章标题关键词搜索 |

#### 响应结构

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "articleId": 123,
        "title": "Spring Boot 最佳实践",
        "summary": "本文介绍 Spring Boot 开发中的最佳实践...",
        "coverImage": "https://example.com/cover.jpg",
        "authorName": "管理员",
        "categoryName": "技术分享",
        "viewCount": 100,
        "likeCount": 10,
        "favoriteTime": "2024-01-15 10:30:00"
      }
    ],
    "total": 50,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

#### 实现要点

1. 在 `PortalController` 或新建 `FavoriteController` 添加接口
2. 新建 `FavoriteService` 和 `FavoriteServiceImpl`
3. 关联查询 `user_action`、`article`、`category` 表
4. 按收藏时间倒序排列
5. 过滤已删除的文章

### 1.4 前端设计

#### 页面位置

个人中心 `/user` 页面新增「我的收藏」标签页，与「个人资料」「修改密码」并列显示。

#### 路由配置

```javascript
{
  path: 'user',
  name: 'UserCenter',
  component: () => import('@/views/portal/UserCenter.vue'),
  meta: { title: '个人中心', requiresAuth: true },
  children: [
    { path: '', redirect: 'profile' },
    { path: 'profile', name: 'UserProfile', component: UserProfile },
    { path: 'favorites', name: 'UserFavorites', component: UserFavorites },
    { path: 'history', name: 'UserHistory', component: UserHistory }
  ]
}
```

#### 功能特性

- 卡片式文章列表，显示封面、标题、摘要、收藏时间
- 支持按标题关键词搜索
- 支持分页加载
- 取消收藏按钮（点击后确认，确认后移除）
- 点击卡片跳转到文章详情
- 空状态展示引导文案

#### 新增文件

```
src/
├── api/
│   └── favorite.js          # 收藏相关 API
└── views/
    └── portal/
        └── UserCenter.vue   # 修改：改为标签页布局
```

---

## 功能二：阅读历史记录

### 2.1 功能描述

记录用户浏览过的文章，方便用户回顾和继续阅读。

### 2.2 数据库设计

新建 `reading_history` 表：

```sql
CREATE TABLE `reading_history` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `article_id` BIGINT NOT NULL COMMENT '文章ID',
    `read_duration` INT DEFAULT 0 COMMENT '阅读时长(秒)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '首次阅读时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后阅读时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_article` (`user_id`, `article_id`),
    KEY `idx_user_time` (`user_id`, `update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='阅读历史表';
```

**设计说明：**
- `uk_user_article`：同一用户对同一文章只保留一条记录
- `update_time`：每次阅读更新，用于排序
- `read_duration`：预留字段，可用于统计阅读时长

### 2.3 后端设计

#### 新增实体类

```java
// ReadingHistory.java
@TableName("reading_history")
public class ReadingHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long articleId;
    private Integer readDuration;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

#### 新增 API

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/portal/history/{articleId}` | 记录阅读 |
| GET | `/api/portal/history` | 获取阅读历史列表 |
| DELETE | `/api/portal/history/{articleId}` | 删除单条记录 |
| DELETE | `/api/portal/history` | 清空所有历史 |

#### GET 响应结构

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "articleId": 123,
        "title": "Vue 3 组合式 API 实践",
        "coverImage": "https://example.com/cover.jpg",
        "lastReadTime": "2024-01-15 10:30:00"
      }
    ],
    "total": 100,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

#### 实现要点

1. 进入文章详情页时调用记录接口
2. 使用 `INSERT ... ON DUPLICATE KEY UPDATE` 实现幂等
3. 按最后阅读时间倒序排列
4. 删除文章时同步清理阅读历史

### 2.4 前端设计

#### 页面位置

个人中心新增「阅读历史」标签页。

#### 功能特性

- 按最后阅读时间倒序排列
- 显示文章封面、标题、阅读时间
- 支持删除单条记录
- 支持清空全部（需二次确认）
- 空状态展示引导文案

#### 交互细节

- 进入文章详情页时自动记录
- 仅登录用户记录历史
- 清空操作弹出确认对话框

#### 新增文件

```
src/
├── api/
│   └── history.js           # 阅读历史相关 API
└── views/
    └── portal/
        └── UserHistory.vue  # 阅读历史页面组件
```

---

## 功能三：文章分享功能

### 3.1 功能描述

为每篇文章生成分享链接和二维码，方便用户通过微信、微博等平台传播。

### 3.2 后端设计

**方案：** 无需后端改动。分享链接即为当前文章 URL，二维码由前端生成。

如需未来扩展分享统计，可预留接口：
- `GET /api/portal/article/{id}/share-stats` — 获取分享统计数据

### 3.3 前端设计

#### 位置

文章详情页底部操作栏，在「点赞」「收藏」按钮旁新增「分享」按钮。

#### 功能特性

1. **分享面板**：点击「分享」按钮弹出分享选项
   - 复制链接
   - 生成二维码
   - 分享到微博（跳转微博分享页）
   - 分享到微信（显示二维码图片供扫描）

2. **二维码生成：**
   - 使用 `qrcode-generator` 或 `qrcodejs2` 库
   - 显示文章链接的二维码
   - 支持下载二维码图片（可选）

3. **反馈提示：**
   - 复制成功显示 Toast 提示

#### 交互细节

- 移动端：分享面板从底部滑出（使用 Vant 的 `ActionSheet` 或 `Popup`）
- PC 端：分享面板下拉显示
- 二维码弹窗居中显示，点击空白处关闭

#### 视觉设计

- 分享图标与现有操作按钮风格一致
- 二维码外围显示文章标题和站点 Logo
- 分享选项使用图标 + 文字组合

#### 新增依赖

```bash
pnpm add qrcode-generator
# 或
pnpm add qrcodejs2
```

#### 修改文件

```
src/
└── views/
    └── portal/
        └── ArticleDetail.vue  # 添加分享按钮和分享面板
```

---

## 实现顺序

按照渐进式实现策略，建议按以下顺序迭代：

| 阶段 | 功能 | 预估工作量 | 依赖 |
|------|------|------------|------|
| 第一阶段 | 我的收藏列表 | 1-2 小时 | 无（复用现有表和 API） |
| 第二阶段 | 阅读历史记录 | 2-3 小时 | 新建数据库表 |
| 第三阶段 | 文章分享功能 | 1-2 小时 | 无（纯前端实现） |

**总预估工作量：** 4-7 小时

---

## 风险与注意事项

1. **性能考虑**
   - 收藏列表和阅读历史都需要分页，避免一次性加载过多数据
   - 阅读历史记录写入频繁，考虑异步处理

2. **用户体验**
   - 未登录用户点击收藏/分享时，提示登录并跳转
   - 清空操作需二次确认，防止误操作

3. **数据一致性**
   - 文章删除时，同步清理相关的收藏记录和阅读历史
   - 用户注销时，清理用户行为数据

4. **移动端适配**
   - 分享面板在移动端使用底部弹出样式
   - 标签页在小屏幕下考虑滚动或下拉切换

---

## 验收标准

### 我的收藏列表

- [ ] 用户可在个人中心查看收藏的文章列表
- [ ] 支持按标题搜索收藏的文章
- [ ] 可取消收藏，取消后从列表移除
- [ ] 点击文章跳转到详情页

### 阅读历史记录

- [ ] 登录用户浏览文章时自动记录阅读历史
- [ ] 用户可在个人中心查看阅读历史
- [ ] 支持删除单条记录和清空全部
- [ ] 文章删除后阅读历史同步清理

### 文章分享功能

- [ ] 文章详情页显示分享按钮
- [ ] 点击分享可复制文章链接
- [ ] 可生成文章二维码供扫码分享
- [ ] 可跳转到微博分享页面
