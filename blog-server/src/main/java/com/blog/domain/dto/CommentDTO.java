package com.blog.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentDTO {
    
    private Long id;
    
    @NotNull(message = "文章ID不能为空")
    private Long articleId;
    
    private Long parentId;
    
    private Long replyId;
    
    @NotBlank(message = "评论内容不能为空")
    private String content;
    
    private String nickname;
    
    private String email;
}
