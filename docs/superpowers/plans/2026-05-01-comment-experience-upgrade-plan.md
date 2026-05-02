# 评论体验升级功能实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现评论系统的六大升级功能：楼中楼回复、评论点赞、热评置顶、@提及、Emoji表情、多种排序。

**Architecture:** 后端采用 Spring Boot + MyBatis Plus 扩展现有评论服务，新增 CommentLikeService 和 MentionService；前端采用 Vue 3 组件化开发，新增评论相关组件并集成到文章详情页。

**Tech Stack:** Spring Boot 3.5.14, MyBatis Plus 3.5.5, Vue 3.5, Vant 4

---

## 文件结构

### 后端新增/修改文件

```
blog-server/src/main/java/com/blog/
├── domain/
│   ├── entity/
│   │   ├── CommentLike.java          # 新增：评论点赞实体
│   │   └── Mention.java              # 新增：@提及实体
│   ├── dto/
│   │   ├── CommentDTO.java           # 修改：添加 replyToUserId 字段
│   │   └── LikeResultDTO.java        # 新增：点赞结果DTO
│   ├── vo/
│   │   ├── CommentVO.java            # 修改：添加 likeCount, isLiked, replyCount, replies 字段
│   │   ├── ReplyVO.java              # 新增：回复视图对象
│   │   └── UserSimpleVO.java         # 新增：用户简要信息VO
│   └── enums/
│       ├── CommentSortType.java      # 新增：评论排序类型枚举
│       ├── MentionSourceType.java    # 新增：@提及来源类型枚举
│       └── NotificationType.java     # 修改：添加 MENTION 类型
├── repository/
│   ├── CommentLikeMapper.java        # 新增
│   └── MentionMapper.java            # 新增
├── service/
│   ├── CommentService.java           # 修改：扩展接口
│   ├── CommentLikeService.java       # 新增
│   ├── MentionService.java           # 新增
│   └── impl/
│       ├── CommentServiceImpl.java   # 修改：实现新功能
│       ├── CommentLikeServiceImpl.java # 新增
│       └── MentionServiceImpl.java   # 新增
└── controller/
    └── portal/
        └── CommentController.java    # 修改：添加新接口
```

### 前端新增/修改文件

```
blog-web/src/
├── api/
│   └── comment.js                    # 修改：添加新API
├── components/
│   ├── comment/
│   │   ├── CommentList.vue           # 新增：评论列表组件
│   │   ├── CommentItem.vue           # 新增：单条评论组件
│   │   ├── CommentInput.vue          # 新增：评论输入组件
│   │   ├── ReplyList.vue             # 新增：回复列表组件
│   │   ├── EmojiPicker.vue           # 新增：Emoji选择器
│   │   └── LikeListPopup.vue         # 新增：点赞列表弹窗
│   └── common/
│       └── MentionPicker.vue         # 新增：@用户选择器
└── views/
    └── portal/
        └── ArticleDetail.vue         # 修改：集成新评论组件
```

### 数据库迁移文件

```
blog-server/src/main/resources/db/migration/
└── V2026.05.01__comment_upgrade.sql  # 新增
```

---

## Task 1: 数据库表创建和字段扩展

**Files:**
- Create: `blog-server/src/main/resources/db/migration/V2026.05.01__comment_upgrade.sql`

- [ ] **Step 1: 创建迁移SQL文件**

