---
title: 评论体验升级功能设计
date: 2026-05-01
category: feature
module: blog-server, blog-web
status: draft
---

# 评论体验升级功能设计

## 概述

本次升级旨在提升博客社区互动体验，主要围绕评论系统进行增强。

### 核心功能

| 功能 | 描述 |
|------|------|
| 楼中楼回复 | 支持2层嵌套的回复展示，扁平化UI |
| 评论点赞 | 用户可为评论点赞，查看点赞列表 |
| 热评置顶 | 按点赞数自动排序，高赞评论靠前 |
| @提及 | 评论和文章内容中可@用户 |
| Emoji表情 | 内置Emoji选择器 |
| 多种排序 | 支持最热、最新、最早三种排序 |

### 与现有系统的关系

当前评论系统已有：
- 评论表 `comment`（支持 `parent_id` 和 `reply_id`）
- 评论状态管理（待审核、已通过、已拒绝）
- 游客评论支持

本次升级在此基础上扩展，不破坏现有功能。

---

## 数据库设计

### 1. 评论点赞表（新增）

```sql
-- 评论点赞表
CREATE TABLE `comment_like` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `comment_id` BIGINT NOT NULL COMMENT '评论ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_comment_user` (`comment_id`, `user_id`),
    KEY `idx_comment_id` (`comment_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论点赞表';
```

### 2. 评论表字段扩展

```sql
-- 为评论表添加点赞数字段
ALTER TABLE `comment` ADD COLUMN `like_count` INT DEFAULT 0 COMMENT '点赞数' AFTER `status`;

-- 添加索引优化排序查询
ALTER TABLE `comment` ADD INDEX `idx_article_like` (`article_id`, `like_count` DESC);
```

### 3. @提及记录表（新增）

```sql
-- @提及记录表
CREATE TABLE `mention` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `source_type` VARCHAR(20) NOT NULL COMMENT '来源类型: COMMENT/ARTICLE',
    `source_id` BIGINT NOT NULL COMMENT '来源ID',
    `mentioned_user_id` BIGINT NOT NULL COMMENT '被@的用户ID',
    `mentioner_id` BIGINT NOT NULL COMMENT '@别人的用户ID',
    `content` VARCHAR(500) DEFAULT NULL COMMENT '@时的内容片段',
    `is_notified` TINYINT DEFAULT 0 COMMENT '是否已发送通知',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_source` (`source_type`, `source_id`),
    KEY `idx_mentioned_user` (`mentioned_user_id`, `is_notified`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='@提及记录表';
```

### 4. 通知表扩展

现有的 `notification` 表已支持多种类型，扩展增加 `type=5` 表示"@提及"通知。

```sql
-- 扩展通知类型（通过注释说明，无需SQL修改）
-- type: 1:关注动态 2:评论通知 3:回复通知 4:系统公告 5:@提及
```

---

## API 设计

### 评论相关接口

#### 1. 获取评论列表（增强）

```
GET /api/portal/articles/{articleId}/comments
```

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页条数，默认10 |
| sortBy | string | 否 | 排序方式：hot(最热)/newest(最新)/oldest(最早)，默认hot |

**响应结构：**

```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 1,
        "content": "这篇文章写得很好！👍",
        "userId": 10,
        "nickname": "张三",
        "avatar": "https://...",
        "likeCount": 128,
        "isLiked": false,
        "createTime": "2026-05-01 10:00:00",
        "replies": [
          {
            "id": 2,
            "content": "确实，第三章的分析很到位",
            "userId": 11,
            "nickname": "李四",
            "avatar": "https://...",
            "replyToUserId": 10,
            "replyToNickname": "张三",
            "likeCount": 32,
            "isLiked": true,
            "createTime": "2026-05-01 10:30:00"
          }
        ],
        "replyCount": 5
      }
    ],
    "total": 50,
    "hasMore": true
  }
}
```

#### 2. 创建评论/回复

```
POST /api/portal/comments
```

**请求体：**

```json
{
  "articleId": 1,
  "content": "感谢分享！@李四 你怎么看？",
  "parentId": 0,
  "replyId": null,
  "replyToUserId": null
}
```

**回复评论时：**

```json
{
  "articleId": 1,
  "content": "我也这么认为 👍",
  "parentId": 1,
  "replyId": 2,
  "replyToUserId": 11
}
```

**响应：**

```json
{
  "code": 200,
  "data": {
    "id": 3,
    "content": "我也这么认为 👍",
    "userId": 12,
    "nickname": "王五",
    "avatar": "https://...",
    "parentId": 1,
    "replyToUserId": 11,
    "replyToNickname": "李四",
    "likeCount": 0,
    "isLiked": false,
    "createTime": "2026-05-01 11:00:00"
  }
}
```

#### 3. 评论点赞

```
POST /api/portal/comments/{commentId}/like
```

**响应：**

```json
{
  "code": 200,
  "data": {
    "liked": true,
    "likeCount": 129
  }
}
```

#### 4. 取消点赞

```
DELETE /api/portal/comments/{commentId}/like
```

#### 5. 获取评论点赞列表

```
GET /api/portal/comments/{commentId}/likes
```

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页条数，默认20 |

**响应：**

```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "userId": 10,
        "nickname": "张三",
        "avatar": "https://..."
      },
      {
        "userId": 11,
        "nickname": "李四",
        "avatar": "https://..."
      }
    ],
    "total": 128
  }
}
```

#### 6. 获取回复列表（分页加载更多回复）

```
GET /api/portal/comments/{commentId}/replies
```

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| page | int | 否 | 页码，默认1 |
| size | int | 否 | 每页条数，默认10 |
| sortBy | string | 否 | 排序方式，同评论列表 |

---

### @提及相关接口

#### 1. 搜索用户（用于@选择器）

```
GET /api/portal/users/search
```

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | string | 是 | 搜索关键词（用户名/昵称） |
| size | int | 否 | 返回数量，默认10 |

**响应：**

```json
{
  "code": 200,
  "data": [
    {
      "userId": 10,
      "nickname": "张三",
      "avatar": "https://..."
    },
    {
      "userId": 11,
      "nickname": "李四",
      "avatar": "https://..."
    }
  ]
}
```

#### 2. 获取@提及通知

```
GET /api/portal/notifications/mentions
```

（复用现有通知接口，通过 type=5 筛选）

---

## 后端架构

### 新增文件

```
blog-server/src/main/java/com/blog/
├── domain/
│   ├── entity/
│   │   ├── CommentLike.java          # 评论点赞实体
│   │   └── Mention.java              # @提及实体
│   ├── dto/
│   │   ├── CommentCreateRequest.java # 评论创建请求（已存在，扩展）
│   │   ├── CommentVO.java            # 评论视图对象
│   │   ├── CommentReplyVO.java       # 回复视图对象
│   │   └── CommentLikeVO.java        # 点赞用户视图对象
│   └── enums/
│       ├── CommentSortType.java      # 评论排序类型枚举
│       └── MentionSourceType.java    # @提及来源类型枚举
├── repository/
│   ├── CommentLikeMapper.java        # 评论点赞Mapper
│   └── MentionMapper.java            # @提及Mapper
├── service/
│   ├── CommentService.java           # 评论服务（扩展）
│   ├── CommentLikeService.java       # 评论点赞服务
│   ├── MentionService.java           # @提及服务
│   └── impl/
│       ├── CommentServiceImpl.java
│       ├── CommentLikeServiceImpl.java
│       └── MentionServiceImpl.java
└── controller/
    └── portal/
        └── CommentController.java     # 评论Controller（扩展）
