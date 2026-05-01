package com.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.domain.dto.CommentDTO;
import com.blog.domain.vo.CommentVO;

public interface CommentService {

    Page<CommentVO> pageComment(Long articleId, int pageNum, int pageSize);

    Page<CommentVO> pageAdminComment(int status, int pageNum, int pageSize);

    void addComment(CommentDTO dto);

    void auditComment(Long id, Integer status);

    void deleteComment(Long id);

    Long countPending();
}
