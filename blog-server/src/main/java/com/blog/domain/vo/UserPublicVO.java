package com.blog.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserPublicVO {

    private Long id;

    private String nickname;

    private String avatar;

    private String bio;

    private String website;

    private Integer followerCount;

    private Integer followingCount;

    private LocalDateTime createTime;

    private Boolean isFollowing; // 当前用户是否关注了该用户
}
