# 用户关注与站内通知系统实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为博客系统添加用户关注系统和站内通知系统，实现用户社交互动能力。

**Architecture:** 后端使用 Spring Boot + MyBatis Plus，遵循现有分层架构（Entity、Mapper、Service、Controller）。前端使用 Vue 3 + Vant 4，遵循现有组件和页面结构。通知采用异步生成方式，避免阻塞主流程。

**Tech Stack:** Spring Boot 3.2.0, MyBatis Plus 3.5.5, Vue 3.5, Vant 4, Pinia

---

## 文件结构

### 后端新增文件

| 文件路径 | 职责 |
|---------|------|
| `blog-server/src/main/java/com/blog/domain/entity/UserFollow.java` | 关注关系实体 |
| `blog-server/src/main/java/com/blog/domain/entity/Notification.java` | 通知实体 |
| `blog-server/src/main/java/com/blog/domain/entity/Announcement.java` | 系统公告实体 |
| `blog-server/src/main/java/com/blog/domain/vo/FollowVO.java` | 关注列表VO |
| `blog-server/src/main/java/com/blog/domain/vo/NotificationVO.java` | 通知列表VO |
| `blog-server/src/main/java/com/blog/domain/vo/AnnouncementVO.java` | 公告VO |
| `blog-server/src/main/java/com/blog/repository/mapper/UserFollowMapper.java` | 关注Mapper |
| `blog-server/src/main/java/com/blog/repository/mapper/NotificationMapper.java` | 通知Mapper |
| `blog-server/src/main/java/com/blog/repository/mapper/AnnouncementMapper.java` | 公告Mapper |
| `blog-server/src/main/java/com/blog/service/UserFollowService.java` | 关注服务接口 |
| `blog-server/src/main/java/com/blog/service/NotificationService.java` | 通知服务接口 |
| `blog-server/src/main/java/com/blog/service/AnnouncementService.java` | 公告服务接口 |
| `blog-server/src/main/java/com/blog/service/impl/UserFollowServiceImpl.java` | 关注服务实现 |
| `blog-server/src/main/java/com/blog/service/impl/NotificationServiceImpl.java` | 通知服务实现 |
| `blog-server/src/main/java/com/blog/service/impl/AnnouncementServiceImpl.java` | 公告服务实现 |
| `blog-server/src/main/java/com/blog/controller/portal/FollowController.java` | 关注API控制器 |
| `blog-server/src/main/java/com/blog/controller/portal/NotificationController.java` | 通知API控制器 |
| `blog-server/src/main/java/com/blog/controller/admin/AnnouncementController.java` | 公告管理API控制器 |

### 后端修改文件

| 文件路径 | 修改内容 |
|---------|---------|
| `blog-server/src/main/resources/db/schema.sql` | 添加新表DDL |
| `blog-server/src/main/java/com/blog/domain/entity/User.java` | 添加关注数/粉丝数字段 |
| `blog-server/src/main/java/com/blog/service/impl/ArticleServiceImpl.java` | 发布文章时生成关注通知 |

### 前端新增文件

| 文件路径 | 职责 |
|---------|------|
| `blog-web/src/api/follow.js` | 关注API |
| `blog-web/src/api/notification.js` | 通知API |
| `blog-web/src/api/announcement.js` | 公告API |
| `blog-web/src/views/portal/UserFollowing.vue` | 关注列表页 |
| `blog-web/src/views/portal/UserFollowers.vue` | 粉丝列表页 |
| `blog-web/src/views/portal/Notification.vue` | 通知中心页 |
| `blog-web/src/views/admin/AnnouncementManage.vue` | 公告管理页 |
| `blog-web/src/components/FollowButton.vue` | 关注按钮组件 |

### 前端修改文件

| 文件路径 | 修改内容 |
|---------|---------|
| `blog-web/src/router/index.js` | 添加新路由 |
| `blog-web/src/views/portal/UserCenter.vue` | 添加关注/粉丝Tab |
| `blog-web/src/views/portal/Layout.vue` | 添加通知图标和未读角标 |
| `blog-web/src/views/portal/ArticleDetail.vue` | 添加作者关注按钮 |
| `blog-web/src/views/portal/Home.vue` | 添加关注筛选 |
| `blog-web/src/stores/user.js` | 添加未读通知数状态 |

---

## Task 1: 数据库表创建

**Files:**
- Modify: `blog-server/src/main/resources/db/schema.sql`

- [ ] **Step 1: 添加用户关注表DDL**

在 `schema.sql` 文件末尾添加：

```sql
-- =============================================
-- 17. 用户关注表
-- =============================================
DROP TABLE IF EXISTS `user_follow`;
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

-- =============================================
-- 18. 通知表
-- =============================================
DROP TABLE IF EXISTS `notification`;
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

-- =============================================
-- 19. 系统公告表
-- =============================================
DROP TABLE IF EXISTS `announcement`;
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

-- 为用户表添加关注数和粉丝数字段
ALTER TABLE `sys_user` ADD COLUMN `follower_count` INT DEFAULT 0 COMMENT '粉丝数';
ALTER TABLE `sys_user` ADD COLUMN `following_count` INT DEFAULT 0 COMMENT '关注数';
```

- [ ] **Step 2: 提交数据库变更**

```bash
git add blog-server/src/main/resources/db/schema.sql
git commit -m "feat: add user_follow, notification, announcement tables and user count fields"
```

---

## Task 2: 后端关注系统 - 实体层

**Files:**
- Create: `blog-server/src/main/java/com/blog/domain/entity/UserFollow.java`
- Modify: `blog-server/src/main/java/com/blog/domain/entity/User.java`

- [ ] **Step 1: 创建 UserFollow 实体**

```java
package com.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("user_follow")
public class UserFollow implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long followerId;

    private Long followingId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
```

- [ ] **Step 2: 修改 User 实体添加计数字段**

在 `User.java` 中添加：

```java
private Integer followerCount;

private Integer followingCount;
```

- [ ] **Step 3: 提交实体变更**

```bash
git add blog-server/src/main/java/com/blog/domain/entity/UserFollow.java
git add blog-server/src/main/java/com/blog/domain/entity/User.java
git commit -m "feat: add UserFollow entity and follower/following count fields to User"
```

---

## Task 3: 后端关注系统 - Mapper层

**Files:**
- Create: `blog-server/src/main/java/com/blog/repository/mapper/UserFollowMapper.java`

- [ ] **Step 1: 创建 UserFollowMapper**

```java
package com.blog.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.domain.entity.UserFollow;
import com.blog.domain.vo.FollowVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserFollowMapper extends BaseMapper<UserFollow> {

    @Select("""
        SELECT u.id, u.username, u.nickname, u.avatar, uf.create_time AS followTime
        FROM user_follow uf
        INNER JOIN sys_user u ON uf.following_id = u.id
        WHERE uf.follower_id = #{userId} AND u.deleted = 0
        ORDER BY uf.create_time DESC
        """)
    List<FollowVO> selectFollowingList(@Param("userId") Long userId);

    @Select("""
        SELECT u.id, u.username, u.nickname, u.avatar, uf.create_time AS followTime
        FROM user_follow uf
        INNER JOIN sys_user u ON uf.follower_id = u.id
        WHERE uf.following_id = #{userId} AND u.deleted = 0
        ORDER BY uf.create_time DESC
        """)
    List<FollowVO> selectFollowerList(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM user_follow WHERE follower_id = #{userId}")
    Integer countFollowing(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM user_follow WHERE following_id = #{userId}")
    Integer countFollowers(@Param("userId") Long userId);
}
```

- [ ] **Step 2: 创建 FollowVO**

```java
package com.blog.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FollowVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private LocalDateTime followTime;
}
```

- [ ] **Step 3: 提交 Mapper 变更**

```bash
git add blog-server/src/main/java/com/blog/repository/mapper/UserFollowMapper.java
git add blog-server/src/main/java/com/blog/domain/vo/FollowVO.java
git commit -m "feat: add UserFollowMapper and FollowVO"
```

---

## Task 4: 后端关注系统 - Service层

**Files:**
- Create: `blog-server/src/main/java/com/blog/service/UserFollowService.java`
- Create: `blog-server/src/main/java/com/blog/service/impl/UserFollowServiceImpl.java`

- [ ] **Step 1: 创建 UserFollowService 接口**

