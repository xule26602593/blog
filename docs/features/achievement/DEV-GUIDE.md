# 成就徽章系统开发文档

## 1. 概述

### 1.1 功能定位

成就徽章系统是博客平台的用户激励核心模块，通过游戏化机制增强用户粘性和参与度。

### 1.2 核心特性

- 多类型成就（内容、社交、活动、特殊）
- 多等级徽章（普通、稀有、史诗、传说）
- 成就进度追踪
- 积分奖励联动
- 成就解锁通知

### 1.3 技术栈

| 层级 | 技术选型 |
|------|----------|
| 后端 | Spring Boot 3.2 + MyBatis Plus |
| 前端 | Vue 3.5 + Vant 4 |
| 数据库 | MySQL 8.0 |
| 缓存 | Redis |

---

## 2. 数据库设计

### 2.1 表结构

#### 2.1.1 成就定义表 `achievement`

```sql
CREATE TABLE `achievement` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `code` VARCHAR(50) NOT NULL COMMENT '成就编码(唯一标识)',
    `name` VARCHAR(100) NOT NULL COMMENT '成就名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '成就描述',
    `icon` VARCHAR(255) DEFAULT NULL COMMENT '徽章图标URL',
    `category` VARCHAR(50) NOT NULL COMMENT '分类: content/social/activity/special',
    `type` VARCHAR(50) NOT NULL COMMENT '类型: count/streak/special',
    `condition_value` INT DEFAULT 0 COMMENT '达成条件值',
    `points` INT DEFAULT 0 COMMENT '奖励积分',
    `level` TINYINT DEFAULT 1 COMMENT '等级: 1-普通 2-稀有 3-史诗 4-传说',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态 0:禁用 1:启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_code` (`code`),
    KEY `idx_category` (`category`),
    KEY `idx_level` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成就定义表';
```

**字段说明：**

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| code | VARCHAR(50) | 是 | 成就唯一编码，如 `first_article` |
| name | VARCHAR(100) | 是 | 成就名称，如 "初出茅庐" |
| category | VARCHAR(50) | 是 | 分类：content(内容)/social(社交)/activity(活动)/special(特殊) |
| type | VARCHAR(50) | 是 | 类型：count(计数)/streak(连续)/special(特殊) |
| condition_value | INT | 否 | 达成条件值，如发布文章数=10 |
| points | INT | 否 | 解锁奖励积分 |
| level | TINYINT | 否 | 稀有度：1普通/2稀有/3史诗/4传说 |

#### 2.1.2 用户成就表 `user_achievement`

```sql
CREATE TABLE `user_achievement` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `achievement_id` BIGINT NOT NULL COMMENT '成就ID',
    `progress` INT DEFAULT 0 COMMENT '当前进度',
    `unlocked` TINYINT DEFAULT 0 COMMENT '是否解锁 0:进行中 1:已解锁',
    `unlock_time` DATETIME DEFAULT NULL COMMENT '解锁时间',
    `notified` TINYINT DEFAULT 0 COMMENT '是否已通知 0:未通知 1:已通知',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_achievement` (`user_id`, `achievement_id`),
    KEY `idx_user_unlocked` (`user_id`, `unlocked`),
    KEY `idx_achievement` (`achievement_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户成就表';
```

#### 2.1.3 用户表扩展

```sql
-- 为用户表添加成就相关字段
ALTER TABLE `sys_user` 
ADD COLUMN `achievement_count` INT DEFAULT 0 COMMENT '已解锁成就数量',
ADD COLUMN `total_achievement_points` INT DEFAULT 0 COMMENT '成就奖励总积分';
```

### 2.2 初始数据

```sql
-- ==================== 内容类成就 ====================
INSERT INTO `achievement` (`code`, `name`, `description`, `category`, `type`, `condition_value`, `points`, `level`, `sort`) VALUES
-- 文章发布
('first_article', '初出茅庐', '发布第一篇文章，开启创作之旅', 'content', 'count', 1, 10, 1, 1),
('article_10', '笔耕不辍', '累计发布10篇文章', 'content', 'count', 10, 50, 2, 2),
('article_50', '著作等身', '累计发布50篇文章', 'content', 'count', 50, 200, 3, 3),
('article_100', '文坛巨匠', '累计发布100篇文章', 'content', 'count', 100, 500, 4, 4),
('article_500', '传奇作者', '累计发布500篇文章', 'content', 'count', 500, 2000, 4, 5),

-- 点赞获得
('like_100', '初获认可', '累计获得100个赞', 'content', 'count', 100, 30, 1, 10),
('like_500', '人气作者', '累计获得500个赞', 'content', 'count', 500, 80, 2, 11),
('like_1000', '万众瞩目', '累计获得1000个赞', 'content', 'count', 1000, 150, 3, 12),
('like_5000', '顶流作者', '累计获得5000个赞', 'content', 'count', 5000, 500, 4, 13),

