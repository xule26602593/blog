CREATE TABLE IF NOT EXISTS `article_template` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL COMMENT '模板名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '模板描述',
    `content` LONGTEXT NOT NULL COMMENT '模板内容(Markdown)',
    `category_id` BIGINT DEFAULT NULL COMMENT '默认分类',
    `tags` VARCHAR(255) DEFAULT NULL COMMENT '默认标签(JSON数组)',
    `is_default` TINYINT DEFAULT 0 COMMENT '是否默认模板',
    `status` TINYINT DEFAULT 1 COMMENT '状态',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci COMMENT='文章模板表';

-- 初始化默认模板
INSERT INTO `article_template` (`name`, `description`, `content`, `is_default`, `status`) VALUES
('技术教程', '适合技术类文章', '## 简介\n\n## 环境准备\n\n## 实现步骤\n\n## 总结\n', 1, 1),
('问题解决', '适合问题排查类文章', '## 问题描述\n\n## 分析过程\n\n## 解决方案\n\n## 参考\n', 0, 1),
('学习笔记', '适合学习总结类文章', '## 主题\n\n## 要点\n\n## 代码示例\n\n## 总结\n', 0, 1);
