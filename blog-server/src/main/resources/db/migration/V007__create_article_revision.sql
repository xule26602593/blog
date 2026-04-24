CREATE TABLE IF NOT EXISTS `article_revision` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `article_id` BIGINT NOT NULL COMMENT '文章ID',
    `version` INT NOT NULL COMMENT '版本号',
    `title` VARCHAR(200) NOT NULL COMMENT '标题快照',
    `content` LONGTEXT NOT NULL COMMENT '内容快照',
    `summary` VARCHAR(500) DEFAULT NULL COMMENT '摘要快照',
    `editor_id` BIGINT NOT NULL COMMENT '编辑者ID',
    `change_note` VARCHAR(200) DEFAULT NULL COMMENT '修改说明',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_article_version` (`article_id`, `version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci COMMENT='文章版本历史表';
