package com.blog.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("search_suggestion")
public class SearchSuggestion implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String keyword;

    private Integer searchCount;

    private LocalDateTime lastSearchTime;
}
