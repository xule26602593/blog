package com.blog.domain.vo;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AnnouncementVO {
    private Long id;
    private String title;
    private String content;
    private Integer status;
    private LocalDateTime publishTime;
    private LocalDateTime createTime;
}
