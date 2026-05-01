-- =============================================
-- 排版规则配置表
-- 执行时间: 2026-04-30
-- =============================================

USE blog_db;

CREATE TABLE IF NOT EXISTS `format_rule` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `rule_key` VARCHAR(50) NOT NULL COMMENT '规则标识',
    `rule_name` VARCHAR(100) NOT NULL COMMENT '规则名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '规则描述',
    `rule_type` VARCHAR(50) NOT NULL COMMENT '类型：regex/ai/http',
    `rule_config` JSON NOT NULL COMMENT '规则配置JSON',
    `priority` INT DEFAULT 0 COMMENT '执行优先级（数字越小越先执行）',
    `is_default` TINYINT DEFAULT 1 COMMENT '是否默认启用',
    `status` TINYINT DEFAULT 1 COMMENT '状态 0:禁用 1:启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_rule_key` (`rule_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排版规则配置表';

INSERT INTO `format_rule` (`rule_key`, `rule_name`, `description`, `rule_type`, `rule_config`, `priority`, `is_default`) VALUES
('heading_normalize', '标题层级规范化', '确保标题层级连续，避免跳级', 'regex',
 '{"patterns":[{"pattern":"^#{1}\\\\s","replaceWith":"## "},{"pattern":"^#{4,}\\\\s","replaceWith":"### "}]}',
 1, 1),

('empty_line_cleanup', '空行清理', '删除多余空行', 'regex',
 '{"pattern":"\\\\n{3,}","replacement":"\\\\n\\\\n"}',
 10, 1),

('chinese_english_spacing', '中英文间距', '中文与英文/数字间添加空格', 'regex',
 '{"patterns":[{"pattern":"([\\\\u4e00-\\\\u9fa5])([a-zA-Z0-9])","replacement":"$1 $2"},{"pattern":"([a-zA-Z0-9])([\\\\u4e00-\\\\u9fa5])","replacement":"$1 $2"}]}',
 5, 1),

('punctuation_normalize', '标点规范化', '规范化中文标点', 'regex',
 '{"patterns":[{"pattern":",","replacement":"，"},{"pattern":"\\\\. ","replacement":"。 "},{"pattern":"!","replacement":"！"},{"pattern":"\\\\?","replacement":"？"}],"skipInCode":true}',
 8, 1),

('code_block_format', '代码块格式化', '为代码块添加语言标识', 'regex',
 '{"patterns":[{"pattern":"```\\\\n","replacement":"```java\\\\n"},{"pattern":"```\\\\s*$","replacement":"```java\\\\n"}]}',
 20, 1),

('link_check', '链接检查', '检测无效链接', 'http',
 '{"timeout":5000,"allowedStatusCodes":[200,301,302]}',
 100, 1);

SELECT '排版规则表创建完成' AS message;
