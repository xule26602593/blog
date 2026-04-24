package com.blog.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RevisionVO {

    private Long id;

    private Long articleId;

    private Integer version;

    private String title;

    private String content;

    private String summary;

    private Long editorId;

    private String editorNickname;

    private String changeNote;

    private LocalDateTime createTime;
}