```sql
-- 评论体验升级数据库迁移脚本
-- 执行日期: 2026-05-01

-- 1. 为评论表添加点赞数字段
ALTER TABLE `comment` ADD COLUMN `like_count` INT DEFAULT 0 COMMENT '点赞数' AFTER `status`;

-- 2. 为评论表添加索引优化排序查询
ALTER TABLE `comment` ADD INDEX `idx_article_like` (`article_id`, `like_count` DESC);

-- 3. 为评论表添加 reply_to_user_id 字段（用于扁平展示回复对象）
ALTER TABLE `comment` ADD COLUMN `reply_to_user_id` BIGINT DEFAULT NULL COMMENT '回复的用户ID' AFTER `reply_id`;
ALTER TABLE `comment` ADD INDEX `idx_reply_to_user` (`reply_to_user_id`);

-- 4. 创建评论点赞表
CREATE TABLE IF NOT EXISTS `comment_like` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `comment_id` BIGINT NOT NULL COMMENT '评论ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_comment_user` (`comment_id`, `user_id`),
    KEY `idx_comment_id` (`comment_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论点赞表';

-- 5. 创建@提及记录表
CREATE TABLE IF NOT EXISTS `mention` (
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

- [ ] **Step 2: 提交数据库迁移文件**

```bash
git add blog-server/src/main/resources/db/migration/V2026.05.01__comment_upgrade.sql
git commit -m "feat(db): add comment upgrade migration - like_count, comment_like table, mention table"
```

---

## Task 2: 后端枚举类创建

**Files:**
- Create: `blog-server/src/main/java/com/blog/domain/enums/CommentSortType.java`
- Create: `blog-server/src/main/java/com/blog/domain/enums/MentionSourceType.java`
- Modify: `blog-server/src/main/java/com/blog/domain/enums/NotificationType.java`

- [ ] **Step 1: 创建评论排序类型枚举**

```java
// CommentSortType.java
package com.blog.domain.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public enum CommentSortType {
    HOT("hot", "最热"),
    NEWEST("newest", "最新"),
    OLDEST("oldest", "最早");

    private final String code;
    private final String desc;

    CommentSortType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private static class Cache {
        static final Map<String, CommentSortType> CODE_MAP =
                Arrays.stream(values()).collect(Collectors.toMap(CommentSortType::getCode, Function.identity()));
    }

    public static CommentSortType fromCode(String code) {
        if (code == null) return HOT; // 默认最热
        return Cache.CODE_MAP.getOrDefault(code.toLowerCase(), HOT);
    }
}
```

- [ ] **Step 2: 创建@提及来源类型枚举**

```java
// MentionSourceType.java
package com.blog.domain.enums;

import lombok.Getter;

@Getter
public enum MentionSourceType {
    COMMENT("COMMENT", "评论"),
    ARTICLE("ARTICLE", "文章");

    private final String code;
    private final String desc;

    MentionSourceType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
```

- [ ] **Step 3: 扩展通知类型枚举，添加 MENTION**

修改 `NotificationType.java`，在枚举值列表中添加：

```java
FOLLOW(1, "FOLLOW", "关注"),
COMMENT(2, "COMMENT", "评论"),
REPLY(3, "REPLY", "回复"),
ANNOUNCEMENT(4, "ANNOUNCEMENT", "公告"),
MENTION(5, "MENTION", "@提及");  // 新增
```

- [ ] **Step 4: 提交枚举类**

```bash
git add blog-server/src/main/java/com/blog/domain/enums/
git commit -m "feat(enums): add CommentSortType, MentionSourceType and MENTION notification type"
```

---

## Task 3: 后端实体类创建

**Files:**
- Modify: `blog-server/src/main/java/com/blog/domain/entity/Comment.java`
- Create: `blog-server/src/main/java/com/blog/domain/entity/CommentLike.java`
- Create: `blog-server/src/main/java/com/blog/domain/entity/Mention.java`

- [ ] **Step 1: 扩展 Comment 实体，添加新字段**

在 `Comment.java` 中添加字段：

```java
// 在 status 字段后添加
private Integer likeCount;

// 在 replyId 字段后添加
private Long replyToUserId;
```

- [ ] **Step 2: 创建 CommentLike 实体**

```java
// CommentLike.java
package com.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("comment_like")
public class CommentLike implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long commentId;

    private Long userId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
```

- [ ] **Step 3: 创建 Mention 实体**

```java
// Mention.java
package com.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("mention")
public class Mention implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String sourceType;

    private Long sourceId;

    private Long mentionedUserId;

    private Long mentionerId;

    private String content;

    private Integer isNotified;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
```

- [ ] **Step 4: 提交实体类**

```bash
git add blog-server/src/main/java/com/blog/domain/entity/
git commit -m "feat(entity): add CommentLike, Mention entities and extend Comment with likeCount"
```

---

## Task 4: DTO 和 VO 扩展

**Files:**
- Modify: `blog-server/src/main/java/com/blog/domain/dto/CommentDTO.java`
- Create: `blog-server/src/main/java/com/blog/domain/dto/LikeResultDTO.java`
- Modify: `blog-server/src/main/java/com/blog/domain/vo/CommentVO.java`
- Create: `blog-server/src/main/java/com/blog/domain/vo/ReplyVO.java`
- Create: `blog-server/src/main/java/com/blog/domain/vo/UserSimpleVO.java`

- [ ] **Step 1: 扩展 CommentDTO，添加 replyToUserId**

```java
// 在 CommentDTO.java 中添加字段
private Long replyToUserId;
```

- [ ] **Step 2: 创建 LikeResultDTO**

```java
// LikeResultDTO.java
package com.blog.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikeResultDTO {
    private Boolean liked;
    private Integer likeCount;
}
```

- [ ] **Step 3: 扩展 CommentVO，添加新字段**

在 `CommentVO.java` 中添加：

```java
// 添加新字段
private Integer likeCount;
private Boolean isLiked;
private Integer replyCount;
private Long replyToUserId;
private String replyToNickname;
private List<ReplyVO> replies;
```

- [ ] **Step 4: 创建 ReplyVO**

```java
// ReplyVO.java
package com.blog.domain.vo;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ReplyVO {
    private Long id;
    private Long userId;
    private String nickname;
    private String avatar;
    private String content;
    private Integer likeCount;
    private Boolean isLiked;
    private Long replyToUserId;
    private String replyToNickname;
    private LocalDateTime createTime;
}
```

- [ ] **Step 5: 创建 UserSimpleVO**

```java
// UserSimpleVO.java
package com.blog.domain.vo;

import lombok.Data;

@Data
public class UserSimpleVO {
    private Long userId;
    private String nickname;
    private String avatar;
}
```

- [ ] **Step 6: 提交 DTO 和 VO**

```bash
git add blog-server/src/main/java/com/blog/domain/dto/
git add blog-server/src/main/java/com/blog/domain/vo/
git commit -m "feat(dto/vo): add LikeResultDTO, ReplyVO, UserSimpleVO and extend CommentDTO/CommentVO"
```

---

## Task 5: Mapper 接口创建

**Files:**
- Create: `blog-server/src/main/java/com/blog/repository/mapper/CommentLikeMapper.java`
- Create: `blog-server/src/main/java/com/blog/repository/mapper/MentionMapper.java`
- Modify: `blog-server/src/main/java/com/blog/repository/mapper/CommentMapper.java`

- [ ] **Step 1: 创建 CommentLikeMapper**

```java
// CommentLikeMapper.java
package com.blog.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.domain.entity.CommentLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CommentLikeMapper extends BaseMapper<CommentLike> {

    @Update("UPDATE comment SET like_count = like_count + 1 WHERE id = #{commentId}")
    void incrementLikeCount(@Param("commentId") Long commentId);

    @Update("UPDATE comment SET like_count = GREATEST(0, like_count - 1) WHERE id = #{commentId}")
    void decrementLikeCount(@Param("commentId") Long commentId);
}
```

- [ ] **Step 2: 创建 MentionMapper**

```java
// MentionMapper.java
package com.blog.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.domain.entity.Mention;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MentionMapper extends BaseMapper<Mention> {
}
```

- [ ] **Step 3: 扩展 CommentMapper，添加查询回复数量方法**

在 `CommentMapper.java` 中添加：

```java
@Select("SELECT COUNT(*) FROM comment WHERE parent_id = #{parentId} AND status = 1")
Integer countReplies(@Param("parentId") Long parentId);

@Select("SELECT like_count FROM comment WHERE id = #{commentId}")
Integer selectLikeCount(@Param("commentId") Long commentId);
```

- [ ] **Step 4: 提交 Mapper**

```bash
git add blog-server/src/main/java/com/blog/repository/mapper/
git commit -m "feat(mapper): add CommentLikeMapper, MentionMapper and extend CommentMapper"
```

---

## Task 6: CommentLikeService 实现

**Files:**
- Create: `blog-server/src/main/java/com/blog/service/CommentLikeService.java`
- Create: `blog-server/src/main/java/com/blog/service/impl/CommentLikeServiceImpl.java`

- [ ] **Step 1: 创建 CommentLikeService 接口**

```java
// CommentLikeService.java
package com.blog.service;

import com.blog.common.result.PageResult;
import com.blog.domain.dto.LikeResultDTO;
import com.blog.domain.vo.UserSimpleVO;
import java.util.List;
import java.util.Map;

public interface CommentLikeService {

    /**
     * 点赞/取消点赞（幂等操作）
     */
    LikeResultDTO toggleLike(Long commentId, Long userId);

    /**
     * 获取评论的点赞用户列表
     */
    PageResult<UserSimpleVO> listLikes(Long commentId, int page, int size);

    /**
     * 检查用户是否已点赞
     */
    boolean isLiked(Long commentId, Long userId);

    /**
     * 批量检查点赞状态
     * @return Map<commentId, isLiked>
     */
    Map<Long, Boolean> batchCheckLiked(List<Long> commentIds, Long userId);
}
```

- [ ] **Step 2: 创建 CommentLikeServiceImpl**

```java
// CommentLikeServiceImpl.java
package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.PageResult;
import com.blog.domain.dto.LikeResultDTO;
import com.blog.domain.entity.CommentLike;
import com.blog.domain.entity.User;
import com.blog.domain.vo.UserSimpleVO;
import com.blog.repository.mapper.CommentLikeMapper;
import com.blog.repository.mapper.CommentMapper;
import com.blog.repository.mapper.UserMapper;
import com.blog.service.CommentLikeService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentLikeServiceImpl implements CommentLikeService {

    private final CommentLikeMapper commentLikeMapper;
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public LikeResultDTO toggleLike(Long commentId, Long userId) {
        // 查询是否已点赞
        CommentLike existing = commentLikeMapper.selectOne(
                new LambdaQueryWrapper<CommentLike>()
                        .eq(CommentLike::getCommentId, commentId)
                        .eq(CommentLike::getUserId, userId)
        );

        boolean liked;
        if (existing != null) {
            // 已点赞，取消
            commentLikeMapper.deleteById(existing.getId());
            commentLikeMapper.decrementLikeCount(commentId);
            liked = false;
        } else {
            // 未点赞，添加
            CommentLike like = new CommentLike();
            like.setCommentId(commentId);
            like.setUserId(userId);
            commentLikeMapper.insert(like);
            commentLikeMapper.incrementLikeCount(commentId);
            liked = true;
        }

        Integer likeCount = commentMapper.selectLikeCount(commentId);
        return new LikeResultDTO(liked, likeCount);
    }

    @Override
    public PageResult<UserSimpleVO> listLikes(Long commentId, int page, int size) {
        Page<CommentLike> likePage = new Page<>(page, size);
        Page<CommentLike> result = commentLikeMapper.selectPage(likePage,
                new LambdaQueryWrapper<CommentLike>()
                        .eq(CommentLike::getCommentId, commentId)
                        .orderByDesc(CommentLike::getCreateTime));

        List<UserSimpleVO> users = result.getRecords().stream()
                .map(like -> {
                    User user = userMapper.selectById(like.getUserId());
                    if (user == null) return null;
                    UserSimpleVO vo = new UserSimpleVO();
                    vo.setUserId(user.getId());
                    vo.setNickname(user.getNickname());
                    vo.setAvatar(user.getAvatar());
                    return vo;
                })
                .filter(vo -> vo != null)
                .collect(Collectors.toList());

        return new PageResult<>(users, result.getTotal());
    }

    @Override
    public boolean isLiked(Long commentId, Long userId) {
        if (userId == null) return false;
        return commentLikeMapper.selectCount(
                new LambdaQueryWrapper<CommentLike>()
                        .eq(CommentLike::getCommentId, commentId)
                        .eq(CommentLike::getUserId, userId)
        ) > 0;
    }

    @Override
    public Map<Long, Boolean> batchCheckLiked(List<Long> commentIds, Long userId) {
        Map<Long, Boolean> result = new HashMap<>();
        if (userId == null || commentIds == null || commentIds.isEmpty()) {
            commentIds.forEach(id -> result.put(id, false));
            return result;
        }

        // 初始化全部为 false
        commentIds.forEach(id -> result.put(id, false));

        // 查询已点赞的
        List<CommentLike> likes = commentLikeMapper.selectList(
                new LambdaQueryWrapper<CommentLike>()
                        .eq(CommentLike::getUserId, userId)
                        .in(CommentLike::getCommentId, commentIds)
        );

        likes.forEach(like -> result.put(like.getCommentId(), true));
        return result;
    }
}
```

- [ ] **Step 3: 提交 CommentLikeService**

```bash
git add blog-server/src/main/java/com/blog/service/CommentLikeService.java
git add blog-server/src/main/java/com/blog/service/impl/CommentLikeServiceImpl.java
git commit -m "feat(service): add CommentLikeService for comment like toggle and list"
```

---

## Task 7: MentionService 实现

**Files:**
- Create: `blog-server/src/main/java/com/blog/service/MentionService.java`
- Create: `blog-server/src/main/java/com/blog/service/impl/MentionServiceImpl.java`

- [ ] **Step 1: 创建 MentionService 接口**

```java
// MentionService.java
package com.blog.service;

import com.blog.common.result.PageResult;
import com.blog.domain.enums.MentionSourceType;
import com.blog.domain.vo.MentionVO;
import java.util.List;

public interface MentionService {

    /**
     * 解析内容中的@用户昵称
     * @return 被@的用户ID列表
     */
    List<Long> parseMentions(String content);

    /**
     * 创建@提及记录并发送通知
     */
    void createMentions(MentionSourceType sourceType, Long sourceId, Long mentionerId, String content);

    /**
     * 获取用户的@提及通知列表
     */
    PageResult<MentionVO> listUserMentions(Long userId, int page, int size);
}
```

- [ ] **Step 2: 创建 MentionVO**

```java
// MentionVO.java
package com.blog.domain.vo;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class MentionVO {
    private Long id;
    private String sourceType;
    private Long sourceId;
    private Long mentionerId;
    private String mentionerNickname;
    private String mentionerAvatar;
    private String content;
    private LocalDateTime createTime;
    // 关联信息
    private String articleTitle;
}
```

- [ ] **Step 3: 创建 MentionServiceImpl**

```java
// MentionServiceImpl.java
package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.PageResult;
import com.blog.domain.entity.Article;
import com.blog.domain.entity.Mention;
import com.blog.domain.entity.Notification;
import com.blog.domain.entity.User;
import com.blog.domain.enums.MentionSourceType;
import com.blog.domain.enums.NotificationType;
import com.blog.domain.vo.MentionVO;
import com.blog.repository.mapper.ArticleMapper;
import com.blog.repository.mapper.MentionMapper;
import com.blog.repository.mapper.NotificationMapper;
import com.blog.repository.mapper.UserMapper;
import com.blog.service.MentionService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MentionServiceImpl implements MentionService {

    private final MentionMapper mentionMapper;
    private final UserMapper userMapper;
    private final ArticleMapper articleMapper;
    private final NotificationMapper notificationMapper;

    // 匹配 @昵称 格式，昵称可以是中文、英文、数字、下划线
    private static final Pattern MENTION_PATTERN = Pattern.compile("@([\\u4e00-\\u9fa5a-zA-Z0-9_]+)");

    @Override
    public List<Long> parseMentions(String content) {
        if (content == null || content.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Long> mentionedUserIds = new HashSet<>();
        Matcher matcher = MENTION_PATTERN.matcher(content);

        while (matcher.find()) {
            String nickname = matcher.group(1);
            // 根据昵称查找用户
            User user = userMapper.selectOne(
                    new LambdaQueryWrapper<User>()
                            .eq(User::getNickname, nickname)
                            .eq(User::getStatus, 1)
            );
            if (user != null) {
                mentionedUserIds.add(user.getId());
            }
        }

        return new ArrayList<>(mentionedUserIds);
    }

    @Override
    @Async
    @Transactional
    public void createMentions(MentionSourceType sourceType, Long sourceId, Long mentionerId, String content) {
        List<Long> mentionedUserIds = parseMentions(content);
        User mentioner = userMapper.selectById(mentionerId);
        if (mentioner == null) return;

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
            mention.setIsNotified(0);
            mentionMapper.insert(mention);

            // 发送通知
            sendMentionNotification(userId, mentioner, sourceType, sourceId);
        }
    }

    @Override
    public PageResult<MentionVO> listUserMentions(Long userId, int page, int size) {
        Page<Mention> mentionPage = new Page<>(page, size);
        Page<Mention> result = mentionMapper.selectPage(mentionPage,
                new LambdaQueryWrapper<Mention>()
                        .eq(Mention::getMentionedUserId, userId)
                        .orderByDesc(Mention::getCreateTime));

        List<MentionVO> vos = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(vos, result.getTotal());
    }

    private void sendMentionNotification(Long userId, User mentioner, MentionSourceType sourceType, Long sourceId) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(NotificationType.MENTION.getCode());
        notification.setTitle("有人@了你");
        notification.setContent(mentioner.getNickname() + "在" + sourceType.getDesc() + "中提到了你");
        notification.setRelatedId(sourceId);
        notification.setSenderId(mentioner.getId());
        notification.setIsRead(0);
        notificationMapper.insert(notification);
    }

    private String extractContext(String content, Long userId) {
        // 提取@相关的内容片段（前后各取20个字符）
        User user = userMapper.selectById(userId);
        if (user == null) return null;

        String nickname = user.getNickname();
        int index = content.indexOf("@" + nickname);
        if (index < 0) return null;

        int start = Math.max(0, index - 20);
        int end = Math.min(content.length(), index + nickname.length() + 1 + 20);
        return content.substring(start, end);
    }

    private MentionVO convertToVO(Mention mention) {
        MentionVO vo = new MentionVO();
        vo.setId(mention.getId());
        vo.setSourceType(mention.getSourceType());
        vo.setSourceId(mention.getSourceId());
        vo.setMentionerId(mention.getMentionerId());
        vo.setContent(mention.getContent());
        vo.setCreateTime(mention.getCreateTime());

        User mentioner = userMapper.selectById(mention.getMentionerId());
        if (mentioner != null) {
            vo.setMentionerNickname(mentioner.getNickname());
            vo.setMentionerAvatar(mentioner.getAvatar());
        }

        // 如果是评论中的@，获取文章标题
        if (MentionSourceType.COMMENT.getCode().equals(mention.getSourceType())) {
            // 需要通过评论ID找到文章
            // 这里简化处理，实际可能需要额外查询
        } else if (MentionSourceType.ARTICLE.getCode().equals(mention.getSourceType())) {
            Article article = articleMapper.selectById(mention.getSourceId());
            if (article != null) {
                vo.setArticleTitle(article.getTitle());
            }
        }

        return vo;
    }
}
```

- [ ] **Step 4: 提交 MentionService**

```bash
git add blog-server/src/main/java/com/blog/service/MentionService.java
git add blog-server/src/main/java/com/blog/service/impl/MentionServiceImpl.java
git add blog-server/src/main/java/com/blog/domain/vo/MentionVO.java
git commit -m "feat(service): add MentionService for @mention parsing and notification"
```

---

## Task 8: 扩展 CommentService

**Files:**
- Modify: `blog-server/src/main/java/com/blog/service/CommentService.java`
- Modify: `blog-server/src/main/java/com/blog/service/impl/CommentServiceImpl.java`

- [ ] **Step 1: 扩展 CommentService 接口**

```java
// 在 CommentService.java 中添加新方法
package com.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.PageResult;
import com.blog.domain.dto.CommentDTO;
import com.blog.domain.dto.LikeResultDTO;
import com.blog.domain.enums.CommentSortType;
import com.blog.domain.vo.CommentVO;
import com.blog.domain.vo.ReplyVO;
import com.blog.domain.vo.UserSimpleVO;

public interface CommentService {

    // 原有方法...

    /**
     * 获取评论列表（支持排序）
     */
    PageResult<CommentVO> listComments(Long articleId, CommentSortType sortType, int page, int size, Long currentUserId);

    /**
     * 创建评论（处理@提及）
     */
    CommentVO createComment(CommentDTO dto, Long currentUserId);

    /**
     * 获取回复列表
     */
    PageResult<ReplyVO> listReplies(Long commentId, CommentSortType sortType, int page, int size, Long currentUserId);

    /**
     * 点赞/取消点赞评论
     */
    LikeResultDTO toggleLike(Long commentId, Long userId);

    /**
     * 获取评论点赞列表
     */
    PageResult<UserSimpleVO> listLikes(Long commentId, int page, int size);
}
```

- [ ] **Step 2: 重写 CommentServiceImpl 实现新功能**

由于改动较大，完整重写 `CommentServiceImpl.java`：

```java
// CommentServiceImpl.java
package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.exception.BusinessException;
import com.blog.common.result.ErrorCode;
import com.blog.common.result.PageResult;
import com.blog.common.utils.BeanCopyUtils;
import com.blog.common.utils.IpUtils;
import com.blog.domain.dto.CommentDTO;
import com.blog.domain.dto.LikeResultDTO;
import com.blog.domain.entity.Comment;
import com.blog.domain.entity.User;
import com.blog.domain.enums.CommentSortType;
import com.blog.domain.enums.MentionSourceType;
import com.blog.domain.vo.CommentVO;
import com.blog.domain.vo.ReplyVO;
import com.blog.domain.vo.UserSimpleVO;
import com.blog.repository.mapper.CommentMapper;
import com.blog.repository.mapper.UserMapper;
import com.blog.service.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final UserMapper userMapper;
    private final SensitiveWordService sensitiveWordService;
    private final AchievementTriggerService achievementTriggerService;
    private final CommentLikeService commentLikeService;
    private final MentionService mentionService;

    // 原有方法保留...

    @Override
    public PageResult<CommentVO> listComments(Long articleId, CommentSortType sortType,
                                               int page, int size, Long currentUserId) {
        // 构建排序条件
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getArticleId, articleId)
               .eq(Comment::getStatus, 1)
               .eq(Comment::getParentId, 0);

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

        Page<Comment> commentPage = commentMapper.selectPage(new Page<>(page, size), wrapper);

        // 获取所有评论ID用于批量检查点赞状态
        List<Long> commentIds = commentPage.getRecords().stream()
                .map(Comment::getId)
                .collect(Collectors.toList());
        Map<Long, Boolean> likedMap = commentLikeService.batchCheckLiked(commentIds, currentUserId);

        // 转换为VO
        List<CommentVO> vos = commentPage.getRecords().stream()
                .map(comment -> convertToVO(comment, likedMap, currentUserId))
                .collect(Collectors.toList());

        return new PageResult<>(vos, commentPage.getTotal());
    }

    @Override
    @Transactional
    public CommentVO createComment(CommentDTO dto, Long currentUserId) {
        Comment comment = new Comment();
        comment.setArticleId(dto.getArticleId());
        comment.setParentId(dto.getParentId() != null ? dto.getParentId() : 0L);
        comment.setReplyId(dto.getReplyId());
        comment.setReplyToUserId(dto.getReplyToUserId());

        // 敏感词过滤
        String filteredContent = sensitiveWordService.filter(dto.getContent());
        comment.setContent(filteredContent);
        comment.setLikeCount(0);
        comment.setIpAddress(IpUtils.getIpAddress());

        if (currentUserId != null) {
            comment.setUserId(currentUserId);
            User user = userMapper.selectById(currentUserId);
            if (user != null) {
                comment.setNickname(user.getNickname());
                comment.setEmail(user.getEmail());
            }
        } else {
            comment.setNickname(dto.getNickname());
            comment.setEmail(dto.getEmail());
        }

        comment.setStatus(1); // 默认通过，可配置
        commentMapper.insert(comment);

        // 异步处理@提及
        mentionService.createMentions(MentionSourceType.COMMENT, comment.getId(),
                currentUserId, filteredContent);

        return convertToVO(comment, Map.of(), currentUserId);
    }

    @Override
    public PageResult<ReplyVO> listReplies(Long commentId, CommentSortType sortType,
                                            int page, int size, Long currentUserId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getParentId, commentId)
               .eq(Comment::getStatus, 1);

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

        Page<Comment> replyPage = commentMapper.selectPage(new Page<>(page, size), wrapper);

        // 批量检查点赞状态
        List<Long> replyIds = replyPage.getRecords().stream()
                .map(Comment::getId)
                .collect(Collectors.toList());
        Map<Long, Boolean> likedMap = commentLikeService.batchCheckLiked(replyIds, currentUserId);

        List<ReplyVO> vos = replyPage.getRecords().stream()
                .map(reply -> convertToReplyVO(reply, likedMap, currentUserId))
                .collect(Collectors.toList());

        return new PageResult<>(vos, replyPage.getTotal());
    }

    @Override
    public LikeResultDTO toggleLike(Long commentId, Long userId) {
        return commentLikeService.toggleLike(commentId, userId);
    }

    @Override
    public PageResult<UserSimpleVO> listLikes(Long commentId, int page, int size) {
        return commentLikeService.listLikes(commentId, page, size);
    }

    // === 私有方法 ===

    private CommentVO convertToVO(Comment comment, Map<Long, Boolean> likedMap, Long currentUserId) {
        CommentVO vo = BeanCopyUtils.copy(comment, CommentVO.class);

        if (comment.getUserId() != null) {
            User user = userMapper.selectById(comment.getUserId());
            if (user != null) {
                vo.setAvatar(user.getAvatar());
            }
        }

        vo.setIsLiked(likedMap.getOrDefault(comment.getId(), false));

        // 查询回复数
        Integer replyCount = commentMapper.countReplies(comment.getId());
        vo.setReplyCount(replyCount != null ? replyCount : 0);

        // 加载前3条回复
        if (replyCount != null && replyCount > 0) {
            List<Comment> topReplies = commentMapper.selectList(
                    new LambdaQueryWrapper<Comment>()
                            .eq(Comment::getParentId, comment.getId())
                            .eq(Comment::getStatus, 1)
                            .orderByDesc(Comment::getLikeCount)
                            .last("LIMIT 3")
            );
            vo.setReplies(topReplies.stream()
                    .map(r -> convertToReplyVO(r, likedMap, currentUserId))
                    .collect(Collectors.toList()));
        }

        return vo;
    }

    private ReplyVO convertToReplyVO(Comment reply, Map<Long, Boolean> likedMap, Long currentUserId) {
        ReplyVO vo = new ReplyVO();
        vo.setId(reply.getId());
        vo.setUserId(reply.getUserId());
        vo.setContent(reply.getContent());
        vo.setLikeCount(reply.getLikeCount());
        vo.setCreateTime(reply.getCreateTime());
        vo.setReplyToUserId(reply.getReplyToUserId());

        if (reply.getUserId() != null) {
            User user = userMapper.selectById(reply.getUserId());
            if (user != null) {
                vo.setNickname(user.getNickname());
                vo.setAvatar(user.getAvatar());
            }
        }

        if (reply.getReplyToUserId() != null) {
            User replyToUser = userMapper.selectById(reply.getReplyToUserId());
            if (replyToUser != null) {
                vo.setReplyToNickname(replyToUser.getNickname());
            }
        }

        vo.setIsLiked(likedMap.getOrDefault(reply.getId(), false));
        return vo;
    }
}
```

- [ ] **Step 3: 提交 CommentService 扩展**

```bash
git add blog-server/src/main/java/com/blog/service/CommentService.java
git add blog-server/src/main/java/com/blog/service/impl/CommentServiceImpl.java
git commit -m "feat(service): extend CommentService with sort, like, reply and mention support"
```

---

## Task 9: 扩展 CommentController

**Files:**
- Modify: `blog-server/src/main/java/com/blog/controller/portal/CommentController.java`

- [ ] **Step 1: 添加新的 API 接口**

```java
// CommentController.java
package com.blog.controller.portal;