```

### 核心服务方法

#### CommentService 扩展

```java
public interface CommentService {

    // 获取评论列表（支持排序）
    PageResult<CommentVO> listComments(Long articleId, CommentSortType sortType, int page, int size);

    // 创建评论（处理@提及）
    CommentVO createComment(CommentCreateRequest request);

    // 获取回复列表
    PageResult<CommentReplyVO> listReplies(Long commentId, CommentSortType sortType, int page, int size);

    // 获取评论详情
    CommentVO getCommentDetail(Long commentId);
}
```

#### CommentLikeService

```java
public interface CommentLikeService {

    // 点赞/取消点赞（返回最新状态）
    LikeResult toggleLike(Long commentId, Long userId);

    // 获取点赞列表
    PageResult<CommentLikeVO> listLikes(Long commentId, int page, int size);

    // 检查是否已点赞
    boolean isLiked(Long commentId, Long userId);

    // 批量检查点赞状态
    Map<Long, Boolean> batchCheckLiked(List<Long> commentIds, Long userId);
}
```

#### MentionService

```java
public interface MentionService {

    // 解析内容中的@用户
    List<Long> parseMentions(String content);

    // 创建@提及记录并发送通知
    void createMentions(MentionSourceType sourceType, Long sourceId, Long mentionerId, String content);

