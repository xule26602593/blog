package com.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("user_checkin")
public class UserCheckin implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private LocalDate checkinDate;

    private Integer consecutiveDays;

    private Integer pointsEarned;

    private LocalDateTime checkinTime;

    private String ipAddress;

    private String deviceInfo;
}