-- 文章浏览
('view_1000', '小有名气', '文章累计浏览量达到1000', 'content', 'count', 1000, 20, 1, 20),
('view_10000', '声名远扬', '文章累计浏览量达到10000', 'content', 'count', 10000, 100, 2, 21),
('view_100000', '名震一方', '文章累计浏览量达到100000', 'content', 'count', 100000, 300, 3, 22),

-- ==================== 社交类成就 ====================
INSERT INTO `achievement` (`code`, `name`, `description`, `category`, `type`, `condition_value`, `points`, `level`, `sort`) VALUES
-- 评论
('first_comment', '初次交流', '发表第一条评论', 'social', 'count', 1, 5, 1, 100),
('comment_10', '畅所欲言', '累计发表10条评论', 'social', 'count', 10, 15, 1, 101),
('comment_50', '活跃评论员', '累计发表50条评论', 'social', 'count', 50, 50, 2, 102),
('comment_100', '评论达人', '累计发表100条评论', 'social', 'count', 100, 100, 3, 103),

-- 粉丝
('follower_10', '小有名气', '获得10个粉丝关注', 'social', 'count', 10, 20, 1, 110),
('follower_50', '人气之星', '获得50个粉丝关注', 'social', 'count', 50, 50, 2, 111),
('follower_100', '百家争鸣', '获得100个粉丝关注', 'social', 'count', 100, 100, 3, 112),
('follower_500', '千军万马', '获得500个粉丝关注', 'social', 'count', 500, 300, 3, 113),
('follower_1000', '意见领袖', '获得1000个粉丝关注', 'social', 'count', 1000, 500, 4, 114),

-- 关注
('following_10', '博采众长', '关注10位作者', 'social', 'count', 10, 10, 1, 120),
('following_50', '学海无涯', '关注50位作者', 'social', 'count', 50, 30, 2, 121),

-- 收藏
('favorite_10', '珍藏家', '收藏10篇文章', 'social', 'count', 10, 10, 1, 130),
('favorite_50', '知识库', '收藏50篇文章', 'social', 'count', 50, 30, 2, 131),

-- ==================== 活动类成就 ====================
INSERT INTO `achievement` (`code`, `name`, `description`, `category`, `type`, `condition_value`, `points`, `level`, `sort`) VALUES
-- 签到
('checkin_1', '打卡新人', '完成首次签到', 'activity', 'count', 1, 5, 1, 200),
('checkin_7', '坚持一周', '连续签到7天', 'activity', 'streak', 7, 20, 1, 201),
('checkin_30', '月度达人', '连续签到30天', 'activity', 'streak', 30, 100, 2, 202),
('checkin_100', '百日坚持', '连续签到100天', 'activity', 'streak', 100, 300, 3, 203),
('checkin_365', '年度守望', '连续签到365天', 'activity', 'streak', 365, 1000, 4, 204),

('checkin_total_30', '月度打卡', '累计签到30天', 'activity', 'count', 30, 30, 1, 210),
('checkin_total_100', '百日打卡', '累计签到100天', 'activity', 'count', 100, 100, 2, 211),
('checkin_total_365', '年度打卡', '累计签到365天', 'activity', 'count', 365, 365, 3, 212),

-- ==================== 特殊类成就 ====================
INSERT INTO `achievement` (`code`, `name`, `description`, `category`, `type`, `condition_value`, `points`, `level`, `sort`) VALUES
('early_bird', '早起的鸟儿', '早上6-8点签到', 'special', 'special', 0, 15, 2, 300),
('night_owl', '夜猫子', '深夜23-1点活跃', 'special', 'special', 0, 15, 2, 301),
('explorer', '探索者', '首次使用盲盒功能', 'special', 'special', 0, 10, 1, 302),
('first_edit', '初次编辑', '首次编辑个人资料', 'special', 'special', 0, 5, 1, 303),
('profile_complete', '完美档案', '完善所有个人资料', 'special', 'special', 0, 20, 2, 304),
('anniversary_1', '一周年纪念', '注册满一周年', 'special', 'special', 0, 100, 3, 305);
```

---

## 3. 后端实现

### 3.1 目录结构

```
blog-server/src/main/java/com/blog/
├── domain/
│   ├── entity/
│   │   ├── Achievement.java
│   │   └── UserAchievement.java
│   ├── dto/
│   │   └── AchievementProgressDTO.java
│   └── vo/
│       ├── AchievementVO.java
│       └── UserAchievementVO.java
├── repository/
│   └── mapper/
│       ├── AchievementMapper.java
│       └── UserAchievementMapper.java
├── service/
│   ├── AchievementService.java
│   ├── AchievementTriggerService.java
│   └── impl/
│       ├── AchievementServiceImpl.java
│       └── AchievementTriggerServiceImpl.java
└── controller/
    └── portal/
        └── AchievementController.java