    // 获取用户的@提及列表
    PageResult<MentionVO> listUserMentions(Long userId, int page, int size);
}
```

### 枚举定义

```java
// 评论排序类型
public enum CommentSortType {
    HOT("hot", "按点赞数排序"),
    NEWEST("newest", "按时间倒序"),
    OLDEST("oldest", "按时间正序");

    private final String code;
    private final String desc;
}

// @提及来源类型
public enum MentionSourceType {
    COMMENT("COMMENT", "评论"),
    ARTICLE("ARTICLE", "文章");

    private final String code;
    private final String desc;
}
```

---

## 核心业务逻辑

### 1. 评论列表排序

```java
// CommentServiceImpl.java
public PageResult<CommentVO> listComments(Long articleId, CommentSortType sortType, int page, int size) {
    // 构建排序条件
    LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
    wrapper.eq(Comment::getArticleId, articleId)
           .eq(Comment::getStatus, CommentStatus.APPROVED.getCode())
           .eq(Comment::getParentId, 0); // 只查顶级评论

    switch (sortType) {
        case HOT:
            wrapper.orderByDesc(Comment::getLikeCount);
            break;
        case NEWEST:
            wrapper.orderByDesc(Comment::getCreateTime);
            break;
        case OLDEST:
            wrapper.orderByAsc(Comment::getCreateTime);
            break;
    }

    // 分页查询
    Page<Comment> pageResult = commentMapper.selectPage(new Page<>(page, size), wrapper);

    // 转换为VO，加载前N条回复
    List<CommentVO> vos = pageResult.getRecords().stream()
        .map(this::convertToVO)
        .peek(vo -> vo.setReplies(loadTopReplies(vo.getId(), 3)))
        .collect(Collectors.toList());

    return new PageResult<>(vos, pageResult.getTotal());
}
```

### 2. 评论点赞（幂等）

```java
// CommentLikeServiceImpl.java
@Transactional
public LikeResult toggleLike(Long commentId, Long userId) {
    // 查询是否已点赞
    CommentLike existing = commentLikeMapper.selectOne(
        new LambdaQueryWrapper<CommentLike>()
            .eq(CommentLike::getCommentId, commentId)
            .eq(CommentLike::getUserId, userId)
    );

    boolean liked;
    int likeCount;

    if (existing != null) {
        // 已点赞，取消
        commentLikeMapper.deleteById(existing.getId());
        commentMapper.decrementLikeCount(commentId);
        liked = false;
    } else {
        // 未点赞，添加
        CommentLike like = new CommentLike();
        like.setCommentId(commentId);
        like.setUserId(userId);
        commentLikeMapper.insert(like);
        commentMapper.incrementLikeCount(commentId);
        liked = true;
    }

    likeCount = commentMapper.selectLikeCount(commentId);
    return new LikeResult(liked, likeCount);
}
```

### 3. @提及解析与通知

```java
// MentionServiceImpl.java
private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\S+)\\s?");

public List<Long> parseMentions(String content) {
    Set<Long> mentionedUserIds = new HashSet<>();
    Matcher matcher = MENTION_PATTERN.matcher(content);

    while (matcher.find()) {
        String nickname = matcher.group(1);
        // 根据昵称查找用户
        SysUser user = userMapper.selectByNickname(nickname);
        if (user != null) {
            mentionedUserIds.add(user.getId());
        }
    }

    return new ArrayList<>(mentionedUserIds);
}

public void createMentions(MentionSourceType sourceType, Long sourceId,
                           Long mentionerId, String content) {
    List<Long> mentionedUserIds = parseMentions(content);

    for (Long userId : mentionedUserIds) {
        // 跳过@自己
        if (userId.equals(mentionerId)) continue;

        // 创建提及记录
        Mention mention = new Mention();
        mention.setSourceType(sourceType.getCode());
        mention.setSourceId(sourceId);
        mention.setMentionedUserId(userId);
        mention.setMentionerId(mentionerId);
        mention.setContent(extractContext(content, userId));
        mentionMapper.insert(mention);

        // 发送通知
        notificationService.sendMentionNotification(userId, mentionerId, sourceType, sourceId);
    }
}
```

---

## 前端架构

### 新增文件

```
blog-web/src/
├── api/
│   └── comment.js              # 评论API（扩展）
├── components/
│   ├── CommentList.vue         # 评论列表组件
│   ├── CommentItem.vue         # 单条评论组件
│   ├── CommentInput.vue        # 评论输入组件（含Emoji选择器）
│   ├── ReplyList.vue           # 回复列表组件
│   ├── LikeList.vue            # 点赞列表弹窗
│   └── EmojiPicker.vue         # Emoji选择器
├── stores/
│   └── comment.js              # 评论状态管理（可选）
└── views/
    └── portal/
        └── ArticleDetail.vue   # 文章详情页（集成评论）
