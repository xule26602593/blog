-- 话题灵感库表
-- 用于记录写作灵感、话题追踪和AI分析结果

CREATE TABLE `topic` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title` VARCHAR(200) NOT NULL COMMENT '话题标题',
    `description` TEXT COMMENT '话题描述/灵感记录',
    `source` VARCHAR(100) COMMENT '来源(知乎/微博/掘金/灵感/读书/工作)',
    `source_url` VARCHAR(500) COMMENT '来源链接',

    -- AI分析结果
    `analysis` TEXT COMMENT 'AI分析结果(JSON)',
    `analysis_status` TINYINT DEFAULT 0 COMMENT '0:待分析 1:分析中 2:已完成 3:失败',

    -- 创作状态追踪
    `status` TINYINT DEFAULT 0 COMMENT '0:待写 1:写作中 2:已发布 3:放弃',
    `article_id` BIGINT COMMENT '关联文章ID',

    -- 优先级与时间
    `priority` TINYINT DEFAULT 2 COMMENT '优先级 1:高 2:中 3:低',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `deleted` TINYINT DEFAULT 0,
    PRIMARY KEY (`id`),
    KEY `idx_status` (`status`),
    KEY `idx_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='话题灵感表';
