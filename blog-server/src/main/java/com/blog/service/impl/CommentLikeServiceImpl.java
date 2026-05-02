package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.result.PageResult;
import com.blog.domain.dto.LikeResultDTO;
import com.blog.domain.entity.CommentLike;
import com.blog.domain.entity.User;
import com.blog.domain.vo.UserSimpleVO;
import com.blog.repository.mapper.CommentLikeMapper;
import com.blog.repository.mapper.CommentMapper;
import com.blog.repository.mapper.UserMapper;
import com.blog.service.CommentLikeService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentLikeServiceImpl implements CommentLikeService {

    private final CommentLikeMapper commentLikeMapper;
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public LikeResultDTO toggleLike(Long commentId, Long userId) {
        // 查询是否已点赞
        CommentLike existing = commentLikeMapper.selectOne(new LambdaQueryWrapper<CommentLike>()
                .eq(CommentLike::getCommentId, commentId)
                .eq(CommentLike::getUserId, userId));

        boolean liked;
        if (existing != null) {
            // 已点赞，取消
            commentLikeMapper.deleteById(existing.getId());
            commentLikeMapper.decrementLikeCount(commentId);
            liked = false;
        } else {
            // 未点赞，添加
            CommentLike like = new CommentLike();
            like.setCommentId(commentId);
            like.setUserId(userId);
            commentLikeMapper.insert(like);
            commentLikeMapper.incrementLikeCount(commentId);
            liked = true;
        }

        Integer likeCount = commentMapper.selectLikeCount(commentId);
        return new LikeResultDTO(liked, likeCount);
    }

    @Override
    public PageResult<UserSimpleVO> listLikes(Long commentId, int page, int size) {
        Page<CommentLike> likePage = new Page<>(page, size);
        Page<CommentLike> result = commentLikeMapper.selectPage(
                likePage,
                new LambdaQueryWrapper<CommentLike>()
                        .eq(CommentLike::getCommentId, commentId)
                        .orderByDesc(CommentLike::getCreateTime));

        List<UserSimpleVO> users = result.getRecords().stream()
                .map(like -> {
                    User user = userMapper.selectById(like.getUserId());
                    if (user == null) return null;
                    UserSimpleVO vo = new UserSimpleVO();
                    vo.setUserId(user.getId());
                    vo.setNickname(user.getNickname());
                    vo.setAvatar(user.getAvatar());
                    return vo;
                })
                .filter(vo -> vo != null)
                .collect(Collectors.toList());

        return PageResult.of(users, result.getTotal(), (long) size, (long) page);
    }

    @Override
    public boolean isLiked(Long commentId, Long userId) {
        if (userId == null) return false;
        return commentLikeMapper.selectCount(new LambdaQueryWrapper<CommentLike>()
                        .eq(CommentLike::getCommentId, commentId)
                        .eq(CommentLike::getUserId, userId))
                > 0;
    }

    @Override
    public Map<Long, Boolean> batchCheckLiked(List<Long> commentIds, Long userId) {
        Map<Long, Boolean> result = new HashMap<>();
        if (userId == null || commentIds == null || commentIds.isEmpty()) {
            commentIds.forEach(id -> result.put(id, false));
            return result;
        }

        // 初始化全部为 false
        commentIds.forEach(id -> result.put(id, false));

        // 查询已点赞的
        List<CommentLike> likes = commentLikeMapper.selectList(new LambdaQueryWrapper<CommentLike>()
                .eq(CommentLike::getUserId, userId)
                .in(CommentLike::getCommentId, commentIds));

        likes.forEach(like -> result.put(like.getCommentId(), true));
        return result;
    }
}
