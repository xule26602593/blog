package com.blog.domain.enums;

import lombok.Getter;

/**
 * 话题优先级枚举
 */
@Getter
public enum TopicPriority {
    HIGH(1, "HIGH", "高"),
    MEDIUM(2, "MEDIUM", "中"),
    LOW(3, "LOW", "低");

    private final Integer code;
    private final String name;
    private final String desc;

    TopicPriority(Integer code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public static TopicPriority fromCode(Integer code) {
        if (code == null) return null;
        for (TopicPriority priority : values()) {
            if (priority.getCode().equals(code)) {
                return priority;
            }
        }
        return null;
    }

    public static TopicPriority fromName(String name) {
        if (name == null || name.isBlank()) return null;
        for (TopicPriority priority : values()) {
            if (priority.getName().equalsIgnoreCase(name)) {
                return priority;
            }
        }
        return null;
    }

    public static Integer codeFromName(String name) {
        TopicPriority priority = fromName(name);
        return priority != null ? priority.getCode() : null;
    }

    public static String nameFromCode(Integer code) {
        TopicPriority priority = fromCode(code);
        return priority != null ? priority.getName() : null;
    }
}