```

### 3.2 实体类

#### Achievement.java

```java
package com.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("achievement")
public class Achievement implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    private String name;

    private String description;

    private String icon;

    /**
     * 分类: content/social/activity/special
     */
    private String category;

    /**
     * 类型: count/streak/special
     */
    private String type;

    /**
     * 达成条件值
     */
    private Integer conditionValue;

    /**
     * 奖励积分
     */
    private Integer points;

    /**
     * 等级: 1-普通 2-稀有 3-史诗 4-传说
     */
    private Integer level;

    private Integer sort;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

#### UserAchievement.java

```java
package com.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("user_achievement")
public class UserAchievement implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long achievementId;

    /**
     * 当前进度
     */
    private Integer progress;

    /**
     * 是否解锁: 0-进行中 1-已解锁
     */
    private Integer unlocked;

    /**
     * 解锁时间
     */
    private LocalDateTime unlockTime;

    /**
     * 是否已通知
     */
    private Integer notified;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
```

### 3.3 服务层

#### AchievementService.java

```java
package com.blog.service;

import com.blog.domain.vo.AchievementVO;
import com.blog.domain.vo.UserAchievementVO;
import java.util.List;

public interface AchievementService {

    /**
     * 获取所有成就列表
     */
    List<AchievementVO> listAll();

    /**
     * 按分类获取成就列表
     */
    List<AchievementVO> listByCategory(String category);

    /**
     * 获取用户成就列表（含进度）
     */
    List<UserAchievementVO> getUserAchievements(Long userId);

    /**
     * 获取用户已解锁成就
     */
    List<UserAchievementVO> getUserUnlockedAchievements(Long userId);

    /**
     * 获取用户成就进度
     */
    UserAchievementVO getUserAchievementProgress(Long userId, Long achievementId);

    /**
     * 检查并解锁成就
     * @return 是否新解锁
     */
    boolean checkAndUnlock(Long userId, String achievementCode);

    /**
     * 批量检查某分类下所有成就
     */
    void checkCategory(Long userId, String category);
}
```

#### AchievementTriggerService.java

```java
package com.blog.service;

/**
 * 成就触发服务
 * 各业务模块调用此服务触发成就检查
 */
public interface AchievementTriggerService {

    /**
     * 触发文章相关成就检查
     * @param userId 用户ID
     */
    void triggerArticleAchievements(Long userId);

    /**
     * 触发评论相关成就检查
     * @param userId 用户ID
     */
    void triggerCommentAchievements(Long userId);

    /**
     * 触发关注相关成就检查
     * @param userId 用户ID
     */
    void triggerFollowAchievements(Long userId);

    /**
     * 触发签到相关成就检查
     * @param userId 用户ID
     * @param consecutiveDays 连续天数
     */
    void triggerCheckinAchievements(Long userId, int consecutiveDays);

    /**
     * 触发特殊成就
     * @param userId 用户ID
     * @param achievementCode 成就编码
     */
    void triggerSpecialAchievement(Long userId, String achievementCode);
}
```

#### AchievementServiceImpl.java

