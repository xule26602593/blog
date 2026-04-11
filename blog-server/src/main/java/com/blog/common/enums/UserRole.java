package com.blog.common.enums;

import lombok.Getter;

@Getter
public enum UserRole {
    ADMIN("admin", "管理员"),
    VISITOR("visitor", "访客");

    private final String code;
    private final String desc;

    UserRole(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