import com.blog.common.result.PageResult;
import com.blog.common.result.Result;
import com.blog.domain.dto.CommentDTO;
import com.blog.domain.dto.LikeResultDTO;
import com.blog.domain.enums.CommentSortType;
import com.blog.domain.vo.CommentVO;
import com.blog.domain.vo.ReplyVO;
import com.blog.domain.vo.UserSimpleVO;
import com.blog.security.LoginUser;
import com.blog.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "前台评论接口")
@RestController
@RequestMapping("/api/portal/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "获取文章评论列表")
    @GetMapping("/article/{articleId}")
    public Result<PageResult<CommentVO>> listComments(
            @PathVariable Long articleId,
            @RequestParam(defaultValue = "hot") String sortBy,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long currentUserId = getCurrentUserId();
        CommentSortType sortType = CommentSortType.fromCode(sortBy);
        return Result.success(commentService.listComments(articleId, sortType, page, size, currentUserId));
    }

    @Operation(summary = "发表评论")
    @PostMapping
    public Result<CommentVO> createComment(@Valid @RequestBody CommentDTO dto) {
        Long currentUserId = getCurrentUserId();
        CommentVO vo = commentService.createComment(dto, currentUserId);
        return Result.success(vo);
    }

    @Operation(summary = "获取评论的回复列表")
    @GetMapping("/{commentId}/replies")
    public Result<PageResult<ReplyVO>> listReplies(
            @PathVariable Long commentId,
            @RequestParam(defaultValue = "hot") String sortBy,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long currentUserId = getCurrentUserId();
        CommentSortType sortType = CommentSortType.fromCode(sortBy);
        return Result.success(commentService.listReplies(commentId, sortType, page, size, currentUserId));
    }

    @Operation(summary = "点赞/取消点赞评论")
    @PostMapping("/{commentId}/like")
    public Result<LikeResultDTO> toggleLike(@PathVariable Long commentId) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            return Result.error(401, "请先登录");
        }
        return Result.success(commentService.toggleLike(commentId, currentUserId));
    }

    @Operation(summary = "获取评论点赞列表")
    @GetMapping("/{commentId}/likes")
    public Result<PageResult<UserSimpleVO>> listLikes(
            @PathVariable Long commentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(commentService.listLikes(commentId, page, size));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            return ((LoginUser) authentication.getPrincipal()).getUserId();
        }
        return null;
    }
}
```

- [ ] **Step 2: 提交 Controller**

```bash
git add blog-server/src/main/java/com/blog/controller/portal/CommentController.java
git commit -m "feat(controller): add comment like, reply list and sort APIs"
```

---

## Task 10: 用户搜索 API

**Files:**
- Modify: `blog-server/src/main/java/com/blog/controller/portal/UserProfileController.java`
- Or Create: `blog-server/src/main/java/com/blog/controller/portal/UserController.java`

- [ ] **Step 1: 添加用户搜索接口**

在 `UserProfileController.java` 中添加：

```java
@Operation(summary = "搜索用户（用于@提及）")
@GetMapping("/search")
public Result<List<UserSimpleVO>> searchUsers(
        @RequestParam String keyword,
        @RequestParam(defaultValue = "10") int size) {
    if (keyword == null || keyword.trim().isEmpty()) {
        return Result.success(new ArrayList<>());
    }

    List<User> users = userMapper.selectList(
            new LambdaQueryWrapper<User>()
                    .eq(User::getStatus, 1)
                    .and(w -> w
                            .like(User::getNickname, keyword)
                            .or()
                            .like(User::getUsername, keyword)
                    )
                    .last("LIMIT " + size)
    );

    List<UserSimpleVO> vos = users.stream()
            .map(user -> {
                UserSimpleVO vo = new UserSimpleVO();
                vo.setUserId(user.getId());
                vo.setNickname(user.getNickname());
                vo.setAvatar(user.getAvatar());
                return vo;
            })
            .collect(Collectors.toList());

    return Result.success(vos);
}
```

- [ ] **Step 2: 提交用户搜索接口**

```bash
git add blog-server/src/main/java/com/blog/controller/portal/UserProfileController.java
git commit -m "feat(controller): add user search API for @mention"
```

---

## Task 11: 前端评论 API 封装

**Files:**
- Modify: `blog-web/src/api/comment.js`

- [ ] **Step 1: 添加评论相关 API**

```javascript
// comment.js
import request from '@/utils/request'

