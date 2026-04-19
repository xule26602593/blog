package com.blog.domain.vo;

import lombok.Data;

@Data
public class FavoriteVO {

    private Long id;

    private Long articleId;

    private String title;

    private String summary;

    private String coverImage;

    private String authorName;

    private String categoryName;

    private Long viewCount;

    private Long likeCount;

    private String favoriteTime;
}