```java
package com.blog.service;

import com.blog.domain.vo.FollowVO;
import java.util.List;

public interface UserFollowService {

    void follow(Long userId, Long targetUserId);

    void unfollow(Long userId, Long targetUserId);

    boolean isFollowing(Long userId, Long targetUserId);

    List<FollowVO> getFollowingList(Long userId);

    List<FollowVO> getFollowerList(Long userId);

    Integer getFollowingCount(Long userId);

    Integer getFollowerCount(Long userId);
}
```

- [ ] **Step 2: 创建 UserFollowServiceImpl 实现**

```java
package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.exception.BusinessException;
import com.blog.common.result.ErrorCode;
import com.blog.domain.entity.User;
import com.blog.domain.entity.UserFollow;
import com.blog.domain.vo.FollowVO;
import com.blog.repository.mapper.UserFollowMapper;
import com.blog.repository.mapper.UserMapper;
import com.blog.service.UserFollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserFollowServiceImpl implements UserFollowService {

    private final UserFollowMapper userFollowMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public void follow(Long userId, Long targetUserId) {
        if (userId.equals(targetUserId)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不能关注自己");
        }

        User targetUser = userMapper.selectById(targetUserId);
        if (targetUser == null || targetUser.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 检查是否已关注
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getFollowerId, userId)
               .eq(UserFollow::getFollowingId, targetUserId);
        if (userFollowMapper.selectCount(wrapper) > 0) {
            return; // 已关注，幂等处理
        }

        // 创建关注关系
        UserFollow userFollow = new UserFollow();
        userFollow.setFollowerId(userId);
        userFollow.setFollowingId(targetUserId);
        userFollowMapper.insert(userFollow);

        // 更新计数
        userMapper.updateFollowingCount(userId, 1);
        userMapper.updateFollowerCount(targetUserId, 1);
    }

    @Override
    @Transactional
    public void unfollow(Long userId, Long targetUserId) {
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getFollowerId, userId)
               .eq(UserFollow::getFollowingId, targetUserId);

        UserFollow userFollow = userFollowMapper.selectOne(wrapper);
        if (userFollow == null) {
            return; // 未关注，幂等处理
        }

        userFollowMapper.deleteById(userFollow.getId());

        // 更新计数
        userMapper.updateFollowingCount(userId, -1);
        userMapper.updateFollowerCount(targetUserId, -1);
    }

    @Override
    public boolean isFollowing(Long userId, Long targetUserId) {
        if (userId == null || targetUserId == null) {
            return false;
        }
        LambdaQueryWrapper<UserFollow> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserFollow::getFollowerId, userId)
               .eq(UserFollow::getFollowingId, targetUserId);
        return userFollowMapper.selectCount(wrapper) > 0;
    }

    @Override
    public List<FollowVO> getFollowingList(Long userId) {
        return userFollowMapper.selectFollowingList(userId);
    }

    @Override
    public List<FollowVO> getFollowerList(Long userId) {
        return userFollowMapper.selectFollowerList(userId);
    }

    @Override
    public Integer getFollowingCount(Long userId) {
        return userFollowMapper.countFollowing(userId);
    }

    @Override
    public Integer getFollowerCount(Long userId) {
        return userFollowMapper.countFollowers(userId);
    }
}
```

- [ ] **Step 3: 在 UserMapper 添加计数更新方法**

在 `UserMapper.java` 添加：

```java
@Update("UPDATE sys_user SET following_count = following_count + #{delta} WHERE id = #{userId}")
int updateFollowingCount(@Param("userId") Long userId, @Param("delta") int delta);

@Update("UPDATE sys_user SET follower_count = follower_count + #{delta} WHERE id = #{userId}")
int updateFollowerCount(@Param("userId") Long userId, @Param("delta") int delta);
```

- [ ] **Step 4: 提交 Service 变更**

```bash
git add blog-server/src/main/java/com/blog/service/UserFollowService.java
git add blog-server/src/main/java/com/blog/service/impl/UserFollowServiceImpl.java
git add blog-server/src/main/java/com/blog/repository/mapper/UserMapper.java
git commit -m "feat: add UserFollowService with follow/unfollow functionality"
```

---

## Task 5: 后端关注系统 - Controller层

**Files:**
- Create: `blog-server/src/main/java/com/blog/controller/portal/FollowController.java`

- [ ] **Step 1: 创建 FollowController**

```java
package com.blog.controller.portal;

import com.blog.common.result.Result;
import com.blog.domain.vo.FollowVO;
import com.blog.security.LoginUser;
import com.blog.service.UserFollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/portal")
@RequiredArgsConstructor
public class FollowController {

    private final UserFollowService userFollowService;

    @PostMapping("/follow/{userId}")
    public Result<Void> follow(@PathVariable Long userId) {
        Long currentUserId = getCurrentUserId();
        userFollowService.follow(currentUserId, userId);
        return Result.success();
    }

    @DeleteMapping("/follow/{userId}")
    public Result<Void> unfollow(@PathVariable Long userId) {
        Long currentUserId = getCurrentUserId();
        userFollowService.unfollow(currentUserId, userId);
        return Result.success();
    }

    @GetMapping("/follow/check/{userId}")
    public Result<Boolean> checkFollow(@PathVariable Long userId) {
        Long currentUserId = getCurrentUserId();
        boolean isFollowing = userFollowService.isFollowing(currentUserId, userId);
        return Result.success(isFollowing);
    }

    @GetMapping("/following/{userId}")
    public Result<Map<String, Object>> getFollowingList(@PathVariable Long userId) {
        List<FollowVO> list = userFollowService.getFollowingList(userId);
        Integer count = userFollowService.getFollowingCount(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", count);
        return Result.success(result);
    }

    @GetMapping("/followers/{userId}")
    public Result<Map<String, Object>> getFollowerList(@PathVariable Long userId) {
        List<FollowVO> list = userFollowService.getFollowerList(userId);
        Integer count = userFollowService.getFollowerCount(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", count);
        return Result.success(result);
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

- [ ] **Step 2: 提交 Controller 变更**

```bash
git add blog-server/src/main/java/com/blog/controller/portal/FollowController.java
git commit -m "feat: add FollowController with follow/unfollow/check/list APIs"
```

---

## Task 6: 后端通知系统 - 实体层

**Files:**
- Create: `blog-server/src/main/java/com/blog/domain/entity/Notification.java`
- Create: `blog-server/src/main/java/com/blog/domain/entity/Announcement.java`
- Create: `blog-server/src/main/java/com/blog/domain/vo/NotificationVO.java`
- Create: `blog-server/src/main/java/com/blog/domain/vo/AnnouncementVO.java`

- [ ] **Step 1: 创建 Notification 实体**

```java
package com.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("notification")
public class Notification implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Integer type;

    private String title;

    private String content;

    private Long relatedId;

    private Long senderId;

    private Integer isRead;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
```

- [ ] **Step 2: 创建 Announcement 实体**

```java
package com.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("announcement")
public class Announcement implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    private Integer status;

    private LocalDateTime publishTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
