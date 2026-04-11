package com.blog.domain.dto;

import lombok.Data;

@Data
public class ArticleQueryDTO {
    
    private String title;
    
    private Long categoryId;
    
    private Long tagId;
    
    private Integer status;
    
    private Integer pageNum = 1;
    
    private Integer pageSize = 10;
}
