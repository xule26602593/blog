-- =============================================
-- 补充 AI 写作助手模板
-- 执行时间: 2026-04-30
-- 说明: 添加 expand_writing, rewrite_writing, proofread_writing 模板
-- =============================================

USE blog_db;

-- =============================================
-- 添加写作助手 Prompt 模板
-- =============================================
INSERT INTO `prompt_template` (`template_key`, `template_name`, `category`, `system_prompt`, `user_template`, `variables`, `is_default`) VALUES
('expand_writing', '内容扩写', 'writing',
 '你是写作助手，擅长扩展文章内容。请在保持原意和风格的基础上，适当扩展内容，增加细节、例证或深度分析。只返回扩写后的内容，不要解释。',
 '请扩写以下内容，使其更加丰富完整：\n{content}\n\n扩写方向：{direction}',
 '{"variables": ["content", "direction"]}', 1),

('rewrite_writing', '内容改写', 'writing',
 '你是写作助手，擅长改写文章内容。请根据指定的改写风格重新组织内容，保持核心信息不变。只返回改写后的内容，不要解释。',
 '请将以下内容改写为{style}风格：\n{content}',
 '{"variables": ["content", "style"]}', 1),

('proofread_writing', '内容纠错', 'writing',
 '你是专业编辑，擅长检查文章中的错误。请检查以下内容中的拼写错误、语法错误、标点错误和表达不当之处。返回JSON格式的纠错结果。',
 '请检查以下内容中的错误：\n{content}\n\n返回格式：{"errors":[{"type":"拼写/语法/标点/表达","original":"原文","corrected":"修改后","position":"位置说明"}],"correctedText":"修改后的完整文本"}',
 '{"variables": ["content"]}', 1)
ON DUPLICATE KEY UPDATE
    `template_name` = VALUES(`template_name`),
    `category` = VALUES(`category`),
    `system_prompt` = VALUES(`system_prompt`),
    `user_template` = VALUES(`user_template`),
    `variables` = VALUES(`variables`),
    `is_default` = VALUES(`is_default`);

-- 完成
SELECT '写作助手模板添加完成' AS message;