```

- [ ] **Step 3: 创建 NotificationVO**

```java
package com.blog.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationVO {
    private Long id;
    private Integer type;
    private String title;
    private String content;
    private Long relatedId;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private Integer isRead;
    private LocalDateTime createTime;
}
```

- [ ] **Step 4: 创建 AnnouncementVO**

```java
package com.blog.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AnnouncementVO {
    private Long id;
    private String title;
    private String content;
    private Integer status;
    private LocalDateTime publishTime;
    private LocalDateTime createTime;
}
```

- [ ] **Step 5: 提交实体变更**

```bash
git add blog-server/src/main/java/com/blog/domain/entity/Notification.java
git add blog-server/src/main/java/com/blog/domain/entity/Announcement.java
git add blog-server/src/main/java/com/blog/domain/vo/NotificationVO.java
git add blog-server/src/main/java/com/blog/domain/vo/AnnouncementVO.java
git commit -m "feat: add Notification, Announcement entities and VOs"
```

---

## Task 7: 后端通知系统 - Mapper层

**Files:**
- Create: `blog-server/src/main/java/com/blog/repository/mapper/NotificationMapper.java`
- Create: `blog-server/src/main/java/com/blog/repository/mapper/AnnouncementMapper.java`

- [ ] **Step 1: 创建 NotificationMapper**

```java
package com.blog.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.domain.entity.Notification;
import com.blog.domain.vo.NotificationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    @Select("""
        SELECT n.id, n.type, n.title, n.content, n.related_id, n.sender_id,
               u.nickname AS senderName, u.avatar AS senderAvatar, n.is_read, n.create_time
        FROM notification n
        LEFT JOIN sys_user u ON n.sender_id = u.id
        WHERE n.user_id = #{userId}
        ORDER BY n.create_time DESC
        """)
    List<NotificationVO> selectNotificationList(@Param("userId") Long userId);

    @Select("""
        SELECT n.id, n.type, n.title, n.content, n.related_id, n.sender_id,
               u.nickname AS senderName, u.avatar AS senderAvatar, n.is_read, n.create_time
        FROM notification n
        LEFT JOIN sys_user u ON n.sender_id = u.id
        WHERE n.user_id = #{userId} AND n.type = #{type}
        ORDER BY n.create_time DESC
        """)
    List<NotificationVO> selectNotificationListByType(@Param("userId") Long userId, @Param("type") Integer type);

    @Select("SELECT COUNT(*) FROM notification WHERE user_id = #{userId} AND is_read = 0")
    Integer countUnread(@Param("userId") Long userId);

    @Update("UPDATE notification SET is_read = 1 WHERE id = #{id} AND user_id = #{userId}")
    int markAsRead(@Param("id") Long id, @Param("userId") Long userId);

    @Update("UPDATE notification SET is_read = 1 WHERE user_id = #{userId} AND is_read = 0")
    int markAllAsRead(@Param("userId") Long userId);
}
```

- [ ] **Step 2: 创建 AnnouncementMapper**

```java
package com.blog.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.domain.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {

    @Select("SELECT * FROM announcement WHERE status = 1 AND deleted = 0 ORDER BY publish_time DESC")
    List<Announcement> selectPublishedAnnouncements();
}
```

- [ ] **Step 3: 提交 Mapper 变更**

```bash
git add blog-server/src/main/java/com/blog/repository/mapper/NotificationMapper.java
git add blog-server/src/main/java/com/blog/repository/mapper/AnnouncementMapper.java
git commit -m "feat: add NotificationMapper and AnnouncementMapper"
```

---

## Task 8: 后端通知系统 - Service层

**Files:**
- Create: `blog-server/src/main/java/com/blog/service/NotificationService.java`
- Create: `blog-server/src/main/java/com/blog/service/impl/NotificationServiceImpl.java`
- Create: `blog-server/src/main/java/com/blog/service/AnnouncementService.java`
- Create: `blog-server/src/main/java/com/blog/service/impl/AnnouncementServiceImpl.java`

- [ ] **Step 1: 创建 NotificationService 接口**

```java
package com.blog.service;

import com.blog.domain.vo.NotificationVO;
import java.util.List;

public interface NotificationService {

    List<NotificationVO> getNotificationList(Long userId, Integer type);

    Integer getUnreadCount(Long userId);

    void markAsRead(Long userId, Long notificationId);

    void markAllAsRead(Long userId);

    void createFollowNotification(Long userId, Long articleId, String articleTitle, Long senderId, String senderName);

    void createCommentNotification(Long userId, Long articleId, String articleTitle, Long senderId, String senderName);

    void createReplyNotification(Long userId, Long commentId, Long senderId, String senderName);

    void createAnnouncementNotification(Long announcementId, String title);
}
```

- [ ] **Step 2: 创建 NotificationServiceImpl 实现**

```java
package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.domain.entity.Announcement;
import com.blog.domain.entity.Notification;
import com.blog.domain.entity.User;
import com.blog.domain.vo.NotificationVO;
import com.blog.repository.mapper.AnnouncementMapper;
import com.blog.repository.mapper.NotificationMapper;
import com.blog.repository.mapper.UserMapper;
import com.blog.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;
    private final AnnouncementMapper announcementMapper;

    @Override
    public List<NotificationVO> getNotificationList(Long userId, Integer type) {
        if (type != null && type > 0) {
            return notificationMapper.selectNotificationListByType(userId, type);
        }
        return notificationMapper.selectNotificationList(userId);
    }

    @Override
    public Integer getUnreadCount(Long userId) {
        return notificationMapper.countUnread(userId);
    }

    @Override
    public void markAsRead(Long userId, Long notificationId) {
        notificationMapper.markAsRead(notificationId, userId);
    }

    @Override
    public void markAllAsRead(Long userId) {
        notificationMapper.markAllAsRead(userId);
    }

    @Override
    @Async
    public void createFollowNotification(Long userId, Long articleId, String articleTitle, Long senderId, String senderName) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(1);
        notification.setTitle("关注的作者发布了新文章");
        notification.setContent(senderName + " 发布了《" + truncateTitle(articleTitle) + "》");
        notification.setRelatedId(articleId);
        notification.setSenderId(senderId);
        notification.setIsRead(0);
        notificationMapper.insert(notification);
    }

    @Override
    @Async
    public void createCommentNotification(Long userId, Long articleId, String articleTitle, Long senderId, String senderName) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(2);
        notification.setTitle("文章收到新评论");
        notification.setContent(senderName + " 评论了《" + truncateTitle(articleTitle) + "》");
        notification.setRelatedId(articleId);
        notification.setSenderId(senderId);
        notification.setIsRead(0);
        notificationMapper.insert(notification);
    }

    @Override
    @Async
    public void createReplyNotification(Long userId, Long commentId, Long senderId, String senderName) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(3);
        notification.setTitle("评论收到回复");
        notification.setContent(senderName + " 回复了你的评论");
        notification.setRelatedId(commentId);
        notification.setSenderId(senderId);
        notification.setIsRead(0);
        notificationMapper.insert(notification);
    }

    @Override
    @Async
    public void createAnnouncementNotification(Long announcementId, String title) {
        Announcement announcement = announcementMapper.selectById(announcementId);
        if (announcement == null || announcement.getStatus() != 1) {
            return;
        }

        // 为所有用户创建通知（分批处理避免内存问题）
        int pageSize = 1000;
        int pageNo = 1;
        long total = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getDeleted, 0));

        while ((long) (pageNo - 1) * pageSize < total) {
            List<User> users = userMapper.selectList(
                new LambdaQueryWrapper<User>()
                    .eq(User::getDeleted, 0)
                    .last("LIMIT " + pageSize + " OFFSET " + (pageNo - 1) * pageSize)
            );

            for (User user : users) {
                Notification notification = new Notification();
                notification.setUserId(user.getId());
                notification.setType(4);
                notification.setTitle("系统公告");
                notification.setContent(title);
                notification.setRelatedId(announcementId);
                notification.setSenderId(null);
                notification.setIsRead(0);
                notificationMapper.insert(notification);
            }
            pageNo++;
        }
    }

    private String truncateTitle(String title) {
        if (title == null) return "";
        return title.length() > 20 ? title.substring(0, 20) + "..." : title;
    }
}
```

- [ ] **Step 3: 创建 AnnouncementService 接口**

```java
package com.blog.service;

import com.blog.domain.entity.Announcement;
import java.util.List;

public interface AnnouncementService {

    List<Announcement> getAnnouncementList();

    Announcement getAnnouncementById(Long id);

    void saveOrUpdateAnnouncement(Announcement announcement);

    void deleteAnnouncement(Long id);

