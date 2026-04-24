package com.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("media")
public class Media implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String filename;

    private String originalName;

    private String filePath;

    private String fileUrl;

    private Long fileSize;

    private String fileType;

    private Long uploaderId;

    private Integer useCount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
