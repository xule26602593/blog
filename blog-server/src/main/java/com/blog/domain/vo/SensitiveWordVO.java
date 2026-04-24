package com.blog.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SensitiveWordVO {

    private Long id;

    private String word;

    private String category;

    private String replaceWord;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
