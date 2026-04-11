package com.blog.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ArticleDTO {
    
    private Long id;
    
    @NotBlank(message = "文章标题不能为空")
    private String title;
    
    private String summary;
    
    @NotBlank(message = "文章内容不能为空")
    private String content;
    
    private String coverImage;
    
    @NotNull(message = "请选择分类")
    private Long categoryId;
    
    private List<Long> tagIds;
    
    private Integer isTop;
    
    private Integer status;
}
