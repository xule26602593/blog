package com.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.domain.vo.RevisionVO;

public interface RevisionService {

    void createSnapshot(Long articleId, Long editorId, String changeNote);

    Page<RevisionVO> getRevisions(Long articleId, int pageNum, int pageSize);

    RevisionVO getRevision(Long articleId, Integer version);

    void restore(Long articleId, Integer version, Long editorId);
}
