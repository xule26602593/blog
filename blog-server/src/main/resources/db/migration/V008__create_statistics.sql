-- 每日统计表
CREATE TABLE IF NOT EXISTS `daily_statistics` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `date` DATE NOT NULL COMMENT '日期',
    `pv` INT DEFAULT 0 COMMENT '页面访问量',
    `uv` INT DEFAULT 0 COMMENT '独立访客数',
    `ip_count` INT DEFAULT 0 COMMENT '独立IP数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_date` (`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci COMMENT='每日统计表';

-- 文章统计表（按天）
CREATE TABLE IF NOT EXISTS `article_daily_stats` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `article_id` BIGINT NOT NULL COMMENT '文章ID',
    `date` DATE NOT NULL COMMENT '日期',
    `view_count` INT DEFAULT 0 COMMENT '浏览量',
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    `comment_count` INT DEFAULT 0 COMMENT '评论数',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_date` (`article_id`, `date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci COMMENT='文章每日统计表';
