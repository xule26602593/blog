package com.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_reading_profile")
public class UserReadingProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String preferredTags;
    private String preferredCategories;
    private String readingPattern;
    private LocalDateTime updateTime;
}
