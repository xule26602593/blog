package com.blog.domain.vo;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class MediaVO {

    private Long id;

    private String filename;

    private String originalName;

    private String fileUrl;

    private Long fileSize;

    private String fileType;

    private Integer useCount;

    private LocalDateTime createTime;
}
