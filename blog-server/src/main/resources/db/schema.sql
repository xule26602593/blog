-- =============================================
-- 博客系统数据库初始化脚本
-- 数据库: blog_db
-- 字符集: utf8mb4
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS blog_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE blog_db;

-- =============================================
-- 1. 角色表
-- =============================================
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
    `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '角色描述',
    `status` TINYINT DEFAULT 1 COMMENT '状态 0:禁用 1:启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- =============================================
-- 2. 用户表
-- =============================================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像',
    `status` TINYINT DEFAULT 1 COMMENT '状态 0:禁用 1:启用',
    `role_id` BIGINT DEFAULT NULL COMMENT '角色ID',
    `role_code` VARCHAR(50) DEFAULT 'visitor' COMMENT '角色编码',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    KEY `idx_email` (`email`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- =============================================
-- 3. 分类表
-- =============================================
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '分类描述',
    `sort` INT DEFAULT 0 COMMENT '排序',
    `status` TINYINT DEFAULT 1 COMMENT '状态 0:禁用 1:启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

-- =============================================
-- 4. 标签表
-- =============================================
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(50) NOT NULL COMMENT '标签名称',
    `color` VARCHAR(20) DEFAULT '#409EFF' COMMENT '标签颜色',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';

-- =============================================
-- 5. 文章表
-- =============================================
DROP TABLE IF EXISTS `article`;
CREATE TABLE `article` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title` VARCHAR(200) NOT NULL COMMENT '文章标题',
    `summary` VARCHAR(500) DEFAULT NULL COMMENT '文章摘要',
    `content` LONGTEXT COMMENT '文章内容',
    `cover_image` VARCHAR(255) DEFAULT NULL COMMENT '封面图片',
    `category_id` BIGINT DEFAULT NULL COMMENT '分类ID',
    `author_id` BIGINT DEFAULT NULL COMMENT '作者ID',
    `view_count` BIGINT DEFAULT 0 COMMENT '浏览量',
    `like_count` BIGINT DEFAULT 0 COMMENT '点赞数',
    `comment_count` INT DEFAULT 0 COMMENT '评论数',
    `is_top` TINYINT DEFAULT 0 COMMENT '是否置顶 0:否 1:是',
    `status` TINYINT DEFAULT 0 COMMENT '状态 0:草稿 1:已发布 2:回收站',
    `publish_time` DATETIME DEFAULT NULL COMMENT '发布时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_author_id` (`author_id`),
    KEY `idx_status_publish` (`status`, `publish_time`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';

-- =============================================
-- 6. 文章标签关联表
-- =============================================
DROP TABLE IF EXISTS `article_tag`;
CREATE TABLE `article_tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `article_id` BIGINT NOT NULL COMMENT '文章ID',
    `tag_id` BIGINT NOT NULL COMMENT '标签ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_tag` (`article_id`, `tag_id`),
    KEY `idx_article_id` (`article_id`),
    KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章标签关联表';

-- =============================================
-- 7. 评论表
-- =============================================
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `article_id` BIGINT NOT NULL COMMENT '文章ID',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父评论ID 0表示顶级评论',
    `reply_id` BIGINT DEFAULT NULL COMMENT '回复的评论ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '用户ID(登录用户)',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称(游客)',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱(游客)',
    `content` VARCHAR(1000) NOT NULL COMMENT '评论内容',
    `status` TINYINT DEFAULT 0 COMMENT '状态 0:待审核 1:已通过 2:已拒绝',
    `ip_address` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_article_id` (`article_id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- =============================================
-- 8. 留言表
-- =============================================
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '用户ID(登录用户)',
    `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称(游客)',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱(游客)',
    `content` VARCHAR(1000) NOT NULL COMMENT '留言内容',
    `status` TINYINT DEFAULT 0 COMMENT '状态 0:待审核 1:已通过 2:已拒绝',
    `ip_address` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='留言表';

-- =============================================
-- 9. 用户操作表(点赞/收藏)
-- =============================================
DROP TABLE IF EXISTS `user_action`;
CREATE TABLE `user_action` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `article_id` BIGINT NOT NULL COMMENT '文章ID',
    `action_type` TINYINT NOT NULL COMMENT '操作类型 1:点赞 2:收藏',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_article_action` (`user_id`, `article_id`, `action_type`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_article_id` (`article_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户操作表';

-- =============================================
-- 10. 访问日志表
-- =============================================
DROP TABLE IF EXISTS `visit_log`;
CREATE TABLE `visit_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `ip` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
    `url` VARCHAR(255) DEFAULT NULL COMMENT '请求URL',
    `method` VARCHAR(10) DEFAULT NULL COMMENT '请求方法',
    `params` TEXT DEFAULT NULL COMMENT '请求参数',
    `browser` VARCHAR(100) DEFAULT NULL COMMENT '浏览器',
    `os` VARCHAR(100) DEFAULT NULL COMMENT '操作系统',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访问日志表';

-- =============================================
-- 11. 系统配置表
-- =============================================
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
    `config_value` TEXT COMMENT '配置值',
    `description` VARCHAR(200) DEFAULT NULL COMMENT '配置描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- =============================================
-- 12. 阅读历史表
-- =============================================
DROP TABLE IF EXISTS `reading_history`;
CREATE TABLE `reading_history` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `article_id` BIGINT NOT NULL COMMENT '文章ID',
    `read_duration` INT DEFAULT 0 COMMENT '阅读时长(秒)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '首次阅读时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后阅读时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_article` (`user_id`, `article_id`),
    KEY `idx_user_time` (`user_id`, `update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='阅读历史表';

-- =============================================
-- 初始化数据
-- =============================================

-- 初始化角色
INSERT INTO `sys_role` (`role_name`, `role_code`, `description`, `status`) VALUES
('管理员', 'ADMIN', '系统管理员，拥有所有权限', 1),
('访客', 'visitor', '普通访客，只能浏览和评论', 1);

-- 初始化用户 (密码: admin123, test123 使用BCrypt加密)
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `email`, `status`, `role_id`, `role_code`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '管理员', 'admin@blog.com', 1, 1, 'ADMIN'),
('test', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EH', '测试用户', 'test@blog.com', 1, 2, 'visitor');

-- 更新正确的密码 (admin123, test123)
UPDATE `sys_user` SET `password` = '$2a$10$lUJ.95sSopikye1oRS4dF.A0ekXgm6tqGQPYdajs7S5NXbZUkmXLm' WHERE `username` = 'admin';
UPDATE `sys_user` SET `password` = '$2a$10$lUJ.95sSopikye1oRS4dF.A0ekXgm6tqGQPYdajs7S5NXbZUkmXLm' WHERE `username` = 'test';

-- 初始化分类
INSERT INTO `category` (`name`, `description`, `sort`, `status`) VALUES
('技术分享', '技术相关文章', 1, 1),
('生活随笔', '生活感悟类文章', 2, 1),
('学习笔记', '学习总结笔记', 3, 1);

-- 初始化标签
INSERT INTO `tag` (`name`, `color`) VALUES
('Java', '#f89820'),
('Spring Boot', '#6db33f'),
('Vue', '#42b883'),
('MySQL', '#00758f'),
('Redis', '#dc382d'),
('Docker', '#2496ed');

-- 初始化系统配置
INSERT INTO `sys_config` (`config_key`, `config_value`, `description`) VALUES
('site_name', '我的博客', '网站名称'),
('site_description', '一个简单的个人博客系统', '网站描述'),
('site_keywords', '博客,技术,分享', 'SEO关键词'),
('site_footer', 'Copyright © 2024 My Blog', '页脚信息'),
('comment_open', 'true', '是否开启评论'),
('comment_need_audit', 'true', '评论是否需要审核');