    void publishAnnouncement(Long id);
}
```

- [ ] **Step 4: 创建 AnnouncementServiceImpl 实现**

```java
package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.exception.BusinessException;
import com.blog.common.result.ErrorCode;
import com.blog.domain.entity.Announcement;
import com.blog.repository.mapper.AnnouncementMapper;
import com.blog.service.AnnouncementService;
import com.blog.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementMapper announcementMapper;
    private final NotificationService notificationService;

    @Override
    public List<Announcement> getAnnouncementList() {
        return announcementMapper.selectList(
            new LambdaQueryWrapper<Announcement>()
                .orderByDesc(Announcement::getCreateTime)
        );
    }

    @Override
    public Announcement getAnnouncementById(Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "公告不存在");
        }
        return announcement;
    }

    @Override
    public void saveOrUpdateAnnouncement(Announcement announcement) {
        if (announcement.getId() == null) {
            announcement.setStatus(0); // 默认草稿
            announcementMapper.insert(announcement);
        } else {
            announcementMapper.updateById(announcement);
        }
    }

    @Override
    public void deleteAnnouncement(Long id) {
        announcementMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void publishAnnouncement(Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "公告不存在");
        }

        announcement.setStatus(1);
        announcement.setPublishTime(LocalDateTime.now());
        announcementMapper.updateById(announcement);

        // 异步创建通知
        notificationService.createAnnouncementNotification(id, announcement.getTitle());
    }
}
```

- [ ] **Step 5: 提交 Service 变更**

```bash
git add blog-server/src/main/java/com/blog/service/NotificationService.java
git add blog-server/src/main/java/com/blog/service/impl/NotificationServiceImpl.java
git add blog-server/src/main/java/com/blog/service/AnnouncementService.java
git add blog-server/src/main/java/com/blog/service/impl/AnnouncementServiceImpl.java
git commit -m "feat: add NotificationService and AnnouncementService"
```

---

## Task 9: 后端通知系统 - Controller层

**Files:**
- Create: `blog-server/src/main/java/com/blog/controller/portal/NotificationController.java`
- Create: `blog-server/src/main/java/com/blog/controller/admin/AnnouncementController.java`

- [ ] **Step 1: 创建 NotificationController**

```java
package com.blog.controller.portal;

