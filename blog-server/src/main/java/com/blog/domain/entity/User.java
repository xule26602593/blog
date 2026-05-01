package com.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("sys_user")
public class User implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String nickname;

    private String email;

    private String avatar;

    private Integer status;

    private Long roleId;

    private String roleCode;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    private LocalDateTime lastLoginTime;

    @TableLogic
    private Integer deleted;

    private Integer followerCount;

    private Integer followingCount;

    private String bio;

    private String website;

    private Integer points;

    private Integer totalPoints;

    private Integer level;

    private Integer checkinDays;

    private Integer maxConsecutiveDays;

    private LocalDate lastCheckinDate;

    private Integer achievementCount;

    private Integer totalAchievementPoints;
}
