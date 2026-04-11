package com.blog.domain.vo;

import lombok.Data;

import java.util.List;

@Data
public class ArticleListVO {

    private Long id;

    private String title;

    private String summary;

    private String coverImage;

    private Long categoryId;

    private String categoryName;

    private Long viewCount;

    private Long likeCount;

    private Integer commentCount;

    private Integer isTop;

    private String publishTime;

    private List<TagVO> tags;

    private Integer status;
}