import com.blog.common.result.Result;
import com.blog.domain.vo.NotificationVO;
import com.blog.security.LoginUser;
import com.blog.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/portal")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/notifications")
    public Result<Map<String, Object>> getNotifications(@RequestParam(required = false) Integer type) {
        Long userId = getCurrentUserId();
        List<NotificationVO> list = notificationService.getNotificationList(userId, type);

        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        return Result.success(result);
    }

    @GetMapping("/notifications/unread-count")
    public Result<Integer> getUnreadCount() {
        Long userId = getCurrentUserId();
        Integer count = notificationService.getUnreadCount(userId);
        return Result.success(count);
    }

    @PutMapping("/notifications/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        notificationService.markAsRead(userId, id);
        return Result.success();
    }

    @PutMapping("/notifications/read-all")
    public Result<Void> markAllAsRead() {
        Long userId = getCurrentUserId();
        notificationService.markAllAsRead(userId);
        return Result.success();
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

- [ ] **Step 2: 创建 AnnouncementController**

```java
package com.blog.controller.admin;

import com.blog.common.result.Result;
import com.blog.domain.entity.Announcement;
import com.blog.service.AnnouncementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/announcements")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @GetMapping
    public Result<List<Announcement>> getList() {
        return Result.success(announcementService.getAnnouncementList());
    }

    @GetMapping("/{id}")
    public Result<Announcement> getById(@PathVariable Long id) {
        return Result.success(announcementService.getAnnouncementById(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody Announcement announcement) {
        announcementService.saveOrUpdateAnnouncement(announcement);
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Announcement announcement) {
        announcement.setId(id);
        announcementService.saveOrUpdateAnnouncement(announcement);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        announcementService.deleteAnnouncement(id);
        return Result.success();
    }

    @PutMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id) {
        announcementService.publishAnnouncement(id);
        return Result.success();
    }
}
```

- [ ] **Step 3: 提交 Controller 变更**

```bash
git add blog-server/src/main/java/com/blog/controller/portal/NotificationController.java
git add blog-server/src/main/java/com/blog/controller/admin/AnnouncementController.java
git commit -m "feat: add NotificationController and AnnouncementController"
```

---

## Task 10: 后端集成 - 文章发布时生成关注通知

**Files:**
- Modify: `blog-server/src/main/java/com/blog/service/impl/ArticleServiceImpl.java`

- [ ] **Step 1: 在 ArticleServiceImpl 注入 NotificationService 和 UserFollowMapper**

在 `ArticleServiceImpl.java` 的依赖注入中添加：

```java
private final NotificationService notificationService;
private final UserFollowMapper userFollowMapper;
```

- [ ] **Step 2: 在文章发布后生成关注通知**

修改 `doSaveOrUpdateArticle` 方法，在文章发布时生成通知。找到设置发布时间的位置后添加：

```java
// 发布时设置发布时间
if (article.getStatus() == 1 && article.getPublishTime() == null) {
    article.setPublishTime(LocalDateTime.now());

    // 新发布文章，通知粉丝
    if (dto.getId() == null) {
        // 异步通知粉丝
        notifyFollowers(article.getId(), article.getTitle());
    }
}
```

在类末尾添加辅助方法：

```java
@Async
private void notifyFollowers(Long articleId, String articleTitle) {
    Long authorId = getCurrentUserId();
    User author = userMapper.selectById(authorId);
    if (author == null) return;

    String authorName = author.getNickname() != null ? author.getNickname() : author.getUsername();

    // 获取粉丝列表
    List<Long> followerIds = userFollowMapper.selectList(
        new LambdaQueryWrapper<UserFollow>()
            .eq(UserFollow::getFollowingId, authorId)
    ).stream().map(UserFollow::getFollowerId).collect(Collectors.toList());

    // 为每个粉丝创建通知
    for (Long followerId : followerIds) {
        notificationService.createFollowNotification(followerId, articleId, articleTitle, authorId, authorName);
    }
}
```

- [ ] **Step 3: 添加必要的 import**

```java
import com.blog.domain.entity.UserFollow;
import com.blog.service.NotificationService;
import org.springframework.scheduling.annotation.Async;
```

- [ ] **Step 4: 提交变更**

```bash
git add blog-server/src/main/java/com/blog/service/impl/ArticleServiceImpl.java
git commit -m "feat: notify followers when publishing new article"
```

---

## Task 11: 前端关注系统 - API层

**Files:**
- Create: `blog-web/src/api/follow.js`

- [ ] **Step 1: 创建 follow.js API 模块**

```javascript
import request from '@/utils/request'

// 关注用户
export function followUser(userId) {
  return request.post(`/api/portal/follow/${userId}`)
}

// 取关用户
export function unfollowUser(userId) {
  return request.delete(`/api/portal/follow/${userId}`)
}

// 检查是否已关注
export function checkFollow(userId) {
  return request.get(`/api/portal/follow/check/${userId}`)
}

// 获取关注列表
export function getFollowing(userId) {
  return request.get(`/api/portal/following/${userId}`)
}

// 获取粉丝列表
export function getFollowers(userId) {
  return request.get(`/api/portal/followers/${userId}`)
}
```

- [ ] **Step 2: 提交 API 变更**

```bash
git add blog-web/src/api/follow.js
git commit -m "feat: add follow API module"
```

---

## Task 12: 前端关注系统 - 关注按钮组件

**Files:**
- Create: `blog-web/src/components/FollowButton.vue`

- [ ] **Step 1: 创建 FollowButton 组件**

```vue
<template>
  <button
    class="follow-btn"
    :class="{ following: isFollowing, loading }"
    :disabled="loading || isSelf"
    @click="handleClick"
  >
    <span v-if="loading" class="loading-spinner"></span>
    <template v-else>
      <svg v-if="!isFollowing" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4" />
      </svg>
      <span>{{ buttonText }}</span>
    </template>
  </button>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { followUser, unfollowUser, checkFollow } from '@/api/follow'
import { useUserStore } from '@/stores/user'

const props = defineProps({
  userId: {
    type: Number,
    required: true
  }
})

const userStore = useUserStore()
const isFollowing = ref(false)
const loading = ref(false)

const isSelf = computed(() => {
  return userStore.userInfo?.id === props.userId
})

const buttonText = computed(() => {
  if (isFollowing.value) return '已关注'
  return '关注'
})

const checkFollowStatus = async () => {
  if (!userStore.isLoggedIn || isSelf.value) return

  try {
    const res = await checkFollow(props.userId)
    isFollowing.value = res.data
  } catch (e) {
    console.error('检查关注状态失败', e)
  }
}

const handleClick = async () => {
  if (!userStore.isLoggedIn) {
    // 未登录时跳转登录页
    window.location.href = '/#/login'
    return
  }

  loading.value = true
  try {
    if (isFollowing.value) {
      await unfollowUser(props.userId)
      isFollowing.value = false
    } else {
      await followUser(props.userId)
      isFollowing.value = true
    }
  } catch (e) {
    console.error('关注操作失败', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  checkFollowStatus()
})
</script>

<style lang="scss" scoped>
.follow-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-1);
  padding: var(--space-2) var(--space-4);
  font-size: var(--text-sm);
  font-weight: var(--font-medium);
  color: white;
  background: var(--color-primary);
  border: none;
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: all var(--transition-fast);

  svg {
    width: 14px;
    height: 14px;
  }

  &:hover:not(:disabled):not(.following) {
    background: var(--color-accent);
  }

  &.following {
    background: var(--bg-secondary);
    color: var(--text-secondary);
    border: 1px solid var(--border-color);

    &:hover {
      color: var(--color-error);
      border-color: var(--color-error);
    }
  }

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}

.loading-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
```

- [ ] **Step 2: 提交组件变更**

```bash
git add blog-web/src/components/FollowButton.vue
git commit -m "feat: add FollowButton component"
```

---

## Task 13: 前端关注系统 - 用户中心关注/粉丝页面

**Files:**
- Create: `blog-web/src/views/portal/UserFollowing.vue`
- Create: `blog-web/src/views/portal/UserFollowers.vue`
- Modify: `blog-web/src/views/portal/UserCenter.vue`

- [ ] **Step 1: 创建 UserFollowing 页面**

```vue
<template>
  <div class="following-page">
    <div v-if="loading" class="loading-state">
      <span class="loading-spinner"></span>
      <span>加载中...</span>
    </div>

    <div v-else-if="list.length === 0" class="empty-state">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
        <path stroke-linecap="round" stroke-linejoin="round" d="M18 18.72a9.094 9.094 0 003.741-.479 3 3 0 00-4.682-2.72m.94 3.198l.001.031c0 .225-.012.447-.037.666A11.944 11.944 0 0112 21c-2.17 0-4.207-.576-5.963-1.584A6.062 6.062 0 016 18.719m12 0a5.971 5.971 0 00-.941-3.197m0 0A5.995 5.995 0 0012 12.75a5.995 5.995 0 00-5.058 2.772m0 0a3 3 0 00-4.681 2.72 8.986 8.986 0 003.74.477m.94-3.197a5.971 5.971 0 00-.94 3.197M15 6.75a3 3 0 11-6 0 3 3 0 016 0zm6 3a2.25 2.25 0 11-4.5 0 2.25 2.25 0 014.5 0zm-13.5 0a2.25 2.25 0 11-4.5 0 2.25 2.25 0 014.5 0z" />
      </svg>
      <p>暂无关注的用户</p>
    </div>

    <div v-else class="user-list">
      <div v-for="user in list" :key="user.id" class="user-item">
        <router-link :to="`/user/profile/${user.id}`" class="user-avatar">
          {{ user.nickname?.charAt(0) || user.username?.charAt(0) || 'U' }}
        </router-link>
        <div class="user-info">
          <router-link :to="`/user/profile/${user.id}`" class="user-name">
            {{ user.nickname || user.username }}
          </router-link>
          <span class="follow-time">{{ formatTime(user.followTime) }}</span>
        </div>
        <FollowButton :userId="user.id" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getFollowing } from '@/api/follow'
import FollowButton from '@/components/FollowButton.vue'

const route = useRoute()
const list = ref([])
const loading = ref(true)

const loadList = async () => {
  loading.value = true
  try {
    const userId = route.params.userId || route.matched[0].props?.default?.userId
    const res = await getFollowing(userId)
    list.value = res.data.list || []
  } catch (e) {
    console.error('获取关注列表失败', e)
  } finally {
    loading.value = false
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

onMounted(() => {
  loadList()
})
</script>

<style lang="scss" scoped>
.following-page {
  background: var(--bg-card);
  border-radius: var(--radius-xl);
  padding: var(--space-6);
}

.loading-state, .empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-12);
  color: var(--text-muted);

  svg {
    width: 48px;
    height: 48px;
    margin-bottom: var(--space-4);
    opacity: 0.5;
  }
}

.loading-spinner {
  width: 24px;
  height: 24px;
  border: 2px solid var(--border-color);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
  margin-bottom: var(--space-2);
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.user-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.user-item {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-4);
  border-radius: var(--radius-lg);
  transition: background var(--transition-fast);

  &:hover {
    background: var(--bg-hover);
  }
}

.user-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: white;
  background: var(--color-primary);
  border-radius: var(--radius-full);
  text-decoration: none;
  flex-shrink: 0;
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-name {
  display: block;
  font-size: var(--text-base);
  font-weight: var(--font-medium);
  color: var(--text-primary);
  text-decoration: none;

  &:hover {
    color: var(--color-primary);
  }
}

.follow-time {
  font-size: var(--text-xs);
  color: var(--text-muted);
}
</style>
```

- [ ] **Step 2: 创建 UserFollowers 页面**

```vue
<template>
  <div class="followers-page">
    <div v-if="loading" class="loading-state">
      <span class="loading-spinner"></span>
      <span>加载中...</span>
    </div>

    <div v-else-if="list.length === 0" class="empty-state">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
        <path stroke-linecap="round" stroke-linejoin="round" d="M18 18.72a9.094 9.094 0 003.741-.479 3 3 0 00-4.682-2.72m.94 3.198l.001.031c0 .225-.012.447-.037.666A11.944 11.944 0 0112 21c-2.17 0-4.207-.576-5.963-1.584A6.062 6.062 0 016 18.719m12 0a5.971 5.971 0 00-.941-3.197m0 0A5.995 5.995 0 0012 12.75a5.995 5.995 0 00-5.058 2.772m0 0a3 3 0 00-4.681 2.72 8.986 8.986 0 003.74.477m.94-3.197a5.971 5.971 0 00-.94 3.197M15 6.75a3 3 0 11-6 0 3 3 0 016 0zm6 3a2.25 2.25 0 11-4.5 0 2.25 2.25 0 014.5 0zm-13.5 0a2.25 2.25 0 11-4.5 0 2.25 2.25 0 014.5 0z" />
      </svg>
      <p>暂无粉丝</p>
    </div>

    <div v-else class="user-list">
      <div v-for="user in list" :key="user.id" class="user-item">
        <router-link :to="`/user/profile/${user.id}`" class="user-avatar">
          {{ user.nickname?.charAt(0) || user.username?.charAt(0) || 'U' }}
        </router-link>
        <div class="user-info">
          <router-link :to="`/user/profile/${user.id}`" class="user-name">
            {{ user.nickname || user.username }}
          </router-link>
          <span class="follow-time">{{ formatTime(user.followTime) }} 关注了你</span>
        </div>
        <FollowButton :userId="user.id" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getFollowers } from '@/api/follow'
import FollowButton from '@/components/FollowButton.vue'

const route = useRoute()
const list = ref([])
const loading = ref(true)

const loadList = async () => {
  loading.value = true
  try {
    const userId = route.params.userId || route.matched[0].props?.default?.userId
    const res = await getFollowers(userId)
    list.value = res.data.list || []
  } catch (e) {
    console.error('获取粉丝列表失败', e)
  } finally {
    loading.value = false
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

onMounted(() => {
  loadList()
})
</script>

<style lang="scss" scoped>
.followers-page {
  background: var(--bg-card);
  border-radius: var(--radius-xl);
  padding: var(--space-6);
}

.loading-state, .empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-12);
  color: var(--text-muted);

  svg {
    width: 48px;
    height: 48px;
    margin-bottom: var(--space-4);
    opacity: 0.5;
  }
}

.loading-spinner {
  width: 24px;
  height: 24px;
  border: 2px solid var(--border-color);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
  margin-bottom: var(--space-2);
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.user-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.user-item {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-4);
  border-radius: var(--radius-lg);
  transition: background var(--transition-fast);

  &:hover {
    background: var(--bg-hover);
  }
}

.user-avatar {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  color: white;
  background: var(--color-primary);
  border-radius: var(--radius-full);
  text-decoration: none;
  flex-shrink: 0;
}

.user-info {
  flex: 1;
  min-width: 0;
}

.user-name {
  display: block;
  font-size: var(--text-base);
  font-weight: var(--font-medium);
  color: var(--text-primary);
  text-decoration: none;

  &:hover {
    color: var(--color-primary);
  }
}

.follow-time {
  font-size: var(--text-xs);
  color: var(--text-muted);
}
</style>
```

- [ ] **Step 3: 修改 UserCenter.vue 添加关注/粉丝 Tab**

在 `tabs` 数组中添加两个新的 Tab：

```javascript
const tabs = [
  {
    path: '/user/profile',
    label: '个人资料',
    iconPath: 'M15.75 6a3.75 3.75 0 11-7.5 0 3.75 3.75 0 017.5 0zM4.501 20.118a7.5 7.5 0 0114.998 0A17.933 17.933 0 0112 21.75c-2.676 0-5.216-.584-7.499-1.632z'
  },
  {
    path: '/user/following',
    label: '关注',
    iconPath: 'M18 18.72a9.094 9.094 0 003.741-.479 3 3 0 00-4.682-2.72m.94 3.198l.001.031c0 .225-.012.447-.037.666A11.944 11.944 0 0112 21c-2.17 0-4.207-.576-5.963-1.584A6.062 6.062 0 016 18.719m12 0a5.971 5.971 0 00-.941-3.197m0 0A5.995 5.995 0 0012 12.75a5.995 5.995 0 00-5.058 2.772m0 0a3 3 0 00-4.681 2.72 8.986 8.986 0 003.74.477m.94-3.197a5.971 5.971 0 00-.94 3.197M15 6.75a3 3 0 11-6 0 3 3 0 016 0zm6 3a2.25 2.25 0 11-4.5 0 2.25 2.25 0 014.5 0zm-13.5 0a2.25 2.25 0 11-4.5 0 2.25 2.25 0 014.5 0z'
  },
  {
    path: '/user/followers',
    label: '粉丝',
    iconPath: 'M15 19.128a9.38 9.38 0 002.625.372 9.337 9.337 0 004.121-.952 4.125 4.125 0 00-7.533-2.493M15 19.128v-.003c0-1.113-.285-2.16-.786-3.07M15 19.128v.106A12.318 12.318 0 018.624 21c-2.331 0-4.512-.645-6.374-1.766l-.001-.109a6.375 6.375 0 0111.964-3.07M12 6.375a3.375 3.375 0 11-6.75 0 3.375 3.375 0 016.75 0zm8.25 2.25a2.625 2.625 0 11-5.25 0 2.625 2.625 0 015.25 0z'
  },
  {
    path: '/user/favorites',
    label: '我的收藏',
    iconPath: 'M17.593 3.322c1.1.128 1.907 1.077 1.907 2.185V21L12 17.25 4.5 21V5.507c0-1.108.806-2.057 1.907-2.185a48.507 48.507 0 0111.186 0z'
  },
  {
    path: '/user/history',
    label: '阅读历史',
    iconPath: 'M12 6v6h4.5m4.5 0a9 9 0 11-18 0 9 9 0 0118 0z'
  }
]
```

- [ ] **Step 4: 提交前端关注页面变更**

```bash
git add blog-web/src/views/portal/UserFollowing.vue
git add blog-web/src/views/portal/UserFollowers.vue
git add blog-web/src/views/portal/UserCenter.vue
git commit -m "feat: add following and followers pages in user center"
```

---

## Task 14: 前端通知系统 - API层

**Files:**
- Create: `blog-web/src/api/notification.js`
- Create: `blog-web/src/api/announcement.js`

- [ ] **Step 1: 创建 notification.js API 模块**

```javascript
import request from '@/utils/request'

// 获取通知列表
export function getNotifications(type) {
  return request.get('/api/portal/notifications', { params: { type } })
}

// 获取未读数量
export function getUnreadCount() {
  return request.get('/api/portal/notifications/unread-count')
}

// 标记单条已读
export function markAsRead(id) {
  return request.put(`/api/portal/notifications/${id}/read`)
}

// 全部标记已读
export function markAllAsRead() {
  return request.put('/api/portal/notifications/read-all')
}
```

- [ ] **Step 2: 创建 announcement.js API 模块**

```javascript
import request from '@/utils/request'

// 获取公告列表（管理端）
export function getAnnouncements() {
  return request.get('/api/admin/announcements')
}

// 获取公告详情
export function getAnnouncement(id) {
  return request.get(`/api/admin/announcements/${id}`)
}

// 创建公告
export function createAnnouncement(data) {
  return request.post('/api/admin/announcements', data)
}

// 更新公告
export function updateAnnouncement(id, data) {
  return request.put(`/api/admin/announcements/${id}`, data)
}

// 删除公告
export function deleteAnnouncement(id) {
  return request.delete(`/api/admin/announcements/${id}`)
}

// 发布公告
export function publishAnnouncement(id) {
  return request.put(`/api/admin/announcements/${id}/publish`)
}
```

- [ ] **Step 3: 提交 API 变更**

```bash
git add blog-web/src/api/notification.js
git add blog-web/src/api/announcement.js
git commit -m "feat: add notification and announcement API modules"
```

---

## Task 15: 前端通知系统 - 通知中心页面

**Files:**
- Create: `blog-web/src/views/portal/Notification.vue`

- [ ] **Step 1: 创建 Notification 页面**

```vue
<template>
  <div class="notification-page">
    <header class="page-header">
      <h1 class="page-title">通知中心</h1>
      <button v-if="unreadCount > 0" class="mark-all-btn" @click="handleMarkAllRead">
        全部已读
      </button>
    </header>

    <!-- 类型筛选 -->
    <nav class="type-tabs">
      <button
        v-for="tab in typeTabs"
        :key="tab.value"
        class="type-tab"
        :class="{ active: currentType === tab.value }"
        @click="changeType(tab.value)"
      >
        {{ tab.label }}
        <span v-if="tab.count > 0" class="count">{{ tab.count }}</span>
      </button>
    </nav>

    <!-- 通知列表 -->
    <div v-if="loading" class="loading-state">
      <span class="loading-spinner"></span>
    </div>

    <div v-else-if="list.length === 0" class="empty-state">
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
        <path stroke-linecap="round" stroke-linejoin="round" d="M14.857 17.082a23.848 23.848 0 005.454-1.31A8.967 8.967 0 0118 9.75v-.7V9A6 6 0 006 9v.75a8.967 8.967 0 01-2.312 6.022c1.733.64 3.56 1.085 5.455 1.31m5.714 0a24.255 24.255 0 01-5.714 0m5.714 0a3 3 0 11-5.714 0" />
      </svg>
      <p>暂无通知</p>
    </div>

    <div v-else class="notification-list">
      <div
        v-for="item in list"
        :key="item.id"
        class="notification-item"
        :class="{ unread: item.isRead === 0 }"
        @click="handleClick(item)"
      >
        <div class="notification-icon" :class="getTypeClass(item.type)">
          <svg v-if="item.type === 1" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M12 6.042A8.967 8.967 0 006 3.75c-1.052 0-2.062.18-3 .512v14.25A8.987 8.987 0 016 18c2.305 0 4.408.867 6 2.292m0-14.25a8.966 8.966 0 016-2.292c1.052 0 2.062.18 3 .512v14.25A8.987 8.987 0 0018 18a8.967 8.967 0 00-6 2.292m0-14.25v14.25" />
          </svg>
          <svg v-else-if="item.type === 2 || item.type === 3" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M12 20.25c4.97 0 9-3.694 9-8.25s-4.03-8.25-9-8.25S3 7.444 3 12c0 2.104.859 4.023 2.273 5.48.432.447.74 1.04.586 1.641a4.483 4.483 0 01-.923 1.785A5.969 5.969 0 006 21c1.282 0 2.47-.402 3.445-1.087.81.22 1.668.337 2.555.337z" />
          </svg>
          <svg v-else viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75L11.25 15 15 9.75M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>
        <div class="notification-content">
          <div class="notification-title">{{ item.title }}</div>
          <div class="notification-text">{{ item.content }}</div>
          <div class="notification-time">{{ formatTime(item.createTime) }}</div>
        </div>
        <div v-if="item.isRead === 0" class="unread-dot"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getNotifications, getUnreadCount, markAsRead, markAllAsRead } from '@/api/notification'

const router = useRouter()
const list = ref([])
const loading = ref(true)
const currentType = ref(0)
const unreadCount = ref(0)

const typeTabs = ref([
  { label: '全部', value: 0, count: 0 },
  { label: '关注', value: 1, count: 0 },
  { label: '评论', value: 2, count: 0 },
  { label: '回复', value: 3, count: 0 },
  { label: '公告', value: 4, count: 0 }
])

const loadList = async () => {
  loading.value = true
  try {
    const res = await getNotifications(currentType.value || undefined)
    list.value = res.data.list || []
  } catch (e) {
    console.error('获取通知列表失败', e)
  } finally {
    loading.value = false
  }
}

const loadUnreadCount = async () => {
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data || 0
  } catch (e) {
    console.error('获取未读数量失败', e)
  }
}

const changeType = (type) => {
  currentType.value = type
  loadList()
}

const handleClick = async (item) => {
  if (item.isRead === 0) {
    await markAsRead(item.id)
    item.isRead = 1
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  }

  // 跳转到相关页面
  if (item.type === 1 || item.type === 2) {
    router.push(`/article/${item.relatedId}`)
  } else if (item.type === 4) {
    router.push(`/announcement/${item.relatedId}`)
  }
}

const handleMarkAllRead = async () => {
  await markAllAsRead()
  list.value.forEach(item => item.isRead = 1)
  unreadCount.value = 0
}

const getTypeClass = (type) => {
  const classes = {
    1: 'type-follow',
    2: 'type-comment',
    3: 'type-reply',
    4: 'type-announcement'
  }
  return classes[type] || ''
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date

  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)} 天前`

  return `${date.getMonth() + 1}-${date.getDate()}`
}

