package com.blog.domain.vo;

import lombok.Data;

@Data
public class StatisticsOverviewVO {

    private Integer todayPv;

    private Integer todayUv;

    private Integer totalArticles;

    private Integer totalComments;

    private Integer totalUsers;

    private Integer pendingComments;

    private Integer pendingMessages;
}
