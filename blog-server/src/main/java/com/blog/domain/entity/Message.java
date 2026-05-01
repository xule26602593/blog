package com.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("message")
public class Message {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String nickname;

    private String email;

    private String content;

    private Integer status;

    private String ipAddress;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