```java
package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.domain.entity.Achievement;
import com.blog.domain.entity.UserAchievement;
import com.blog.domain.vo.AchievementVO;
import com.blog.domain.vo.UserAchievementVO;
import com.blog.repository.mapper.AchievementMapper;
import com.blog.repository.mapper.UserAchievementMapper;
import com.blog.repository.mapper.UserMapper;
import com.blog.service.AchievementService;
import com.blog.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AchievementServiceImpl implements AchievementService {

    private final AchievementMapper achievementMapper;
    private final UserAchievementMapper userAchievementMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    @Override
    @Cacheable(value = "achievements", key = "'all'")
    public List<AchievementVO> listAll() {
        return achievementMapper.selectList(
            new LambdaQueryWrapper<Achievement>()
                .eq(Achievement::getStatus, 1)
                .orderByAsc(Achievement::getCategory)
                .orderByAsc(Achievement::getSort)
        ).stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public List<UserAchievementVO> getUserAchievements(Long userId) {
        // 获取所有成就
        List<Achievement> achievements = achievementMapper.selectList(
            new LambdaQueryWrapper<Achievement>()
                .eq(Achievement::getStatus, 1)
                .orderByAsc(Achievement::getSort)
        );

        // 获取用户成就记录
        List<UserAchievement> userAchievements = userAchievementMapper.selectList(
            new LambdaQueryWrapper<UserAchievement>()
                .eq(UserAchievement::getUserId, userId)
        );

        // 组装结果
        return achievements.stream()
            .map(achievement -> {
                UserAchievement ua = userAchievements.stream()
                    .filter(u -> u.getAchievementId().equals(achievement.getId()))
                    .findFirst()
                    .orElse(null);

                return convertToUserVO(achievement, ua);
            })
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "userAchievements", key = "#userId")
    public boolean checkAndUnlock(Long userId, String achievementCode) {
        // 1. 查询成就定义
        Achievement achievement = achievementMapper.selectOne(
            new LambdaQueryWrapper<Achievement>()
                .eq(Achievement::getCode, achievementCode)
                .eq(Achievement::getStatus, 1)
        );

        if (achievement == null) {
            log.warn("成就不存在: {}", achievementCode);
            return false;
        }

        // 2. 查询用户成就记录
        UserAchievement ua = userAchievementMapper.selectOne(
            new LambdaQueryWrapper<UserAchievement>()
                .eq(UserAchievement::getUserId, userId)
                .eq(UserAchievement::getAchievementId, achievement.getId())
        );

        // 3. 已解锁则跳过
        if (ua != null && ua.getUnlocked() == 1) {
            return false;
        }

        // 4. 获取当前进度
        int currentProgress = calculateProgress(userId, achievement);

        // 5. 创建或更新记录
        if (ua == null) {
            ua = new UserAchievement();
            ua.setUserId(userId);
            ua.setAchievementId(achievement.getId());
            ua.setProgress(currentProgress);
        } else {
            ua.setProgress(currentProgress);
        }

        // 6. 检查是否达成
        boolean unlocked = false;
        if ("special".equals(achievement.getType()) || 
            currentProgress >= achievement.getConditionValue()) {
            ua.setUnlocked(1);
            ua.setUnlockTime(LocalDateTime.now());
            unlocked = true;

            // 发放积分奖励
            if (achievement.getPoints() > 0) {
                userMapper.addPoints(userId, achievement.getPoints());
            }

            // 更新成就计数
            userMapper.incrementAchievementCount(userId, achievement.getPoints());

            // 发送通知
            sendUnlockNotification(userId, achievement);
        }

        // 7. 保存记录
        if (ua.getId() == null) {
            userAchievementMapper.insert(ua);
        } else {
            userAchievementMapper.updateById(ua);
        }

        return unlocked;
    }

    /**
     * 计算成就进度
     */
    private int calculateProgress(Long userId, Achievement achievement) {
        String code = achievement.getCode();
        
        // 文章相关
        if (code.startsWith("article_")) {
            return userMapper.countArticles(userId);
        }
        if (code.startsWith("like_")) {
            return userMapper.countTotalLikes(userId);
        }
        if (code.startsWith("view_")) {
            return userMapper.countTotalViews(userId);
        }
        
        // 社交相关
        if (code.startsWith("comment_")) {
            return userMapper.countComments(userId);
        }
        if (code.startsWith("follower_")) {
            return userMapper.getFollowerCount(userId);
        }
        if (code.startsWith("following_")) {
            return userMapper.getFollowingCount(userId);
        }
        if (code.startsWith("favorite_")) {
            return userMapper.countFavorites(userId);
        }
        
        // 签到相关
        if (code.startsWith("checkin_")) {
            if ("streak".equals(achievement.getType())) {
                return userMapper.getConsecutiveCheckinDays(userId);
            }
            return userMapper.getTotalCheckinDays(userId);
        }
        
        // 特殊成就直接返回条件值
        if ("special".equals(achievement.getType())) {
            return achievement.getConditionValue();
        }
        
        return 0;
    }

    /**
     * 发送解锁通知
     */
    private void sendUnlockNotification(Long userId, Achievement achievement) {
        notificationService.sendNotification(
            userId,
            4, // ACHIEVEMENT 类型
            "恭喜解锁成就！",
            String.format("您已解锁成就【%s】，获得%d积分奖励", 
                achievement.getName(), achievement.getPoints())
        );
    }

    /**
     * 转换为VO
     */
    private AchievementVO convertToVO(Achievement achievement) {
        AchievementVO vo = new AchievementVO();
        vo.setId(achievement.getId());
        vo.setCode(achievement.getCode());
        vo.setName(achievement.getName());
        vo.setDescription(achievement.getDescription());
        vo.setIcon(achievement.getIcon());
        vo.setCategory(achievement.getCategory());
        vo.setType(achievement.getType());
        vo.setConditionValue(achievement.getConditionValue());
        vo.setPoints(achievement.getPoints());
        vo.setLevel(achievement.getLevel());
        return vo;
    }

    /**
     * 转换为用户成就VO
     */
    private UserAchievementVO convertToUserVO(Achievement achievement, UserAchievement ua) {
        UserAchievementVO vo = new UserAchievementVO();
        vo.setId(achievement.getId());
        vo.setCode(achievement.getCode());
        vo.setName(achievement.getName());
        vo.setDescription(achievement.getDescription());
        vo.setIcon(achievement.getIcon());
        vo.setCategory(achievement.getCategory());
        vo.setType(achievement.getType());
        vo.setConditionValue(achievement.getConditionValue());
        vo.setPoints(achievement.getPoints());
        vo.setLevel(achievement.getLevel());
        
        if (ua != null) {
            vo.setProgress(ua.getProgress());
            vo.setUnlocked(ua.getUnlocked() == 1);
            vo.setUnlockTime(ua.getUnlockTime());
        } else {
            vo.setProgress(0);
            vo.setUnlocked(false);
        }
        
        // 计算进度百分比
        if ("special".equals(achievement.getType())) {
            vo.setProgressPercent(vo.isUnlocked() ? 100 : 0);
        } else {
            vo.setProgressPercent(Math.min(100, 
                (int)(vo.getProgress() * 100.0 / achievement.getConditionValue())));
        }
        
        return vo;
    }
}
```

