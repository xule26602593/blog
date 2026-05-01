package com.blog.domain.enums;

import lombok.Getter;

/**
 * 话题状态枚举
 */
@Getter
public enum TopicStatus {
    PENDING(0, "PENDING", "待写"),
    WRITING(1, "WRITING", "写作中"),
    PUBLISHED(2, "PUBLISHED", "已发布"),
    ABANDONED(3, "ABANDONED", "放弃");

    private final Integer code;
    private final String name;
    private final String desc;

    TopicStatus(Integer code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public static TopicStatus fromCode(Integer code) {
        if (code == null) return null;
        for (TopicStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }

    public static TopicStatus fromName(String name) {
        if (name == null || name.isBlank()) return null;
        for (TopicStatus status : values()) {
            if (status.getName().equalsIgnoreCase(name)) {
                return status;
            }
        }
        return null;
    }

    public static Integer codeFromName(String name) {
        TopicStatus status = fromName(name);
        return status != null ? status.getCode() : null;
    }

    public static String nameFromCode(Integer code) {
        TopicStatus status = fromCode(code);
        return status != null ? status.getName() : null;
    }
}