```

### 组件设计

#### CommentList.vue

```vue
<template>
  <div class="comment-section">
    <!-- 排序选择器 -->
    <div class="sort-bar">
      <span class="label">排序：</span>
      <button
        v-for="option in sortOptions"
        :key="option.value"
        :class="['sort-btn', { active: sortBy === option.value }]"
        @click="changeSort(option.value)"
      >
        {{ option.label }}
      </button>
    </div>

    <!-- 评论列表 -->
    <div class="comment-list">
      <CommentItem
        v-for="comment in comments"
        :key="comment.id"
        :comment="comment"
        @reply="handleReply"
        @like="handleLike"
        @show-likes="showLikeList"
      />
    </div>

    <!-- 加载更多 -->
    <div v-if="hasMore" class="load-more">
      <button @click="loadMore">加载更多评论</button>
    </div>

    <!-- 评论输入 -->
    <CommentInput
      :article-id="articleId"
      :reply-to="replyTo"
      @submit="handleSubmit"
      @cancel-reply="replyTo = null"
    />
  </div>
</template>
```

#### CommentItem.vue

```vue
<template>
  <div class="comment-item">
    <img :src="comment.avatar" class="avatar" />
    <div class="content">
      <div class="header">
        <span class="nickname">{{ comment.nickname }}</span>
        <span class="time">{{ formatTime(comment.createTime) }}</span>
      </div>
      <div class="text" v-html="renderContent(comment.content)"></div>
      <div class="actions">
        <button class="action-btn" @click="$emit('like', comment)">
          <span :class="['icon', { liked: comment.isLiked }]">👍</span>
          <span class="count" @click.stop="$emit('show-likes', comment)">
            {{ comment.likeCount }}
          </span>
        </button>
        <button class="action-btn" @click="$emit('reply', comment)">
          回复
        </button>
      </div>

      <!-- 回复列表（扁平展示） -->
      <ReplyList
        v-if="comment.replies?.length"
        :replies="comment.replies"
        :reply-count="comment.replyCount"
        @reply="$emit('reply', $event)"
        @like="$emit('like', $event)"
      />
    </div>
  </div>
</template>
```

#### CommentInput.vue（含@选择器和Emoji）

```vue
<template>
  <div class="comment-input">
    <!-- 回复提示 -->
    <div v-if="replyTo" class="reply-hint">
      回复 <span class="nickname">@{{ replyTo.nickname }}</span>
      <button class="cancel-btn" @click="$emit('cancel-reply')">×</button>
    </div>

    <!-- 输入区域 -->
    <div class="input-wrapper">
      <textarea
        v-model="content"
        placeholder="写下你的评论..."
        @input="handleInput"
        @keydown="handleKeydown"
      />

      <!-- @用户选择器 -->
      <div v-if="showMentionPicker" class="mention-picker">
        <div
          v-for="user in matchedUsers"
          :key="user.userId"
          class="user-item"
          @click="selectMention(user)"
        >
          <img :src="user.avatar" class="avatar" />
          <span class="nickname">{{ user.nickname }}</span>
        </div>
      </div>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <button class="tool-btn" @click="showEmojiPicker = !showEmojiPicker">
        😊 表情
      </button>
      <button class="submit-btn" @click="submit" :disabled="!content.trim()">
        发表评论
      </button>
    </div>

    <!-- Emoji选择器 -->
    <EmojiPicker
      v-if="showEmojiPicker"
      @select="insertEmoji"
      @close="showEmojiPicker = false"
    />
  </div>
</template>
```

#### EmojiPicker.vue

```vue
<template>
  <div class="emoji-picker">
    <div class="emoji-grid">
      <button
        v-for="emoji in emojis"
        :key="emoji"
        class="emoji-btn"
        @click="$emit('select', emoji)"
      >
        {{ emoji }}
      </button>
    </div>
  </div>
</template>