#### AchievementTriggerServiceImpl.java

```java
package com.blog.service.impl;

import com.blog.service.AchievementService;
import com.blog.service.AchievementTriggerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AchievementTriggerServiceImpl implements AchievementTriggerService {

    private final AchievementService achievementService;

    // 文章相关成就编码
    private static final List<String> ARTICLE_ACHIEVEMENTS = Arrays.asList(
        "first_article", "article_10", "article_50", "article_100", "article_500",
        "like_100", "like_500", "like_1000", "like_5000",
        "view_1000", "view_10000", "view_100000"
    );

    // 评论相关成就编码
    private static final List<String> COMMENT_ACHIEVEMENTS = Arrays.asList(
        "first_comment", "comment_10", "comment_50", "comment_100"
    );

    // 关注相关成就编码
    private static final List<String> FOLLOW_ACHIEVEMENTS = Arrays.asList(
        "follower_10", "follower_50", "follower_100", "follower_500", "follower_1000",
        "following_10", "following_50"
    );

    // 签到相关成就编码（连续）
    private static final List<String> CHECKIN_STREAK_ACHIEVEMENTS = Arrays.asList(
        "checkin_7", "checkin_30", "checkin_100", "checkin_365"
    );

    // 签到相关成就编码（累计）
    private static final List<String> CHECKIN_TOTAL_ACHIEVEMENTS = Arrays.asList(
        "checkin_1", "checkin_total_30", "checkin_total_100", "checkin_total_365"
    );

    @Override
    @Async
    public void triggerArticleAchievements(Long userId) {
        log.debug("触发文章成就检查: userId={}", userId);
        ARTICLE_ACHIEVEMENTS.forEach(code -> {
            try {
                achievementService.checkAndUnlock(userId, code);
            } catch (Exception e) {
                log.error("成就检查失败: code={}, userId={}", code, userId, e);
            }
        });
    }

    @Override
    @Async
    public void triggerCommentAchievements(Long userId) {
        log.debug("触发评论成就检查: userId={}", userId);
        COMMENT_ACHIEVEMENTS.forEach(code -> {
            try {
                achievementService.checkAndUnlock(userId, code);
            } catch (Exception e) {
                log.error("成就检查失败: code={}, userId={}", code, userId, e);
            }
        });
    }

    @Override
    @Async
    public void triggerFollowAchievements(Long userId) {
        log.debug("触发关注成就检查: userId={}", userId);
        FOLLOW_ACHIEVEMENTS.forEach(code -> {
            try {
                achievementService.checkAndUnlock(userId, code);
            } catch (Exception e) {
                log.error("成就检查失败: code={}, userId={}", code, userId, e);
            }
        });
    }

    @Override
    @Async
    public void triggerCheckinAchievements(Long userId, int consecutiveDays) {
        log.debug("触发签到成就检查: userId={}, consecutiveDays={}", userId, consecutiveDays);
        
        // 检查连续签到成就
        CHECKIN_STREAK_ACHIEVEMENTS.forEach(code -> {
            try {
                achievementService.checkAndUnlock(userId, code);
            } catch (Exception e) {
                log.error("成就检查失败: code={}, userId={}", code, userId, e);
            }
        });
        
        // 检查累计签到成就
        CHECKIN_TOTAL_ACHIEVEMENTS.forEach(code -> {
            try {
                achievementService.checkAndUnlock(userId, code);
            } catch (Exception e) {
                log.error("成就检查失败: code={}, userId={}", code, userId, e);
            }
        });
    }

    @Override
    public void triggerSpecialAchievement(Long userId, String achievementCode) {
        log.debug("触发特殊成就: userId={}, code={}", userId, achievementCode);
        achievementService.checkAndUnlock(userId, achievementCode);
    }
}
```

### 3.4 控制器

#### AchievementController.java