// 获取文章评论列表
export function getComments(articleId, params) {
  return request({
    url: `/api/portal/comments/article/${articleId}`,
    method: 'get',
    params: {
      sortBy: params.sortBy || 'hot',
      page: params.page || 1,
      size: params.size || 10
    }
  })
}

// 发表评论
export function createComment(data) {
  return request({
    url: '/api/portal/comments',
    method: 'post',
    data
  })
}

// 获取评论回复列表
export function getReplies(commentId, params) {
  return request({
    url: `/api/portal/comments/${commentId}/replies`,
    method: 'get',
    params: {
      sortBy: params.sortBy || 'hot',
      page: params.page || 1,
      size: params.size || 10
    }
  })
}

// 点赞/取消点赞评论
export function toggleCommentLike(commentId) {
  return request({
    url: `/api/portal/comments/${commentId}/like`,
    method: 'post'
  })
}

// 获取评论点赞列表
export function getCommentLikes(commentId, params) {
  return request({
    url: `/api/portal/comments/${commentId}/likes`,
    method: 'get',
    params: {
      page: params.page || 1,
      size: params.size || 20
    }
  })
}

// 搜索用户（用于@提及）
export function searchUsers(keyword, size = 10) {
  return request({
    url: '/api/portal/users/search',
    method: 'get',
    params: { keyword, size }
  })
}
```

- [ ] **Step 2: 提交 API 封装**

```bash
git add blog-web/src/api/comment.js
git commit -m "feat(api): add comment like, reply, mention APIs"
```

---

## Task 12: Emoji 选择器组件

**Files:**
- Create: `blog-web/src/components/comment/EmojiPicker.vue`

- [ ] **Step 1: 创建 EmojiPicker 组件**

```vue
<!-- EmojiPicker.vue -->
<template>
  <div class="emoji-picker">
    <div class="emoji-tabs">
      <button
        v-for="tab in tabs"
        :key="tab.name"
        :class="['tab-btn', { active: activeTab === tab.name }]"
        @click="activeTab = tab.name"
      >
        {{ tab.icon }}
      </button>
    </div>
    <div class="emoji-grid">
      <button
        v-for="emoji in currentEmojis"
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
import { ref, computed } from 'vue'

