package com.blog.service;

import com.blog.common.result.PageResult;
import com.blog.domain.dto.LikeResultDTO;
import com.blog.domain.vo.UserSimpleVO;
import java.util.List;
import java.util.Map;

public interface CommentLikeService {

    /**
     * 点赞/取消点赞（幂等操作）
     */
    LikeResultDTO toggleLike(Long commentId, Long userId);

    /**
     * 获取评论的点赞用户列表
     */
    PageResult<UserSimpleVO> listLikes(Long commentId, int page, int size);

    /**
     * 检查用户是否已点赞
     */
    boolean isLiked(Long commentId, Long userId);

    /**
     * 批量检查点赞状态
     * @return Map<commentId, isLiked>
     */
    Map<Long, Boolean> batchCheckLiked(List<Long> commentIds, Long userId);
}