```java
package com.blog.controller.portal;

import com.blog.common.result.Result;
import com.blog.domain.vo.AchievementVO;
import com.blog.domain.vo.UserAchievementVO;
import com.blog.security.UserDetailsImpl;
import com.blog.service.AchievementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "成就接口", description = "成就徽章相关接口")
@RestController
@RequestMapping("/api/portal/achievements")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;

    @Operation(summary = "获取所有成就列表")
    @GetMapping
    public Result<List<AchievementVO>> listAll() {
        return Result.success(achievementService.listAll());
    }

    @Operation(summary = "按分类获取成就列表")
    @GetMapping("/category/{category}")
    public Result<List<AchievementVO>> listByCategory(
            @PathVariable String category) {
        return Result.success(achievementService.listByCategory(category));
    }

    @Operation(summary = "获取用户成就列表")
    @GetMapping("/my")
    public Result<List<UserAchievementVO>> getUserAchievements(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return Result.success(
            achievementService.getUserAchievements(userDetails.getUser().getId())
        );
    }

    @Operation(summary = "获取用户已解锁成就")
    @GetMapping("/my/unlocked")
    public Result<List<UserAchievementVO>> getUserUnlockedAchievements(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return Result.success(
            achievementService.getUserUnlockedAchievements(userDetails.getUser().getId())
        );
    }

    @Operation(summary = "获取单个成就进度")
    @GetMapping("/{achievementId}/progress")
    public Result<UserAchievementVO> getProgress(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @PathVariable Long achievementId) {
        return Result.success(
            achievementService.getUserAchievementProgress(
                userDetails.getUser().getId(), achievementId
            )
        );
    }
}
```

### 3.5 Mapper 扩展

```java
// UserMapper.java 添加以下方法

/**
 * 统计用户文章数
 */
@Select("SELECT COUNT(*) FROM article WHERE author_id = #{userId} AND deleted = 0 AND status = 1")
int countArticles(@Param("userId") Long userId);

/**
 * 统计用户获得的点赞总数
 */
@Select("SELECT COALESCE(SUM(like_count), 0) FROM article WHERE author_id = #{userId} AND deleted = 0")
int countTotalLikes(@Param("userId") Long userId);

/**
 * 统计用户文章总浏览量
 */
@Select("SELECT COALESCE(SUM(view_count), 0) FROM article WHERE author_id = #{userId} AND deleted = 0")
int countTotalViews(@Param("userId") Long userId);

/**
 * 统计用户评论数
 */
@Select("SELECT COUNT(*) FROM comment WHERE user_id = #{userId}")
int countComments(@Param("userId") Long userId);

/**
 * 统计用户收藏数
 */
@Select("SELECT COUNT(*) FROM user_action WHERE user_id = #{userId} AND action_type = 2")
int countFavorites(@Param("userId") Long userId);

/**
 * 获取连续签到天数
 */
@Select("SELECT consecutive_days FROM user_checkin WHERE user_id = #{userId} ORDER BY checkin_date DESC LIMIT 1")
int getConsecutiveCheckinDays(@Param("userId") Long userId);

/**
 * 获取累计签到天数
 */
@Select("SELECT checkin_days FROM sys_user WHERE id = #{userId}")
int getTotalCheckinDays(@Param("userId") Long userId);

/**
 * 增加用户积分
 */
@Update("UPDATE sys_user SET points = points + #{points}, total_points = total_points + #{points} WHERE id = #{userId}")
void addPoints(@Param("userId") Long userId, @Param("points") int points);

/**
 * 增加成就计数
 */
@Update("UPDATE sys_user SET achievement_count = achievement_count + 1, total_achievement_points = total_achievement_points + #{points} WHERE id = #{userId}")
void incrementAchievementCount(@Param("userId") Long userId, @Param("points") int points);
```

---

## 4. 前端实现

### 4.1 目录结构

```
blog-web/src/
├── api/
│   └── achievement.js
├── components/
│   └── achievement/
│       ├── AchievementCard.vue
│       ├── AchievementGrid.vue
│       └── AchievementUnlockDialog.vue
├── stores/
│   └── achievement.js
└── views/
    └── portal/
        └── UserCenter.vue (修改)
```

### 4.2 API 接口

#### api/achievement.js

```javascript
import request from '@/utils/request'

/**
 * 获取所有成就列表
 */
export function getAchievements() {
  return request.get('/api/portal/achievements')
}

/**
 * 按分类获取成就
 */
export function getAchievementsByCategory(category) {
  return request.get(`/api/portal/achievements/category/${category}`)
}

/**
 * 获取用户成就列表
 */
export function getUserAchievements() {
  return request.get('/api/portal/achievements/my')
}

/**
 * 获取已解锁成就
 */
export function getUnlockedAchievements() {
  return request.get('/api/portal/achievements/my/unlocked')
}
```

### 4.3 状态管理

#### stores/achievement.js