<script setup>
// 常用Emoji分类
const emojis = [
  // 表情
  '😀', '😂', '🤣', '😊', '😇', '🙂', '😉', '😍',
  '🥰', '😘', '😋', '🤔', '😏', '😒', '😌', '😴',
  // 手势
  '👍', '👎', '👏', '🙌', '🤝', '✌️', '🤞', '👌',
  // 心形
  '❤️', '💔', '💕', '💖', '💗', '💙', '💚', '💛',
  // 其他
  '🎉', '🎊', '🎁', '🔥', '⭐', '✨', '💪', '🙏'
];
</script>
```

---

## UI 设计

### 评论区域整体布局

```
┌─────────────────────────────────────────────────────────────┐
│  评论区 (50条)                                               │
├─────────────────────────────────────────────────────────────┤
│  排序：[最热 ✓] [最新] [最早]                                │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ 👤 张三                                    2小时前   │   │
│  │                                                      │   │
│  │ 这篇文章写得很好！👍 关于性能优化的部分讲得很透彻     │   │
│  │                                                      │   │
│  │ 👍 128   💬 回复                                     │   │
│  │                                                      │   │
│  │  ┌─────────────────────────────────────────────┐    │   │
│  │  │ 👤 李四  回复 张三               1小时前     │    │   │
│  │  │ 确实，第三章的分析很到位                      │    │   │
│  │  │ 👍 32   💬 回复                              │    │   │
│  │  └─────────────────────────────────────────────┘    │   │
│  │                                                      │   │
│  │  ┌─────────────────────────────────────────────┐    │   │
│  │  │ 👤 王五  回复 李四               30分钟前    │    │   │
│  │  │ 我觉得可以再深入讲讲缓存策略                  │    │   │
│  │  │ 👍 8   💬 回复                               │    │   │
│  │  └─────────────────────────────────────────────┘    │   │
│  │                                                      │   │
│  │  ── 展开更多 5 条回复 ──                             │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │ 👤 赵六                                    3小时前   │   │
│  │                                                      │   │
│  │ @张三 同意！已收藏 📚                                 │   │
│  │                                                      │   │
│  │ 👍 45   💬 回复                                      │   │
│  └─────────────────────────────────────────────────────┘   │
│                                                             │
│  ────────── 加载更多评论 ──────────                         │
│                                                             │
├─────────────────────────────────────────────────────────────┤
│  💬 发表评论                                                │
│  ┌─────────────────────────────────────────────────────┐   │
│  │                                                      │   │
│  │  写下你的评论...                                     │   │
│  │                                                      │   │
│  └─────────────────────────────────────────────────────┘   │
│  [😊 表情]                          [发表评论]             │
└─────────────────────────────────────────────────────────────┘
```

### Emoji选择器

```
┌─────────────────────────────────────────┐
│  表情   手势   心形   其他               │
├─────────────────────────────────────────┤
│  😀  😂  🤣  😊  😇  🙂  😉  😍        │
│  🥰  😘  😋  🤔  😏  😒  😌  😴        │
│  👍  👎  👏  🙌  🤝  ✌️  🤞  👌        │
│  ❤️  💔  💕  💖  💗  💙  💚  💛        │
│  🎉  🎊  🎁  🔥  ⭐  ✨  💪  🙏        │
└─────────────────────────────────────────┘
```

### 点赞列表弹窗

```
┌─────────────────────────────────────────┐
│  点赞列表 (128人)                   [×] │
├─────────────────────────────────────────┤
│  👤 张三                                 │
│  👤 李四                                 │
│  👤 王五                                 │
│  👤 赵六                                 │
│  👤 钱七                                 │
│  ...                                     │
│  ────────── 加载更多 ──────────          │
└─────────────────────────────────────────┘
```

### @用户选择器

```
┌─────────────────────────────────────────┐
│  👤 张三                                 │
│  👤 李四                                 │
│  👤 王五                                 │
└─────────────────────────────────────────┘
```

用户输入 `@` 后自动弹出，显示匹配的用户列表。

---

## 交互流程

### 流程1：发表评论

```
用户输入评论内容
    ↓
（可选）点击😊打开Emoji选择器，选择表情
    ↓
（可选）输入@触发用户选择器，选择要@的用户
    ↓
点击"发表评论"
    ↓
前端验证内容不为空
    ↓
调用 POST /api/portal/comments
    ↓
后端创建评论，解析@提及，发送通知
    ↓
返回新评论数据
    ↓
