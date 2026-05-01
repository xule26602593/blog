package com.blog.domain.vo;

import lombok.Data;

@Data
public class AchievementVO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String icon;
    private String category;
    private String type;
    private Integer conditionValue;
    private Integer points;
    private Integer level;
}