```javascript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getUserAchievements } from '@/api/achievement'

export const useAchievementStore = defineStore('achievement', () => {
  const achievements = ref([])
  const loading = ref(false)

  // 按分类分组
  const groupedAchievements = computed(() => {
    const groups = {
      content: { label: '内容成就', items: [] },
      social: { label: '社交成就', items: [] },
      activity: { label: '活动成就', items: [] },
      special: { label: '特殊成就', items: [] }
    }
    
    achievements.value.forEach(a => {
      if (groups[a.category]) {
        groups[a.category].items.push(a)
      }
    })
    
    return groups
  })

  // 统计
  const stats = computed(() => {
    const total = achievements.value.length
    const unlocked = achievements.value.filter(a => a.unlocked).length
    const totalPoints = achievements.value
      .filter(a => a.unlocked)
      .reduce((sum, a) => sum + a.points, 0)
    
    return { total, unlocked, totalPoints }
  })

  async function fetchAchievements() {
    if (loading.value) return
    loading.value = true
    try {
      const { data } = await getUserAchievements()
      achievements.value = data
    } catch (error) {
      console.error('获取成就列表失败:', error)
    } finally {
      loading.value = false
    }
  }

  return {
    achievements,
    loading,
    groupedAchievements,
    stats,
    fetchAchievements
  }
})
```

### 4.4 组件实现

#### components/achievement/AchievementCard.vue

```vue
<template>
  <div 
    :class="['achievement-card', `level-${achievement.level}`, { unlocked: achievement.unlocked }]"
    @click="handleClick"
  >
    <!-- 徽章图标 -->
    <div class="achievement-icon">
      <img 
        v-if="achievement.unlocked && achievement.icon" 
        :src="achievement.icon" 
        :alt="achievement.name"
      />
      <div v-else class="icon-locked">
        <van-icon name="lock" />
      </div>
      
      <!-- 等级标记 -->
      <span v-if="achievement.level > 1" class="level-badge">
        {{ levelLabels[achievement.level] }}
      </span>
    </div>
    
    <!-- 成就信息 -->
    <div class="achievement-info">
      <h4 class="achievement-name">{{ achievement.name }}</h4>
      <p class="achievement-desc">{{ achievement.description }}</p>
      
      <!-- 进度条 -->
      <div v-if="!achievement.unlocked && achievement.type !== 'special'" class="progress-bar">
        <van-progress 
          :percentage="achievement.progressPercent" 
          stroke-width="4"
          :show-pivot="false"
        />
        <span class="progress-text">
          {{ achievement.progress }}/{{ achievement.conditionValue }}
        </span>
      </div>
      
      <!-- 解锁时间 -->
      <div v-if="achievement.unlocked" class="unlock-time">
        <van-icon name="passed" />
        {{ formatDate(achievement.unlockTime) }} 解锁
      </div>
    </div>
    
    <!-- 积分奖励 -->
    <div class="achievement-points">
      <span class="points-value">+{{ achievement.points }}</span>
      <span class="points-label">积分</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import dayjs from 'dayjs'

const props = defineProps({
  achievement: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['click'])

const levelLabels = {
  1: '',
  2: '稀有',
  3: '史诗',
  4: '传说'
}

const formatDate = (date) => {
  return date ? dayjs(date).format('YYYY-MM-DD') : ''
}

const handleClick = () => {
  emit('click', props.achievement)
}
</script>

<style lang="scss" scoped>
.achievement-card {
  display: flex;
  align-items: center;
  gap: var(--space-4);
  padding: var(--space-4);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  cursor: pointer;
  transition: all var(--transition-base);

  &:hover {
    border-color: var(--text-muted);
    box-shadow: var(--shadow-md);
  }

  &.unlocked {
    border-color: var(--color-primary);
    
    .achievement-icon {
      background: linear-gradient(135deg, var(--color-primary), #5856D6);
    }
  }

  // 等级样式
  &.level-2.unlocked {
    border-color: #34C759;
    .achievement-icon { background: linear-gradient(135deg, #34C759, #30D158); }
  }
  
  &.level-3.unlocked {
    border-color: #AF52DE;
    .achievement-icon { background: linear-gradient(135deg, #AF52DE, #BF5AF2); }
    box-shadow: 0 0 20px rgba(175, 82, 222, 0.2);
  }
  
  &.level-4.unlocked {
    border-color: #FF9500;
    .achievement-icon { background: linear-gradient(135deg, #FF9500, #FF6B6B); }
    box-shadow: 0 0 30px rgba(255, 149, 0, 0.3);
    background: linear-gradient(135deg, rgba(255,149,0,0.05) 0%, transparent 100%);
  }
}

.achievement-icon {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-lg);
  background: var(--bg-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  position: relative;

  img {
    width: 36px;
    height: 36px;
  }

  .icon-locked {
    color: var(--text-muted);
    font-size: 24px;
  }
}

.level-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  padding: 2px 6px;
  font-size: 10px;
  background: var(--color-primary);
  color: white;
  border-radius: var(--radius-full);
}

.achievement-info {
  flex: 1;
  min-width: 0;
}

.achievement-name {
  font-size: var(--text-base);
  font-weight: var(--font-semibold);
  margin-bottom: var(--space-1);
}

.achievement-desc {
  font-size: var(--text-sm);
  color: var(--text-secondary);
  margin-bottom: var(--space-2);
}

.progress-bar {
  display: flex;
  align-items: center;
  gap: var(--space-2);

  :deep(.van-progress) {
    flex: 1;
  }
}

.progress-text {
  font-size: var(--text-xs);
  color: var(--text-muted);
  white-space: nowrap;
}

.unlock-time {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  font-size: var(--text-xs);
  color: var(--color-success);
}

.achievement-points {
  text-align: center;
  padding: var(--space-2) var(--space-3);
  background: rgba(255, 149, 0, 0.1);
  border-radius: var(--radius-md);
  color: #FF9500;
  flex-shrink: 0;

  .points-value {
    display: block;
    font-size: var(--text-lg);
    font-weight: var(--font-bold);
  }

  .points-label {
    font-size: var(--text-xs);
  }
}

// 响应式
@media (max-width: 480px) {
  .achievement-card {
    padding: var(--space-3);
  }
  
  .achievement-icon {
    width: 48px;
    height: 48px;
  }
  
  .achievement-points {
    padding: var(--space-1) var(--space-2);
    
    .points-value {
      font-size: var(--text-base);
    }
  }
}
</style>
```

