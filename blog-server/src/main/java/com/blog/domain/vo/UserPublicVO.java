package com.blog.domain.vo;

import java.time.LocalDateTime;
import lombok.Data;

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
