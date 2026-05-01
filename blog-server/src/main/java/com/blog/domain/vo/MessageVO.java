package com.blog.domain.vo;

import java.time.LocalDateTime;
import lombok.Data;

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
