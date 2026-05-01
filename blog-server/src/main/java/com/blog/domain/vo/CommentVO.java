package com.blog.domain.vo;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class CommentVO {

    private Long id;

    private Long articleId;

    private String articleTitle;

    private Long parentId;

    private Long replyId;

    private Long userId;

    private String nickname;

    private String email;

    private String avatar;

    private String content;

    private Integer status;

    private LocalDateTime createTime;

    private List<CommentVO> children;
}
