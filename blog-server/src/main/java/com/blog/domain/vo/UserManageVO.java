package com.blog.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserManageVO {

    private Long id;

    private String username;

    private String nickname;

    private String email;

    private String avatar;

    private String bio;

    private String website;

    private Integer status;

    private String roleCode;

    private Integer followerCount;

    private Integer followingCount;

    private LocalDateTime createTime;

    private LocalDateTime lastLoginTime;
}
