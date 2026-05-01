package com.blog.domain.vo;

import java.time.LocalDateTime;
import lombok.Data;

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