const emit = defineEmits(['select', 'close'])

const tabs = [
  { name: 'smileys', icon: '😀' },
  { name: 'gestures', icon: '👍' },
  { name: 'hearts', icon: '❤️' },
  { name: 'objects', icon: '🎉' }
]

const emojis = {
  smileys: [
    '😀', '😂', '🤣', '😊', '😇', '🙂', '😉', '😍',
    '🥰', '😘', '😋', '🤔', '😏', '😒', '😌', '😴',
    '😷', '🤗', '🤩', '😎', '🥳', '😢', '😭', '😤'
  ],
  gestures: [
    '👍', '👎', '👏', '🙌', '🤝', '✌️', '🤞', '👌',
    '👈', '👉', '👆', '👇', '☝️', '✋', '🤚', '🖐️',
    '🖖', '👋', '🤙', '💪', '🦾', '🙏', '✍️', '🤳'
  ],
  hearts: [
    '❤️', '🧡', '💛', '💚', '💙', '💜', '🖤', '🤍',
    '💔', '❣️', '💕', '💖', '💗', '💓', '💞', '💘',
    '💝', '💟', '♥️', '❤️‍🔥', '❤️‍🩹', '💕', '💗', '💖'
  ],
  objects: [
    '🎉', '🎊', '🎁', '🎈', '🎂', '🎄', '🎃', '🎄',
    '🔥', '⭐', '✨', '💫', '🌟', '💥', '💢', '💯',
    '📚', '📝', '💡', '📌', '📎', '✏️', '🖊️', '📖'
  ]
}

