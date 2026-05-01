-- =============================================
-- 写作模板表
-- 执行时间: 2026-04-30
-- =============================================

USE blog_db;

CREATE TABLE IF NOT EXISTS `writing_template` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name` VARCHAR(100) NOT NULL COMMENT '模板名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '模板描述',
    `content` LONGTEXT NOT NULL COMMENT '模板内容(Markdown)',
    `category_id` BIGINT DEFAULT NULL COMMENT '默认分类ID',
    `default_tags` VARCHAR(255) DEFAULT NULL COMMENT '默认标签(JSON数组)',
    `preview_image` VARCHAR(255) DEFAULT NULL COMMENT '预览图片URL',
    `usage_count` INT DEFAULT 0 COMMENT '使用次数',
    `is_builtin` TINYINT DEFAULT 0 COMMENT '是否内置模板',
    `is_default` TINYINT DEFAULT 0 COMMENT '是否默认模板',
    `status` TINYINT DEFAULT 1 COMMENT '状态 0:禁用 1:启用',
    `author_id` BIGINT DEFAULT NULL COMMENT '创建者ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除 0:未删除 1:已删除',
    PRIMARY KEY (`id`),
    KEY `idx_status_builtin` (`status`, `is_builtin`),
    KEY `idx_author` (`author_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_general_ci COMMENT='写作模板表';

INSERT INTO `writing_template` (`name`, `description`, `content`, `is_builtin`, `is_default`, `status`) VALUES
('技术教程', '适合技术类教程文章，包含环境准备、步骤说明、代码示例',
 '## 简介\n\n简要介绍本文要解决什么问题。\n\n## 环境准备\n\n### 前置条件\n- 条件1\n- 条件2\n\n### 安装步骤\n\n```bash\n# 安装命令\n```\n\n## 实现步骤\n\n### 步骤一：xxx\n\n说明...\n\n```javascript\n// 代码示例\n```\n\n### 步骤二：xxx\n\n说明...\n\n## 常见问题\n\n### Q1: xxx?\nA: xxx\n\n## 总结\n\n本文介绍了...',
 1, 1, 1),

('问题排查', '适合问题排查类文章，描述问题、分析过程、解决方案',
 '## 问题描述\n\n### 现象\n描述问题的具体表现...\n\n### 环境\n- 操作系统：\n- 软件版本：\n\n## 分析过程\n\n### 初步排查\n1. xxx\n2. xxx\n\n### 深入分析\n\n通过xxx发现...\n\n```bash\n# 排查命令\ndiagnostic_command\n```\n\n### 根因定位\n\n最终发现问题是...\n\n## 解决方案\n\n### 方案一\n\n步骤...\n\n### 方案二（推荐）\n\n步骤...\n\n## 预防措施\n\n- 建议1\n- 建议2\n\n## 参考\n\n- [链接1](url)\n- [链接2](url)',
 1, 0, 1),

('学习笔记', '适合学习总结类文章，记录知识点、心得体会',
 '## 学习主题\n\n> 简要说明学习内容和目标\n\n## 核心概念\n\n### 概念1\n\n解释...\n\n### 概念2\n\n解释...\n\n## 要点总结\n\n### 要点1\n- 细节a\n- 细节b\n\n### 要点2\n- 细节a\n- 细节b\n\n## 代码示例\n\n```javascript\n// 示例代码\n```\n\n## 心得体会\n\n学习过程中的一些思考...\n\n## 扩展阅读\n\n- [资源1](url)\n- [资源2](url)',
 1, 0, 1),

('产品介绍', '适合产品功能介绍类文章',
 '## 产品概述\n\n简要介绍产品定位和核心价值。\n\n## 功能特性\n\n### 特性一：xxx\n\n功能描述...\n\n**应用场景：**\n- 场景1\n- 场景2\n\n### 特性二：xxx\n\n功能描述...\n\n## 使用指南\n\n### 快速开始\n\n1. 步骤1\n2. 步骤2\n\n### 进阶使用\n\n高级功能介绍...\n\n## 常见问题\n\n### Q1: xxx?\nA: xxx\n\n## 用户反馈\n\n> 用户评价...\n\n## 总结\n\n产品优势总结...',
 1, 0, 1),

('经验分享', '适合经验分享类文章',
 '## 背景\n\n介绍分享的背景和动机...\n\n## 问题与挑战\n\n遇到的主要问题：\n1. 问题1\n2. 问题2\n\n## 解决思路\n\n### 思路一\n说明...\n\n### 思路二\n说明...\n\n## 实践过程\n\n### 尝试一\n过程...\n结果...\n\n### 尝试二（最终方案）\n过程...\n结果...\n\n## 经验总结\n\n### 核心经验\n1. 经验1\n2. 经验2\n\n### 踩坑提醒\n- 坑1：xxx\n- 坑2：xxx\n\n## 后续优化\n\n可以改进的方向...\n\n## 参考资料\n\n- [资料1](url)',
 1, 0, 1);

SELECT '写作模板表创建完成' AS message;
