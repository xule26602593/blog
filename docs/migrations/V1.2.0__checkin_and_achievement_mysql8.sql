-- =============================================
-- 博客系统 V1.2.0 数据库迁移脚本 (MySQL 8.0 兼容版)
-- 功能: 签到系统 + 成就徽章系统
-- 版本: V1.2.0
-- 日期: 2026-04-30
-- =============================================

-- =============================================
-- 第一部分: 用户表扩展
-- =============================================

-- 为用户表添加积分、等级、签到、成就相关字段
ALTER TABLE `sys_user` 
ADD COLUMN `points` INT DEFAULT 0 COMMENT '当前积分',
ADD COLUMN `total_points` INT DEFAULT 0 COMMENT '累计积分',
ADD COLUMN `level` INT DEFAULT 1 COMMENT '用户等级',
ADD COLUMN `checkin_days` INT DEFAULT 0 COMMENT '累计签到天数',
ADD COLUMN `max_consecutive_days` INT DEFAULT 0 COMMENT '最大连续签到天数',
ADD COLUMN `last_checkin_date` DATE DEFAULT NULL COMMENT '最后签到日期',
ADD COLUMN `achievement_count` INT DEFAULT 0 COMMENT '已解锁成就数量',
ADD COLUMN `total_achievement_points` INT DEFAULT 0 COMMENT '成就奖励总积分';

-- =============================================
-- 第二部分: 签到系统表
-- =============================================

-- 签到记录表
CREATE TABLE IF NOT EXISTS `user_checkin` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `checkin_date` DATE NOT NULL COMMENT '签到日期',
    `consecutive_days` INT DEFAULT 1 COMMENT '连续签到天数',
    `points_earned` INT DEFAULT 0 COMMENT '获得积分',
    `checkin_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '签到时间',
    `ip_address` VARCHAR(50) DEFAULT NULL COMMENT '签到IP',
    `device_info` VARCHAR(255) DEFAULT NULL COMMENT '设备信息',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_date` (`user_id`, `checkin_date`),
    KEY `idx_user_consecutive` (`user_id`, `consecutive_days`),
    KEY `idx_checkin_date` (`checkin_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户签到记录表';

-- 签到奖励配置表
CREATE TABLE IF NOT EXISTS `checkin_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `consecutive_days` INT NOT NULL COMMENT '连续签到天数阈值',
    `reward_points` INT NOT NULL COMMENT '额外奖励积分',
    `reward_type` VARCHAR(50) DEFAULT 'points' COMMENT '奖励类型',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '奖励描述',
    `status` TINYINT DEFAULT 1 COMMENT '状态 0:禁用 1:启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_days` (`consecutive_days`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签到奖励配置表';

-- 初始化签到奖励配置
INSERT IGNORE INTO `checkin_config` (`consecutive_days`, `reward_points`, `description`) VALUES
(1, 0, '每日签到基础奖励'),
(7, 20, '连续签到7天额外奖励'),
(14, 50, '连续签到14天额外奖励'),
(30, 100, '连续签到30天额外奖励'),
(60, 200, '连续签到60天额外奖励'),
(100, 500, '连续签到100天额外奖励'),
(180, 1000, '连续签到180天额外奖励'),
(365, 3000, '连续签到365天额外奖励');

-- =============================================
-- 第三部分: 成就系统表
-- =============================================

-- 成就定义表
CREATE TABLE IF NOT EXISTS `achievement` (
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

-- 用户成就表
CREATE TABLE IF NOT EXISTS `user_achievement` (
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

-- =============================================
-- 第四部分: 初始化成就数据
-- =============================================

-- 内容类成就
INSERT IGNORE INTO `achievement` (`code`, `name`, `description`, `category`, `type`, `condition_value`, `points`, `level`, `sort`) VALUES
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
('view_100000', '名震一方', '文章累计浏览量达到100000', 'content', 'count', 100000, 300, 3, 22);

-- 社交类成就
INSERT IGNORE INTO `achievement` (`code`, `name`, `description`, `category`, `type`, `condition_value`, `points`, `level`, `sort`) VALUES
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
('favorite_50', '知识库', '收藏50篇文章', 'social', 'count', 50, 30, 2, 131);

-- 活动类成就
INSERT IGNORE INTO `achievement` (`code`, `name`, `description`, `category`, `type`, `condition_value`, `points`, `level`, `sort`) VALUES
-- 签到（连续）
('checkin_1', '打卡新人', '完成首次签到', 'activity', 'count', 1, 5, 1, 200),
('checkin_7', '坚持一周', '连续签到7天', 'activity', 'streak', 7, 20, 1, 201),
('checkin_30', '月度达人', '连续签到30天', 'activity', 'streak', 30, 100, 2, 202),
('checkin_100', '百日坚持', '连续签到100天', 'activity', 'streak', 100, 300, 3, 203),
('checkin_365', '年度守望', '连续签到365天', 'activity', 'streak', 365, 1000, 4, 204),
-- 签到（累计）
('checkin_total_30', '月度打卡', '累计签到30天', 'activity', 'count', 30, 30, 1, 210),
('checkin_total_100', '百日打卡', '累计签到100天', 'activity', 'count', 100, 100, 2, 211),
('checkin_total_365', '年度打卡', '累计签到365天', 'activity', 'count', 365, 365, 3, 212);

-- 特殊类成就
INSERT IGNORE INTO `achievement` (`code`, `name`, `description`, `category`, `type`, `condition_value`, `points`, `level`, `sort`) VALUES
('early_bird', '早起的鸟儿', '早上6-8点签到', 'special', 'special', 0, 15, 2, 300),
('night_owl', '夜猫子', '深夜23-1点活跃', 'special', 'special', 0, 15, 2, 301),
('explorer', '探索者', '首次使用盲盒功能', 'special', 'special', 0, 10, 1, 302),
('first_edit', '初次编辑', '首次编辑个人资料', 'special', 'special', 0, 5, 1, 303),
('profile_complete', '完美档案', '完善所有个人资料', 'special', 'special', 0, 20, 2, 304),
('anniversary_1', '一周年纪念', '注册满一周年', 'special', 'special', 0, 100, 3, 305);

-- =============================================
-- 迁移完成
-- =============================================