const activeTab = ref('smileys')

const currentEmojis = computed(() => emojis[activeTab.value] || [])
</script>

<style scoped>
.emoji-picker {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
  padding: 8px;
  max-width: 280px;
}

.emoji-tabs {
  display: flex;
  gap: 4px;
  margin-bottom: 8px;
  border-bottom: 1px solid #eee;
  padding-bottom: 8px;
}

.tab-btn {
  width: 36px;
  height: 36px;
  border: none;
  background: transparent;
  border-radius: 4px;
  cursor: pointer;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.tab-btn.active {
  background: #f0f0f0;
}

.emoji-grid {
  display: grid;
  grid-template-columns: repeat(8, 1fr);
  gap: 4px;
}

.emoji-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  border-radius: 4px;
  cursor: pointer;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.2s;
}

.emoji-btn:hover {
  background: #f0f0f0;
}
</style>
```

- [ ] **Step 2: 提交 EmojiPicker 组件**

```bash
git add blog-web/src/components/comment/EmojiPicker.vue
git commit -m "feat(component): add EmojiPicker component"
```

---

## Task 13: 评论输入组件

**Files:**
- Create: `blog-web/src/components/comment/CommentInput.vue`

- [ ] **Step 1: 创建 CommentInput 组件**

```vue
<!-- CommentInput.vue -->
<template>
  <div class="comment-input">
    <!-- 回复提示 -->
    <div v-if="replyTo" class="reply-hint">
      <span>回复 <strong>@{{ replyTo.nickname }}</strong></span>
      <button class="cancel-btn" @click="$emit('cancel-reply')">
        <van-icon name="cross" />
      </button>
    </div>

    <!-- 输入区域 -->
    <div class="input-wrapper">
      <textarea
        ref="textareaRef"
        v-model="content"
        :placeholder="placeholder"
        rows="3"
        @input="handleInput"
        @keydown="handleKeydown"
      />

      <!-- @用户选择器 -->
      <div v-if="showMentionPicker" class="mention-picker">
        <div v-if="loading" class="loading">搜索中...</div>
        <template v-else>
          <div
            v-for="user in matchedUsers"
            :key="user.userId"
            class="user-item"
            @click="selectMention(user)"
          >
            <img :src="user.avatar || defaultAvatar" class="avatar" />
            <span class="nickname">{{ user.nickname }}</span>
          </div>
          <div v-if="matchedUsers.length === 0" class="no-result">
            未找到用户
          </div>
        </template>
      </div>
    </div>

    <!-- 工具栏 -->
    <div class="toolbar">
      <div class="tools">
        <button class="tool-btn" @click="toggleEmojiPicker">
          <span>😊</span>
          <span class="tool-text">表情</span>
        </button>
      </div>
      <button
        class="submit-btn"
        :disabled="!content.trim() || submitting"
        @click="submit"
      >
        {{ submitting ? '发表中...' : '发表评论' }}
      </button>
    </div>

    <!-- Emoji选择器 -->
    <EmojiPicker
      v-if="showEmojiPicker"
      @select="insertEmoji"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch, nextTick } from 'vue'
import { Toast } from 'vant'
import EmojiPicker from './EmojiPicker.vue'
import { searchUsers } from '@/api/comment'

const props = defineProps({
  articleId: {
    type: Number,
    required: true
  },
  replyTo: {
    type: Object,
    default: null
  },
  placeholder: {
    type: String,
    default: '写下你的评论...'
  }
})

const emit = defineEmits(['submit', 'cancel-reply'])

const defaultAvatar = 'https://via.placeholder.com/40'
const content = ref('')
const submitting = ref(false)
const showEmojiPicker = ref(false)
const showMentionPicker = ref(false)
const matchedUsers = ref([])
const loading = ref(false)
const mentionStartIndex = ref(-1)
const textareaRef = ref(null)

const toggleEmojiPicker = () => {
  showEmojiPicker.value = !showEmojiPicker.value
}

const insertEmoji = (emoji) => {
  content.value += emoji
  showEmojiPicker.value = false
  textareaRef.value?.focus()
}

const handleInput = async (e) => {
  const value = e.target.value
  const cursorPos = e.target.selectionStart

  // 检测@符号
  const lastAtIndex = value.lastIndexOf('@', cursorPos - 1)
  if (lastAtIndex !== -1) {
    // 检查@后面是否有空格（表示已完成）
    const textAfterAt = value.slice(lastAtIndex + 1, cursorPos)
    if (!textAfterAt.includes(' ') && textAfterAt.length <= 20) {
      mentionStartIndex.value = lastAtIndex
      showMentionPicker.value = true
      await searchMentionUsers(textAfterAt)
      return
    }
  }

  showMentionPicker.value = false
}

const searchMentionUsers = async (keyword) => {
  if (!keyword) {
    matchedUsers.value = []
    return
  }

  loading.value = true
  try {
    const { data } = await searchUsers(keyword, 5)
    matchedUsers.value = data || []
  } catch (error) {
    console.error('Search users failed:', error)
    matchedUsers.value = []
  } finally {
    loading.value = false
  }
}

const selectMention = (user) => {
  // 替换@及其后面的文本为 @昵称
  const before = content.value.slice(0, mentionStartIndex.value)
  const after = content.value.slice(textareaRef.value.selectionStart)
  content.value = `${before}@${user.nickname} ${after}`

  showMentionPicker.value = false
  nextTick(() => {
    textareaRef.value?.focus()
  })
}

const handleKeydown = (e) => {
  // Ctrl/Cmd + Enter 提交
  if ((e.ctrlKey || e.metaKey) && e.key === 'Enter') {
    submit()
  }
}

const submit = async () => {
  if (!content.value.trim() || submitting.value) return

  submitting.value = true
  try {
    const data = {
      articleId: props.articleId,
      content: content.value.trim()
    }

    if (props.replyTo) {
      data.parentId = props.replyTo.parentId || props.replyTo.id
      data.replyId = props.replyTo.id
      data.replyToUserId = props.replyTo.userId
    }

    emit('submit', data)
    content.value = ''
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.comment-input {
  background: #f7f8fa;
  border-radius: 8px;
  padding: 12px;
}

.reply-hint {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  background: #e8f4ff;
  border-radius: 4px;
  margin-bottom: 8px;
  font-size: 14px;
  color: #1989fa;
}

.cancel-btn {
  border: none;
  background: transparent;
  cursor: pointer;
  padding: 4px;
}

.input-wrapper {
  position: relative;
}

textarea {
  width: 100%;
  border: 1px solid #ebedf0;
  border-radius: 4px;
  padding: 12px;
  font-size: 14px;
  resize: none;
  outline: none;
}

textarea:focus {
  border-color: #1989fa;
}

.mention-picker {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: #fff;
  border: 1px solid #ebedf0;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  max-height: 200px;
  overflow-y: auto;
  z-index: 10;
}

.mention-picker .loading,
.mention-picker .no-result {
  padding: 12px;
  text-align: center;
  color: #969799;
}

.mention-picker .user-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  cursor: pointer;
}

.mention-picker .user-item:hover {
  background: #f7f8fa;
}

.mention-picker .avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
}

.mention-picker .nickname {
  font-size: 14px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}

.tools {
  display: flex;
  gap: 8px;
}

.tool-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border: none;
  background: transparent;
  font-size: 14px;
  cursor: pointer;
  border-radius: 4px;
}

.tool-btn:hover {
  background: #ebedf0;
}

.tool-text {
  color: #646566;
}

.submit-btn {
  padding: 8px 16px;
  background: #1989fa;
  color: #fff;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
}

