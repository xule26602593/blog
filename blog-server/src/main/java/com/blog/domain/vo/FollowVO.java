package com.blog.domain.vo;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class FollowVO {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private LocalDateTime followTime;
}
