package com.blog.common.result;

import lombok.Getter;

@Getter
public enum ErrorCode {
    SUCCESS(200, "success"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "没有权限访问"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 业务错误码 1000+
    USERNAME_EXISTS(1001, "用户名已存在"),
    EMAIL_EXISTS(1002, "邮箱已被注册"),
    USER_NOT_FOUND(1003, "用户不存在"),
    PASSWORD_ERROR(1004, "密码错误"),
    ACCOUNT_DISABLED(1005, "账号已被禁用"),
    LOGIN_ERROR(1006, "登录失败"),
    TOKEN_INVALID(1007, "Token无效"),
    TOKEN_EXPIRED(1008, "Token已过期"),

    // 文章相关 2000+
    ARTICLE_NOT_FOUND(2001, "文章不存在"),
    CATEGORY_NOT_FOUND(2002, "分类不存在"),
    TAG_NOT_FOUND(2003, "标签不存在"),
    ARTICLE_STATUS_ERROR(2004, "文章状态错误"),

    // 评论相关 3000+
    COMMENT_NOT_FOUND(3001, "评论不存在"),
    COMMENT_DISABLED(3002, "评论功能已关闭"),

    // 文件相关 4000+
    FILE_UPLOAD_ERROR(4001, "文件上传失败"),
    FILE_TYPE_ERROR(4002, "文件类型不支持"),
    FILE_SIZE_ERROR(4003, "文件大小超出限制"),
    FILE_NOT_FOUND(4004, "文件不存在");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
