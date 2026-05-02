package com.blog.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.PageResult;
import com.blog.domain.dto.CommentDTO;
import com.blog.domain.dto.LikeResultDTO;
import com.blog.domain.enums.CommentSortType;
import com.blog.domain.vo.CommentVO;
import com.blog.domain.vo.ReplyVO;
import com.blog.domain.vo.UserSimpleVO;

public interface CommentService {

    Page<CommentVO> pageComment(Long articleId, int pageNum, int pageSize);

    Page<CommentVO> pageAdminComment(int status, int pageNum, int pageSize);

    void addComment(CommentDTO dto);

    void auditComment(Long id, Integer status);

    void deleteComment(Long id);

    Long countPending();

    // ========== 新增方法 ==========

    /**
     * 获取评论列表（支持排序）
     */
    PageResult<CommentVO> listComments(
            Long articleId, CommentSortType sortType, int page, int size, Long currentUserId);

    /**
     * 创建评论（处理@提及）
     */
    CommentVO createComment(CommentDTO dto, Long currentUserId);

    /**
     * 获取回复列表
     */
    PageResult<ReplyVO> listReplies(Long commentId, CommentSortType sortType, int page, int size, Long currentUserId);

    /**
     * 点赞/取消点赞评论
     */
    LikeResultDTO toggleLike(Long commentId, Long userId);

    /**
     * 获取评论点赞列表
     */
    PageResult<UserSimpleVO> listLikes(Long commentId, int page, int size);
}