#### components/achievement/AchievementGrid.vue

```vue
<template>
  <div class="achievement-grid">
    <!-- 分类标签 -->
    <van-tabs v-model:active="activeCategory" sticky>
      <van-tab 
        v-for="(group, key) in groupedAchievements" 
        :key="key"
        :name="key"
        :title="group.label"
      >
        <div class="category-stats">
          <span>已解锁 {{ getCategoryStats(key).unlocked }}/{{ getCategoryStats(key).total }}</span>
        </div>
        
        <div class="achievement-list">
          <AchievementCard
            v-for="achievement in group.items"
            :key="achievement.id"
            :achievement="achievement"
            @click="handleAchievementClick"
          />
        </div>
      </van-tab>
    </van-tabs>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useAchievementStore } from '@/stores/achievement'
import AchievementCard from './AchievementCard.vue'

const achievementStore = useAchievementStore()
const { groupedAchievements } = achievementStore

const activeCategory = ref('content')

const getCategoryStats = (category) => {
  const items = achievementStore.groupedAchievements[category]?.items || []
  return {
    total: items.length,
    unlocked: items.filter(a => a.unlocked).length
  }
}

const handleAchievementClick = (achievement) => {
  // 可以显示详情弹窗
  console.log('点击成就:', achievement)
}
</script>

<style lang="scss" scoped>
.achievement-grid {
  :deep(.van-tabs__wrap) {
    background: var(--bg-card);
  }
}

.category-stats {
  padding: var(--space-3) var(--space-4);
  font-size: var(--text-sm);
  color: var(--text-secondary);
}

.achievement-list {
  padding: 0 var(--space-4) var(--space-4);
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}
</style>
```

---

## 5. 业务集成

### 5.1 在各业务模块中触发成就检查

```java
// ArticleServiceImpl.java
@Override
public void publishArticle(Article article) {
    // ... 发布文章逻辑
    
    // 触发文章成就检查
    achievementTriggerService.triggerArticleAchievements(userId);
}

// CommentServiceImpl.java
@Override
public void addComment(Comment comment) {
    // ... 评论逻辑
    
    // 触发评论成就检查
    achievementTriggerService.triggerCommentAchievements(userId);
}

// FollowServiceImpl.java
@Override
public void follow(Long userId, Long targetId) {
    // ... 关注逻辑
    
    // 触发关注成就检查（双方）
    achievementTriggerService.triggerFollowAchievements(userId);
    achievementTriggerService.triggerFollowAchievements(targetId);
}

// CheckinServiceImpl.java
@Override
public CheckinResultDTO checkin(Long userId) {
    // ... 签到逻辑
    
    // 触发签到成就检查
    achievementTriggerService.triggerCheckinAchievements(userId, consecutiveDays);
}
```

---

## 6. 测试要点

### 6.1 单元测试

```java
@SpringBootTest
class AchievementServiceTest {

    @Autowired
    private AchievementService achievementService;

    @Test
    void testCheckAndUnlock_FirstArticle() {
        // 发布第一篇文章后
        boolean unlocked = achievementService.checkAndUnlock(1L, "first_article");
        assertTrue(unlocked);
    }

    @Test
    void testCheckAndUnlock_AlreadyUnlocked() {
        // 已解锁的成就再次检查
        achievementService.checkAndUnlock(1L, "first_article");
        boolean unlocked = achievementService.checkAndUnlock(1L, "first_article");
        assertFalse(unlocked);
    }
}
```

---

## 7. 扩展计划

### 7.1 短期扩展

- [ ] 成就分享功能
- [ ] 成就展示墙（个人主页）
- [ ] 成就排行榜

### 7.2 长期扩展

- [ ] 自定义成就（管理员配置）
- [ ] 成就组合奖励
- [ ] 成就任务系统
