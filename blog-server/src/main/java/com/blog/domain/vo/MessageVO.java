package com.blog.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageVO {

    private Long id;

    private Long userId;

    private String nickname;

    private String email;

    private String content;

    private Integer status;

    private String avatar;

    private LocalDateTime createTime;
}