onMounted(() => {
  loadList()
  loadUnreadCount()
})
</script>

<style lang="scss" scoped>
.notification-page {
  max-width: 800px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-6);
}

.page-title {
  font-size: var(--text-4xl);
  font-weight: var(--font-bold);
  color: var(--text-primary);
}

.mark-all-btn {
  padding: var(--space-2) var(--space-4);
  font-size: var(--text-sm);
  color: var(--color-primary);
  background: none;
  border: 1px solid var(--color-primary);
  border-radius: var(--radius-full);
  cursor: pointer;
  transition: all var(--transition-fast);

  &:hover {
    color: white;
    background: var(--color-primary);
  }
}

.type-tabs {
  display: flex;
  gap: var(--space-2);
  margin-bottom: var(--space-6);
  overflow-x: auto;
  padding-bottom: var(--space-2);
}

.type-tab {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  padding: var(--space-2) var(--space-4);
  font-size: var(--text-sm);
  color: var(--text-secondary);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-full);
  cursor: pointer;
  white-space: nowrap;
  transition: all var(--transition-fast);

  .count {
    padding: 0 var(--space-1);
    font-size: var(--text-xs);
    color: white;
    background: var(--color-primary);
    border-radius: var(--radius-full);
    min-width: 18px;
    text-align: center;
  }

  &:hover {
    color: var(--color-primary);
    border-color: var(--color-primary);
  }

  &.active {
    color: white;
    background: var(--color-primary);
    border-color: var(--color-primary);
  }
}

