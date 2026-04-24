package com.blog.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

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
