package com.blog.domain.enums;

import lombok.Getter;

@Getter
public enum ActionType {
    LIKE(1, "点赞"),
    FAVORITE(2, "收藏");

    private final Integer code;
    private final String desc;

    ActionType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
