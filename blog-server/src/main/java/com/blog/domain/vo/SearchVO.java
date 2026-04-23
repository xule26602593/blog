package com.blog.domain.vo;

import lombok.Data;

@Data
public class SearchVO {

    private Long id;

    private String title;

    private String summary;

    private String contentHighlight;

    private String coverImage;

    private String categoryName;

    private String publishTime;

    private Double score;
}
