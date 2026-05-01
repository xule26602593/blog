package com.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@TableName("search_history")
public class SearchHistory implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String keyword;

    private Integer resultCount;

    private String ipAddress;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