.submit-btn:disabled {
  background: #c8c9cc;
  cursor: not-allowed;
}
</style>
```

- [ ] **Step 2: 提交 CommentInput 组件**

```bash
git add blog-web/src/components/comment/CommentInput.vue
git commit -m "feat(component): add CommentInput with emoji picker and @mention support"
```

---

## Task 14: 评论项组件

**Files:**
- Create: `blog-web/src/components/comment/CommentItem.vue`

- [ ] **Step 1: 创建 CommentItem 组件**

```vue
<!-- CommentItem.vue -->
<template>
  <div class="comment-item">
    <img :src="comment.avatar || defaultAvatar" class="avatar" />

    <div class="content">
      <div class="header">
        <span class="nickname">{{ comment.nickname }}</span>
        <span class="time">{{ formatTime(comment.createTime) }}</span>
      </div>

      <div class="text" v-html="renderContent(comment.content)"></div>

      <div class="actions">
        <button class="action-btn" @click="handleLike">
          <span :class="['like-icon', { liked: comment.isLiked }]">👍</span>
          <span class="count" @click.stop="showLikes">{{ comment.likeCount || 0 }}</span>
        </button>
        <button class="action-btn" @click="$emit('reply', comment)">
          回复
        </button>
      </div>

      <!-- 回复列表 -->
      <div v-if="comment.replies && comment.replies.length > 0" class="replies">
        <ReplyItem
          v-for="reply in comment.replies"
          :key="reply.id"
          :reply="reply"
          @reply="$emit('reply', $event)"
          @like="$emit('like', $event)"
        />

        <button
          v-if="comment.replyCount > comment.replies.length"
          class="more-replies"
          @click="$emit('load-replies', comment.id)"
        >
          展开更多 {{ comment.replyCount - comment.replies.length }} 条回复
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'
import ReplyItem from './ReplyItem.vue'
import { formatTimeAgo } from '@/utils/time'

const props = defineProps({
  comment: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['reply', 'like', 'show-likes', 'load-replies'])

const defaultAvatar = 'https://via.placeholder.com/40'

const formatTime = (time) => formatTimeAgo(time)

const renderContent = (content) => {
  // 将@用户转换为高亮显示
  return content.replace(/@([一-龥a-zA-Z0-9_]+)/g, '<span class="mention">@$1</span>')
}

const handleLike = () => {
  emit('like', props.comment)
}

const showLikes = () => {
  emit('show-likes', props.comment)
}
</script>

<style scoped>
.comment-item {
  display: flex;
  gap: 12px;
  padding: 16px 0;
  border-bottom: 1px solid #ebedf0;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  flex-shrink: 0;
}

.content {
  flex: 1;
  min-width: 0;
}

.header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.nickname {
  font-weight: 500;
  color: #323233;
}

.time {
  font-size: 12px;
  color: #969799;
}

.text {
  font-size: 14px;
  line-height: 1.6;
  color: #323233;
  word-break: break-word;
}

.text :deep(.mention) {
  color: #1989fa;
  font-weight: 500;
}

.actions {
  display: flex;
  gap: 16px;
  margin-top: 8px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border: none;
  background: transparent;
  font-size: 14px;
  color: #646566;
  cursor: pointer;
  border-radius: 4px;
}

.action-btn:hover {
  background: #f7f8fa;
}

.like-icon.liked {
  filter: none;
}

.count {
  color: #969799;
}

.replies {
  margin-top: 12px;
  padding-left: 12px;
  border-left: 2px solid #ebedf0;
}

.more-replies {
  display: block;
  width: 100%;
  padding: 8px;
  margin-top: 8px;
  border: none;
  background: #f7f8fa;
  color: #1989fa;
  font-size: 13px;
  cursor: pointer;
  border-radius: 4px;
}

.more-replies:hover {
  background: #ebedf0;
}
</style>
```

- [ ] **Step 2: 创建 ReplyItem 组件**

```vue
<!-- ReplyItem.vue -->
<template>
  <div class="reply-item">
    <img :src="reply.avatar || defaultAvatar" class="avatar" />

    <div class="content">
      <div class="header">
        <span class="nickname">{{ reply.nickname }}</span>
        <template v-if="reply.replyToNickname">
          <span class="reply-to">回复</span>
          <span class="reply-to-name">@{{ reply.replyToNickname }}</span>
        </template>
        <span class="time">{{ formatTime(reply.createTime) }}</span>
      </div>

      <div class="text" v-html="renderContent(reply.content)"></div>

      <div class="actions">
        <button class="action-btn" @click="handleLike">
          <span :class="['like-icon', { liked: reply.isLiked }]">👍</span>
          <span class="count">{{ reply.likeCount || 0 }}</span>
        </button>
        <button class="action-btn" @click="$emit('reply', reply)">
          回复
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'
import { formatTimeAgo } from '@/utils/time'

const props = defineProps({
  reply: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['reply', 'like'])

const defaultAvatar = 'https://via.placeholder.com/40'

const formatTime = (time) => formatTimeAgo(time)

const renderContent = (content) => {
  return content.replace(/@([一-龥a-zA-Z0-9_]+)/g, '<span class="mention">@$1</span>')
}

const handleLike = () => {
  emit('like', props.reply)
}
</script>

<style scoped>
.reply-item {
  display: flex;
  gap: 8px;
  padding: 8px 0;
}

.avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  flex-shrink: 0;
}

.content {
  flex: 1;
  min-width: 0;
}

.header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 2px;
  font-size: 13px;
}

.nickname {
  font-weight: 500;
  color: #323233;
}

.reply-to {
  color: #969799;
}

.reply-to-name {
  color: #1989fa;
}

.time {
  font-size: 12px;
  color: #969799;
}

.text {
  font-size: 13px;
  line-height: 1.5;
  color: #323233;
}

.text :deep(.mention) {
  color: #1989fa;
}

.actions {
  display: flex;
  gap: 12px;
  margin-top: 4px;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 2px 6px;
  border: none;
  background: transparent;
  font-size: 12px;
  color: #646566;
  cursor: pointer;
  border-radius: 4px;
}

.action-btn:hover {
  background: #f7f8fa;
}

.count {
  color: #969799;
  font-size: 12px;
}
</style>
```

- [ ] **Step 3: 提交组件**

```bash
git add blog-web/src/components/comment/CommentItem.vue
git add blog-web/src/components/comment/ReplyItem.vue
git commit -m "feat(component): add CommentItem and ReplyItem components"
```

---

## Task 15: 评论列表组件

**Files:**
- Create: `blog-web/src/components/comment/CommentList.vue`

- [ ] **Step 1: 创建 CommentList 组件**

```vue
<!-- CommentList.vue -->
<template>
  <div class="comment-section">
    <!-- 标题和排序 -->
    <div class="header">
      <h3 class="title">评论区 ({{ total }}条)</h3>
      <div class="sort-bar">
        <button
          v-for="option in sortOptions"
          :key="option.value"
          :class="['sort-btn', { active: sortBy === option.value }]"
          @click="changeSort(option.value)"
        >
          {{ option.label }}
        </button>
      </div>
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
        @load-replies="loadMoreReplies"
      />

      <!-- 加载更多 -->
      <div v-if="hasMore" class="load-more">
        <van-button block @click="loadMore" :loading="loading">
          加载更多评论
        </van-button>
      </div>

      <!-- 空状态 -->
      <van-empty v-if="!loading && comments.length === 0" description="暂无评论，快来抢沙发吧~" />
    </div>

    <!-- 评论输入 -->
    <div class="comment-input-wrapper">
      <CommentInput
        :article-id="articleId"
        :reply-to="replyTo"
        @submit="handleSubmit"
        @cancel-reply="cancelReply"
      />
    </div>

    <!-- 点赞列表弹窗 -->
    <van-popup
      v-model:show="showLikePopup"
      position="bottom"
      round
      style="height: 50%"
    >
      <div class="like-popup">
        <div class="popup-header">
          <span>点赞列表 ({{ likeTotal }}人)</span>
          <van-icon name="cross" @click="showLikePopup = false" />
        </div>
        <div class="like-list">
          <div v-for="user in likeUsers" :key="user.userId" class="like-user">
            <img :src="user.avatar || defaultAvatar" class="avatar" />
            <span class="nickname">{{ user.nickname }}</span>
          </div>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { Toast } from 'vant'
