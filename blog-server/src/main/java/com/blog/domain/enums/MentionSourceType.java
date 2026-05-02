package com.blog.domain.enums;

import lombok.Getter;

@Getter
public enum MentionSourceType {
    COMMENT("COMMENT", "评论"),
    ARTICLE("ARTICLE", "文章");

    private final String code;
    private final String desc;

    MentionSourceType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
