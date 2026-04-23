package com.blog.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AnnouncementVO {
    private Long id;
    private String title;
    private String content;
    private Integer status;
    private LocalDateTime publishTime;
    private LocalDateTime createTime;
}
