package com.blog.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FollowVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private LocalDateTime followTime;
}
