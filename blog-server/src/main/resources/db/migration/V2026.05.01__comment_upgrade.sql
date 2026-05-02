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