前端追加到评论列表，清空输入框
```

### 流程2：回复评论

```
用户点击某条评论的"回复"按钮
    ↓
评论输入框显示"回复 @XXX"提示
    ↓
用户输入回复内容
    ↓
点击"发表评论"
    ↓
调用 POST /api/portal/comments（带 parentId 和 replyToUserId）
    ↓
返回新回复数据
    ↓
前端追加到对应评论的回复列表中
```

### 流程3：评论点赞

```
用户点击评论的👍按钮
    ↓
调用 POST /api/portal/comments/{id}/like
    ↓
后端判断：已点赞则取消，未点赞则添加
    ↓
返回最新状态 { liked: true/false, likeCount: 129 }
    ↓
前端更新评论的点赞状态和数量
```

### 流程4：查看点赞列表

```
用户点击评论的点赞数量
    ↓
调用 GET /api/portal/comments/{id}/likes
    ↓
弹出点赞列表弹窗，显示点赞用户
    ↓
（可选）滚动加载更多
```

---

## 与现有系统的集成

### 通知系统集成

@提及复用现有的通知表和通知服务：

```java
// NotificationService 扩展
public void sendMentionNotification(Long userId, Long mentionerId,
                                     MentionSourceType sourceType, Long sourceId) {
    Notification notification = new Notification();
    notification.setUserId(userId);
    notification.setType(NotificationType.MENTION.getCode());
    notification.setTitle("有人@了你");
    notification.setContent(mentioner.getNickname() + "在" + sourceType.getDesc() + "中提到了你");
    notification.setRelatedId(sourceId);
    notification.setSenderId(mentionerId);
    notificationMapper.insert(notification);
}
```

### 文章编辑器集成@提及

在文章编辑器中支持@用户：

```vue
<!-- ArticleEditor.vue -->
<template>
  <div class="editor">
    <!-- Markdown编辑器 -->
    <MarkdownEditor
      v-model="content"
      @input="handleInput"
    />

    <!-- @用户选择器（与评论共用） -->
    <MentionPicker
      v-if="showMentionPicker"
      :keyword="mentionKeyword"
      @select="insertMention"
    />
  </div>
</template>
```

### 前端组件复用

- `EmojiPicker.vue` - 评论和文章编辑器共用
- `MentionPicker.vue` - 评论和文章编辑器共用

---

## 实现计划

### 第一阶段：核心功能（P0）

1. 数据库表创建和字段扩展
2. 评论点赞后端服务
3. 评论列表排序增强
4. 评论点赞前端组件
5. Emoji选择器

### 第二阶段：回复增强（P0）

1. 回复列表扁平展示
2. 回复加载更多
3. 回复输入交互

### 第三阶段：@提及功能（P1）

1. @提及解析服务
2. @提及通知
3. @用户选择器组件
4. 文章编辑器@支持

### 第四阶段：体验优化（P2）

1. 点赞列表弹窗
2. 评论数量统计优化
3. 缓存优化
4. 性能测试

---

## 风险与注意事项

### 性能考虑

**问题：** 热门文章可能有大量评论，加载回复可能成为瓶颈。

**方案：**
- 回复采用懒加载，初始只加载前3条
- 点赞数量使用冗余字段 `like_count`，避免 count 查询
- 评论列表使用分页，避免一次加载过多

### @提及边界情况

**问题：** 用户昵称可能包含特殊字符，影响解析。

**方案：**
- @解析时匹配到空格或结束符为止
- 如果昵称不存在，保留原文不转换为链接
- 存储@记录时记录原始文本，便于追溯

### Emoji兼容性

**问题：** 不同设备Emoji渲染可能有差异。

**方案：**
- 使用 Unicode Emoji，依赖系统字体渲染
- 选择广泛支持的 Emoji 字符
- 后续可考虑切换为 Emoji 图片方案

---

## 总结

本次评论体验升级涵盖以下核心功能：

| 功能 | 实现要点 |
|------|----------|
| 楼中楼回复 | 扁平展示，通过 `reply_to_user` 关联 |
| 评论点赞 | 独立点赞表，幂等操作，点赞数冗余 |
| 热评置顶 | 按点赞数排序，无需额外逻辑 |
| @提及 | 正则解析，记录表，通知集成 |
| Emoji表情 | Unicode Emoji，选择器组件 |
| 多种排序 | 前端排序切换，后端动态 ORDER BY |

功能范围清晰，与现有系统耦合度低，可分阶段实现。
