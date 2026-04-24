-- V003__add_user_bio_website.sql
ALTER TABLE `sys_user` ADD COLUMN `bio` VARCHAR(200) DEFAULT NULL COMMENT '个人简介';
ALTER TABLE `sys_user` ADD COLUMN `website` VARCHAR(255) DEFAULT NULL COMMENT '个人网站';
