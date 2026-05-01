package com.blog.domain.vo;

import java.time.LocalDateTime;
import lombok.Data;

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
