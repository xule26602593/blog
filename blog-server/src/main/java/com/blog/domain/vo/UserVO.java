package com.blog.domain.vo;

import lombok.Data;

@Data
public class UserVO {
    
    private Long id;
    
    private String username;
    
    private String nickname;
    
    private String email;
    
    private String avatar;
    
    private String roleCode;
    
    private String createTime;

    private Integer points;

    private Integer totalPoints;

    private Integer level;

    private Integer checkinDays;

    private Integer maxConsecutiveDays;

    private Integer achievementCount;
}