.loading-state, .empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: var(--space-16);
  color: var(--text-muted);

  svg {
    width: 48px;
    height: 48px;
    margin-bottom: var(--space-4);
    opacity: 0.5;
  }
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid var(--border-color);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.notification-list {
  background: var(--bg-card);
  border-radius: var(--radius-xl);
  overflow: hidden;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: var(--space-4);
  padding: var(--space-4);
  border-bottom: 1px solid var(--border-color);
  cursor: pointer;
  transition: background var(--transition-fast);

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: var(--bg-hover);
  }

  &.unread {
    background: rgba(180, 83, 9, 0.05);
  }
}

.notification-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: var(--radius-full);
  flex-shrink: 0;

  svg {
    width: 20px;
    height: 20px;
  }

  &.type-follow {
    background: rgba(34, 197, 94, 0.1);
    color: rgb(34, 197, 94);
  }

  &.type-comment, &.type-reply {
    background: rgba(59, 130, 246, 0.1);
    color: rgb(59, 130, 246);
  }

  &.type-announcement {
    background: rgba(180, 83, 9, 0.1);
    color: var(--color-primary);
  }
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-title {
  font-size: var(--text-base);
  font-weight: var(--font-medium);
  color: var(--text-primary);
  margin-bottom: var(--space-1);
}

.notification-text {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin-bottom: var(--space-1);
}

.notification-time {
  font-size: var(--text-xs);
  color: var(--text-muted);
}

.unread-dot {
  width: 8px;
  height: 8px;
  background: var(--color-primary);
  border-radius: var(--radius-full);
  flex-shrink: 0;
  margin-top: var(--space-2);
}

@media (max-width: 640px) {
  .page-title {
    font-size: var(--text-3xl);
  }

  .type-tabs {
    gap: var(--space-1);
  }

  .type-tab {
    padding: var(--space-1) var(--space-3);
    font-size: var(--text-xs);
  }
}
</style>
```

- [ ] **Step 2: 提交通知中心页面**

```bash
git add blog-web/src/views/portal/Notification.vue
git commit -m "feat: add notification center page"
```

---

## Task 16: 前端通知系统 - 顶部导航通知图标

**Files:**
- Modify: `blog-web/src/views/portal/Layout.vue`
- Modify: `blog-web/src/stores/user.js`

- [ ] **Step 1: 在 user store 添加未读通知数状态**

在 `user.js` store 中添加：

```javascript
import { getUnreadCount } from '@/api/notification'

// 在 state 中添加
const unreadNotificationCount = ref(0)

// 在 actions 中添加
const fetchUnreadCount = async () => {
  if (!isLoggedIn.value) return
  try {
    const res = await getUnreadCount()
    unreadNotificationCount.value = res.data || 0
  } catch (e) {
    console.error('获取未读通知数失败', e)
  }
}

// 导出
return {
  // ...existing exports
  unreadNotificationCount,
  fetchUnreadCount
}
```

- [ ] **Step 2: 在 Layout.vue 添加通知图标**

在 `header-actions` 区域添加通知图标（在 theme-toggle 按钮之前）：

```vue
<!-- 通知图标 -->
<router-link
  v-if="userStore.isLoggedIn"
  to="/notifications"
  class="icon-btn notification-btn"
  title="通知"
>
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
    <path stroke-linecap="round" stroke-linejoin="round" d="M14.857 17.082a23.848 23.848 0 005.454-1.31A8.967 8.967 0 0118 9.75v-.7V9A6 6 0 006 9v.75a8.967 8.967 0 01-2.312 6.022c1.733.64 3.56 1.085 5.455 1.31m5.714 0a24.255 24.255 0 01-5.714 0m5.714 0a3 3 0 11-5.714 0" />
  </svg>
  <span v-if="userStore.unreadNotificationCount > 0" class="notification-badge">
    {{ userStore.unreadNotificationCount > 99 ? '99+' : userStore.unreadNotificationCount }}
  </span>
</router-link>
```

添加对应的 CSS：

```scss
.notification-btn {
  position: relative;
}

.notification-badge {
  position: absolute;
  top: 2px;
  right: 2px;
  min-width: 16px;
  height: 16px;
  padding: 0 4px;
  font-size: 10px;
  font-weight: var(--font-bold);
  color: white;
  background: var(--color-error);
  border-radius: var(--radius-full);
  display: flex;
  align-items: center;
  justify-content: center;
}
```

在 script setup 中添加获取未读数的逻辑：

```javascript
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

