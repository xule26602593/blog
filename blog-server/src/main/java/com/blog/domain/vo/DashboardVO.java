package com.blog.domain.vo;

import lombok.Data;

@Data
public class DashboardVO {

    private Long articleCount;

    private Long commentCount;

    private Long userCount;

    private Long viewCount;

    private Long todayArticleCount;

    private Long todayCommentCount;
}
