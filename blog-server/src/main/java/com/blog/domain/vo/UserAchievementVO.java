package com.blog.domain.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserAchievementVO {
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
    private Integer progress;
    private Boolean unlocked;
    private LocalDateTime unlockTime;
    private Integer progressPercent;
}
