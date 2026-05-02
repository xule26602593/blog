package com.blog.domain.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.Getter;

@Getter
public enum CommentSortType {
    HOT("hot", "最热"),
    NEWEST("newest", "最新"),
    OLDEST("oldest", "最早");

    private final String code;
    private final String desc;

    CommentSortType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    private static class Cache {
        static final Map<String, CommentSortType> CODE_MAP =
                Arrays.stream(values()).collect(Collectors.toMap(CommentSortType::getCode, Function.identity()));
    }

    public static CommentSortType fromCode(String code) {
        if (code == null) return HOT; // 默认最热
        return Cache.CODE_MAP.getOrDefault(code.toLowerCase(), HOT);
    }
}