onMounted(() => {
  if (userStore.isLoggedIn) {
    userStore.fetchUnreadCount()
  }
})
```

- [ ] **Step 3: 提交通知图标变更**

```bash
git add blog-web/src/views/portal/Layout.vue
git add blog-web/src/stores/user.js
git commit -m "feat: add notification icon with unread badge in header"
```

---

## Task 17: 前端路由配置

**Files:**
- Modify: `blog-web/src/router/index.js`

- [ ] **Step 1: 添加新路由**

在 `router/index.js` 中添加关注、粉丝和通知页面的路由：

在 `/user` 的 children 中添加：

```javascript
{
  path: 'following',
  name: 'UserFollowing',
  component: () => import('@/views/portal/UserFollowing.vue'),
  meta: { title: '关注' }
},
{
  path: 'followers',
  name: 'UserFollowers',
  component: () => import('@/views/portal/UserFollowers.vue'),
  meta: { title: '粉丝' }
}
```

在 `/` 的 children 中添加：

```javascript
{
  path: 'notifications',
  name: 'Notification',
  component: () => import('@/views/portal/Notification.vue'),
  meta: { title: '通知中心', requiresAuth: true }
}
```

- [ ] **Step 2: 提交路由变更**

```bash
git add blog-web/src/router/index.js
git commit -m "feat: add routes for following, followers and notifications"
```

---

## Task 18: 前端后台 - 公告管理页面

**Files:**
- Create: `blog-web/src/views/admin/AnnouncementManage.vue`

- [ ] **Step 1: 创建公告管理页面**

```vue
<template>
  <div class="announcement-manage">
    <header class="page-header">
      <h1 class="page-title">公告管理</h1>
      <button class="btn-primary" @click="showEditor = true">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4" />
        </svg>
        发布公告
      </button>
    </header>

    <div v-if="loading" class="loading-state">
      <span class="loading-spinner"></span>
    </div>

    <div v-else class="announcement-list">
      <div v-for="item in list" :key="item.id" class="announcement-item">
        <div class="announcement-info">
          <h3 class="announcement-title">{{ item.title }}</h3>
          <p class="announcement-content">{{ item.content }}</p>
          <div class="announcement-meta">
            <span :class="['status', item.status === 1 ? 'published' : 'draft']">
              {{ item.status === 1 ? '已发布' : '草稿' }}
            </span>
            <span class="time">{{ formatTime(item.createTime) }}</span>
          </div>
        </div>
        <div class="announcement-actions">
          <button v-if="item.status === 0" class="btn-publish" @click="handlePublish(item.id)">
            发布
          </button>
          <button class="btn-edit" @click="handleEdit(item)">编辑</button>
          <button class="btn-delete" @click="handleDelete(item.id)">删除</button>
        </div>
      </div>
    </div>

    <!-- 编辑弹窗 -->
    <div v-if="showEditor" class="modal-overlay" @click="showEditor = false">
      <div class="modal-content" @click.stop>
        <h2 class="modal-title">{{ editingId ? '编辑公告' : '发布公告' }}</h2>
        <div class="form-group">
          <label>标题</label>
          <input v-model="form.title" type="text" placeholder="请输入公告标题" />
        </div>
        <div class="form-group">
          <label>内容</label>
          <textarea v-model="form.content" placeholder="请输入公告内容" rows="6"></textarea>
        </div>
        <div class="modal-actions">
          <button class="btn-secondary" @click="showEditor = false">取消</button>
          <button class="btn-primary" @click="handleSave">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { getAnnouncements, createAnnouncement, updateAnnouncement, deleteAnnouncement, publishAnnouncement } from '@/api/announcement'

const list = ref([])
const loading = ref(true)
const showEditor = ref(false)
const editingId = ref(null)
const form = reactive({
  title: '',
  content: ''
})

const loadList = async () => {
  loading.value = true
  try {
    const res = await getAnnouncements()
    list.value = res.data || []
  } catch (e) {
    console.error('获取公告列表失败', e)
  } finally {
    loading.value = false
  }
}

const handleEdit = (item) => {
  editingId.value = item.id
  form.title = item.title
  form.content = item.content
  showEditor.value = true
}

const handleSave = async () => {
  if (!form.title || !form.content) {
    alert('请填写完整信息')
    return
  }

  try {
    if (editingId.value) {
      await updateAnnouncement(editingId.value, form)
    } else {
      await createAnnouncement(form)
    }
    showEditor.value = false
    editingId.value = null
    form.title = ''
    form.content = ''
    loadList()
  } catch (e) {
    console.error('保存公告失败', e)
  }
}

const handlePublish = async (id) => {
  if (!confirm('确定发布此公告？发布后将通知所有用户。')) return

  try {
    await publishAnnouncement(id)
    loadList()
  } catch (e) {
    console.error('发布公告失败', e)
  }
}

const handleDelete = async (id) => {
  if (!confirm('确定删除此公告？')) return

  try {
    await deleteAnnouncement(id)
    loadList()
  } catch (e) {
    console.error('删除公告失败', e)
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

onMounted(() => {
  loadList()
})
</script>

<style lang="scss" scoped>
.announcement-manage {
  padding: var(--space-6);
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: var(--space-6);
}

.page-title {
  font-size: var(--text-2xl);
  font-weight: var(--font-bold);
}

.btn-primary {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-2) var(--space-4);
  font-size: var(--text-sm);
  color: white;
  background: var(--color-primary);
  border: none;
  border-radius: var(--radius-lg);
  cursor: pointer;

  svg {
    width: 16px;
    height: 16px;
  }

  &:hover {
    background: var(--color-accent);
  }
}

.btn-secondary {
  padding: var(--space-2) var(--space-4);
  font-size: var(--text-sm);
  color: var(--text-secondary);
  background: var(--bg-secondary);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  cursor: pointer;

  &:hover {
    background: var(--bg-hover);
  }
}

.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: var(--space-12);
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid var(--border-color);
  border-top-color: var(--color-primary);
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.announcement-list {
  background: var(--bg-card);
  border-radius: var(--radius-xl);
  overflow: hidden;
}

.announcement-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-4);
  padding: var(--space-4);
  border-bottom: 1px solid var(--border-color);

  &:last-child {
    border-bottom: none;
  }
}

.announcement-info {
  flex: 1;
}

.announcement-title {
  font-size: var(--text-base);
  font-weight: var(--font-medium);
  margin-bottom: var(--space-1);
}

.announcement-content {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin-bottom: var(--space-2);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.announcement-meta {
  display: flex;
  align-items: center;
  gap: var(--space-4);
}

.status {
  font-size: var(--text-xs);
  padding: var(--space-1) var(--space-2);
  border-radius: var(--radius-full);

  &.published {
    color: rgb(34, 197, 94);
    background: rgba(34, 197, 94, 0.1);
  }

  &.draft {
    color: var(--text-muted);
    background: var(--bg-secondary);
  }
}

.time {
  font-size: var(--text-xs);
  color: var(--text-muted);
}

.announcement-actions {
  display: flex;
  gap: var(--space-2);
}

.btn-edit, .btn-delete, .btn-publish {
  padding: var(--space-1) var(--space-3);
  font-size: var(--text-xs);
  border-radius: var(--radius-md);
  cursor: pointer;
  border: none;
}

.btn-publish {
  color: white;
  background: rgb(34, 197, 94);

  &:hover {
    background: rgb(22, 163, 74);
  }
}

.btn-edit {
  color: var(--color-primary);
  background: rgba(180, 83, 9, 0.1);

  &:hover {
    background: rgba(180, 83, 9, 0.2);
  }
}

.btn-delete {
  color: var(--color-error);
  background: rgba(239, 68, 68, 0.1);

  &:hover {
    background: rgba(239, 68, 68, 0.2);
  }
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  width: 90%;
  max-width: 500px;
  background: var(--bg-card);
  border-radius: var(--radius-xl);
  padding: var(--space-6);
}

.modal-title {
  font-size: var(--text-lg);
  font-weight: var(--font-semibold);
  margin-bottom: var(--space-4);
}

.form-group {
  margin-bottom: var(--space-4);

  label {
    display: block;
    font-size: var(--text-sm);
    font-weight: var(--font-medium);
    margin-bottom: var(--space-2);
  }

  input, textarea {
    width: 100%;
    padding: var(--space-3);
    font-size: var(--text-sm);
    background: var(--bg-secondary);
    border: 1px solid var(--border-color);
    border-radius: var(--radius-lg);
    outline: none;

    &:focus {
      border-color: var(--color-primary);
    }
  }
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: var(--space-3);
  margin-top: var(--space-6);
}
</style>
```

- [ ] **Step 2: 添加公告管理路由**

在 `router/index.js` 的 `/admin` children 中添加：

```javascript
{
  path: 'announcements',
  name: 'AnnouncementManage',
  component: () => import('@/views/admin/AnnouncementManage.vue'),
  meta: { title: '公告管理' }
}
```

- [ ] **Step 3: 提交公告管理页面**

```bash
git add blog-web/src/views/admin/AnnouncementManage.vue
git add blog-web/src/router/index.js
git commit -m "feat: add announcement management page in admin panel"
```

---

## Task 19: 最终验证与提交

- [ ] **Step 1: 验证后端编译**

```bash
cd blog-server && mvn clean compile
```

- [ ] **Step 2: 验证前端编译**

```bash
cd blog-web && pnpm build
```

- [ ] **Step 3: 提交所有变更**

```bash
git add -A
git commit -m "feat: complete follow and notification system implementation"
```
