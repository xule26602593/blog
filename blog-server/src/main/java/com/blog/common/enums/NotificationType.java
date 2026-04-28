package com.blog.common.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public enum NotificationType {
    FOLLOW(1, "FOLLOW", "关注"),
    COMMENT(2, "COMMENT", "评论"),
    REPLY(3, "REPLY", "回复"),
    ANNOUNCEMENT(4, "ANNOUNCEMENT", "公告");

    private final Integer code;
    private final String name;
    private final String desc;

    NotificationType(Integer code, String name, String desc) {
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    // 延迟加载缓存
    private static class Cache {
        static final Map<Integer, NotificationType> CODE_MAP = Arrays.stream(values())
                .collect(Collectors.toMap(NotificationType::getCode, Function.identity()));
        static final Map<String, NotificationType> NAME_MAP = Arrays.stream(values())
                .collect(Collectors.toMap(t -> t.name.toLowerCase(), Function.identity()));
    }

    public static String getNameByCode(Integer code) {
        NotificationType type = Cache.CODE_MAP.get(code);
        return type != null ? type.name : null;
    }

    public static Integer getCodeByName(String name) {
        if (name == null || name.isEmpty()) return null;
        NotificationType type = Cache.NAME_MAP.get(name.toLowerCase());
        return type != null ? type.code : null;
    }
}