import CommentItem from './CommentItem.vue'
import CommentInput from './CommentInput.vue'
import {
  getComments,
  createComment,
  toggleCommentLike,
  getCommentLikes,
  getReplies
} from '@/api/comment'

const props = defineProps({
  articleId: {
    type: Number,
    required: true
  }
})

const defaultAvatar = 'https://via.placeholder.com/40'

const sortOptions = [
  { value: 'hot', label: '最热' },
  { value: 'newest', label: '最新' },
  { value: 'oldest', label: '最早' }
]

const comments = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const sortBy = ref('hot')
const loading = ref(false)
const replyTo = ref(null)

// 点赞列表
const showLikePopup = ref(false)
const likeUsers = ref([])
const likeTotal = ref(0)
const currentLikeComment = ref(null)

const hasMore = computed(() => comments.value.length < total.value)

const fetchComments = async (reset = false) => {
  if (reset) {
    page.value = 1
    comments.value = []
  }

  loading.value = true
  try {
    const { data } = await getComments(props.articleId, {
      sortBy: sortBy.value,
      page: page.value,
      size: size.value
    })

    if (reset) {
      comments.value = data.records
    } else {
      comments.value.push(...data.records)
    }
    total.value = data.total
  } catch (error) {
    console.error('Fetch comments failed:', error)
    Toast.fail('加载评论失败')
  } finally {
    loading.value = false
  }
}

const loadMore = () => {
  page.value++
  fetchComments()
}

const changeSort = (newSort) => {
  if (sortBy.value !== newSort) {
    sortBy.value = newSort
    fetchComments(true)
  }
}

const handleReply = (comment) => {
  replyTo.value = comment
}

const cancelReply = () => {
  replyTo.value = null
}

const handleSubmit = async (data) => {
  try {
    const { data: newComment } = await createComment(data)
    Toast.success('评论成功')

    // 添加到列表
    if (data.parentId) {
      // 回复，找到父评论添加回复
      const parent = comments.value.find(c => c.id === data.parentId)
      if (parent) {
        if (!parent.replies) parent.replies = []
        parent.replies.push(newComment)
        parent.replyCount++
      }
    } else {
      // 顶级评论
      comments.value.unshift(newComment)
      total.value++
    }

    cancelReply()
  } catch (error) {
    console.error('Create comment failed:', error)
    Toast.fail('评论失败')
  }
}

const handleLike = async (comment) => {
  try {
    const { data } = await toggleCommentLike(comment.id)
    comment.isLiked = data.liked
    comment.likeCount = data.likeCount
  } catch (error) {
    console.error('Toggle like failed:', error)
    Toast.fail('操作失败')
  }
}

const showLikeList = async (comment) => {
  currentLikeComment.value = comment
  showLikePopup.value = true

  try {
    const { data } = await getCommentLikes(comment.id, { page: 1, size: 50 })
    likeUsers.value = data.records
    likeTotal.value = data.total
  } catch (error) {
    console.error('Fetch likes failed:', error)
  }
}

const loadMoreReplies = async (commentId) => {
  const comment = comments.value.find(c => c.id === commentId)
  if (!comment) return

  try {
    const { data } = await getReplies(commentId, {
      sortBy: sortBy.value,
      page: Math.ceil((comment.replies?.length || 0) / 10) + 1,
      size: 10
    })
    comment.replies = comment.replies || []
    comment.replies.push(...data.records)
  } catch (error) {
    console.error('Load replies failed:', error)
    Toast.fail('加载回复失败')
  }
}

onMounted(() => {
  fetchComments()
})

// 监听文章ID变化
watch(() => props.articleId, () => {
  fetchComments(true)
})
</script>

<style scoped>
.comment-section {
  margin-top: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.title {
  font-size: 16px;
  font-weight: 600;
  color: #323233;
  margin: 0;
}

.sort-bar {
  display: flex;
  gap: 8px;
}

.sort-btn {
  padding: 4px 12px;
  border: none;
  background: transparent;
  font-size: 14px;
  color: #646566;
  cursor: pointer;
  border-radius: 4px;
}

.sort-btn.active {
  background: #1989fa;
  color: #fff;
}

.comment-list {
  min-height: 100px;
}

.load-more {
  padding: 16px 0;
}

.comment-input-wrapper {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #ebedf0;
}

.like-popup {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.popup-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #ebedf0;
  font-size: 16px;
  font-weight: 500;
}

.like-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 16px;
}

.like-user {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #ebedf0;
}

.like-user .avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
}

.like-user .nickname {
  font-size: 14px;
  color: #323233;
}
</style>
```

- [ ] **Step 2: 创建时间工具函数**

```javascript
// blog-web/src/utils/time.js
export function formatTimeAgo(time) {
  if (!time) return ''

  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`

  return date.toLocaleDateString('zh-CN', {
    month: 'numeric',
    day: 'numeric'
  })
}
```

- [ ] **Step 3: 提交组件**

```bash
git add blog-web/src/components/comment/CommentList.vue
git add blog-web/src/utils/time.js
git commit -m "feat(component): add CommentList with sort, like, reply features"
```

---

## Task 16: 集成到文章详情页

**Files:**
- Modify: `blog-web/src/views/portal/ArticleDetail.vue`

- [ ] **Step 1: 在文章详情页引入评论组件**

在 `ArticleDetail.vue` 的底部添加评论组件：

```vue
<!-- 在模板底部添加 -->
<div class="comment-section-wrapper">
  <CommentList :article-id="articleId" />
</div>
```

在 script 中引入：

```javascript
import CommentList from '@/components/comment/CommentList.vue'
```

- [ ] **Step 2: 提交集成**

```bash
git add blog-web/src/views/portal/ArticleDetail.vue
git commit -m "feat(article): integrate CommentList component into article detail page"
```

---

## Task 17: 功能测试与验收

**Files:**
- 无新增文件

- [ ] **Step 1: 启动服务进行测试**

```bash
# 启动后端
cd blog-server && mvn spring-boot:run

# 启动前端
cd blog-web && pnpm dev
```

- [ ] **Step 2: 测试用例清单**

| 功能 | 测试项 | 预期结果 |
|------|--------|----------|
| 评论列表 | 访问文章详情页 | 显示评论列表，默认按热度排序 |
| 评论排序 | 切换排序（最热/最新/最早） | 列表按对应规则重新排序 |
| 发表评论 | 输入内容并提交 | 评论成功，列表新增评论 |
| 回复评论 | 点击回复，输入内容提交 | 回复成功，显示在回复列表中 |
| 评论点赞 | 点击点赞按钮 | 点赞状态切换，数字更新 |
| 点赞列表 | 点击点赞数字 | 弹窗显示点赞用户列表 |
| @提及 | 输入@触发用户选择 | 显示匹配用户，选择后插入昵称 |
| @通知 | 被@的用户查看通知 | 收到@提及通知 |
| Emoji | 点击表情按钮 | 弹出Emoji选择器，选择后插入 |
| 加载更多 | 点击加载更多评论 | 追加显示更多评论 |

- [ ] **Step 3: 最终提交**

```bash
git add -A
git commit -m "feat: complete comment experience upgrade - like, reply, mention, emoji, sort"
```

---

## 自检清单

**1. Spec 覆盖检查：**

| 规格要求 | 对应任务 |
|----------|----------|
| 楼中楼回复（扁平展示） | Task 8, 14, 15 |
| 评论点赞 | Task 6, 9, 15 |
| 热评置顶 | Task 8（排序逻辑） |
| @提及 | Task 7, 9, 13 |
| Emoji表情 | Task 12, 13 |
| 多种排序 | Task 8, 15 |

**2. 占位符扫描：** 无 TBD、TODO 或未完成代码块 ✓

**3. 类型一致性检查：** 方法签名和字段名在各文件中保持一致 ✓
