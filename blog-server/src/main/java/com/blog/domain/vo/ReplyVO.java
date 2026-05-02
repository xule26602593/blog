package com.blog.domain.vo;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ReplyVO {
    private Long id;
    private Long userId;
    private String nickname;
    private String avatar;
    private String content;
    private Integer likeCount;
    private Boolean isLiked;
    private Long replyToUserId;
    private String replyToNickname;
    private LocalDateTime createTime;
}
