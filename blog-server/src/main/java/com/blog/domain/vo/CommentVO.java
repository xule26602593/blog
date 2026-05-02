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

    private Integer likeCount;

    private Boolean isLiked;

    private Integer replyCount;

    private Long replyToUserId;

    private String replyToNickname;

    private LocalDateTime createTime;

    private List<ReplyVO> replies;

    private List<CommentVO> children;
}
