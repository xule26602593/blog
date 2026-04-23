package com.blog.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SeriesVO {

    private Long id;

    private String name;

    private String description;

    private String coverImage;

    private Integer mode;

    private Integer articleCount;

    private Long viewCount;

    private Integer status;

    private LocalDateTime createTime;

    private List<SeriesArticleVO> articles;

    @Data
    public static class SeriesArticleVO {
        private Long id;
        private String title;
        private String summary;
        private String coverImage;
        private Long viewCount;
        private Integer chapterOrder;
    }
}
