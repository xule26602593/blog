-- V002__create_sensitive_word.sql
-- 敏感词表，用于存储需要过滤的敏感词

USE blog_db;

CREATE TABLE IF NOT EXISTS `sensitive_word` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `word` VARCHAR(50) NOT NULL COMMENT '敏感词',
    `category` VARCHAR(50) DEFAULT NULL COMMENT '分类',
    `replace_word` VARCHAR(50) DEFAULT '*' COMMENT '替换字符',
    `status` TINYINT DEFAULT 1 COMMENT '状态 0:禁用 1:启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_word` (`word`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci COMMENT='敏感词表';

-- 初始化一些示例敏感词
INSERT INTO `sensitive_word` (`word`, `category`, `status`) VALUES
('敏感词1', '政治', 1),
('敏感词2', '政治', 1);
