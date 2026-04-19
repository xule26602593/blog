package com.blog.domain.vo;

import lombok.Data;

@Data
public class ReadingHistoryVO {

    private Long id;

    private Long articleId;

    private String title;

    private String coverImage;

    private String lastReadTime;
}
