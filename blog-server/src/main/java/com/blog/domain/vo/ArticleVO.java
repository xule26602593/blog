package com.blog.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ArticleVO {
    
    private Long id;
    
    private String title;
    
    private String summary;
    
    private String content;
    
    private String coverImage;
    
    private Long categoryId;
    
    private String categoryName;
    
    private Long authorId;
    
    private String authorName;
    
    private String authorAvatar;
    
    private Long viewCount;
    
    private Long likeCount;
    
    private Integer commentCount;
    
    private Integer isTop;
    
    private Integer status;
    
    private LocalDateTime publishTime;
    
    private LocalDateTime createTime;
    
    private List<TagVO> tags;
    
    private Boolean isLiked;

    private Boolean isFavorited;

    /**
     * 上一篇文章
     */
    private ArticleNavVO prevArticle;

    /**
     * 下一篇文章
     */
    private ArticleNavVO nextArticle;

    /**
     * 相邻文章摘要信息
     */
    @Data
    public static class ArticleNavVO {
        private Long id;
        private String title;
        private String categoryName;
    }
}
