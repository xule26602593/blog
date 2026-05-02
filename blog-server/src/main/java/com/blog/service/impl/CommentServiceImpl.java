package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.exception.BusinessException;
import com.blog.common.result.ErrorCode;
import com.blog.common.result.PageResult;
import com.blog.common.utils.BeanCopyUtils;
import com.blog.common.utils.IpUtils;
import com.blog.domain.dto.CommentDTO;
import com.blog.domain.dto.LikeResultDTO;
import com.blog.domain.entity.Article;
import com.blog.domain.entity.Comment;
import com.blog.domain.entity.User;
import com.blog.domain.enums.CommentSortType;
import com.blog.domain.enums.MentionSourceType;
import com.blog.domain.vo.CommentVO;
import com.blog.domain.vo.ReplyVO;
import com.blog.domain.vo.UserSimpleVO;
import com.blog.repository.mapper.ArticleMapper;
import com.blog.repository.mapper.CommentMapper;
import com.blog.repository.mapper.UserMapper;
import com.blog.security.LoginUser;
import com.blog.service.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;
    private final SensitiveWordService sensitiveWordService;
    private final AchievementTriggerService achievementTriggerService;
    private final CommentLikeService commentLikeService;
    private final MentionService mentionService;

    @Override
    public Page<CommentVO> pageComment(Long articleId, int pageNum, int pageSize) {
        // 查询顶级评论
        Page<Comment> page = new Page<>(pageNum, pageSize);
        Page<Comment> commentPage = commentMapper.selectPage(
                page,
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getArticleId, articleId)
                        .eq(Comment::getParentId, 0)
                        .eq(Comment::getStatus, 1)
                        .orderByDesc(Comment::getCreateTime));

        Page<CommentVO> voPage = new Page<>(pageNum, pageSize, commentPage.getTotal());
        voPage.setRecords(convertToVOList(commentPage.getRecords(), articleId));

        return voPage;
    }

    @Override
    public Page<CommentVO> pageAdminComment(int status, int pageNum, int pageSize) {
        Page<Comment> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        if (status >= 0) {
            wrapper.eq(Comment::getStatus, status);
        }
        wrapper.orderByDesc(Comment::getCreateTime);

        Page<Comment> commentPage = commentMapper.selectPage(page, wrapper);

        Page<CommentVO> voPage = new Page<>(pageNum, pageSize, commentPage.getTotal());
        voPage.setRecords(commentPage.getRecords().stream()
                .map(comment -> {
                    CommentVO vo = BeanCopyUtils.copy(comment, CommentVO.class);
                    if (comment.getArticleId() != null) {
                        Article article = articleMapper.selectById(comment.getArticleId());
                        if (article != null) {
                            vo.setArticleTitle(article.getTitle());
                        }
                    }
                    if (comment.getUserId() != null) {
                        User user = userMapper.selectById(comment.getUserId());
                        if (user != null) {
                            vo.setAvatar(user.getAvatar());
                        }
                    }
                    return vo;
                })
                .collect(Collectors.toList()));

        return voPage;
    }

    @Override
    @Transactional
    public void addComment(CommentDTO dto) {
        Comment comment = new Comment();
        comment.setArticleId(dto.getArticleId());
        comment.setParentId(dto.getParentId() != null ? dto.getParentId() : 0L);
        comment.setReplyId(dto.getReplyId());

        // 敏感词过滤
        String filteredContent = sensitiveWordService.filter(dto.getContent());
        comment.setContent(filteredContent);

        comment.setIpAddress(IpUtils.getIpAddress());

        // 获取当前登录用户
        Long userId = getCurrentUserId();
        if (userId != null) {
            comment.setUserId(userId);
            User user = userMapper.selectById(userId);
            if (user != null) {
                comment.setNickname(user.getNickname());
                comment.setEmail(user.getEmail());
            }
        } else {
            // 游客评论
            comment.setNickname(dto.getNickname());
            comment.setEmail(dto.getEmail());
        }

        // 默认需要审核
        comment.setStatus(0);

        commentMapper.insert(comment);

        // 更新文章评论数（审核通过后才更新）
        // articleMapper.updateCommentCount(dto.getArticleId(), 1);
    }

    @Override
    @Transactional
    public void auditComment(Long id, Integer status) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
        }

        int oldStatus = comment.getStatus();
        commentMapper.updateStatus(id, status);

        // 审核通过时更新文章评论数
        if (status == 1 && oldStatus != 1) {
            articleMapper.updateCommentCount(comment.getArticleId(), 1);
            if (comment.getUserId() != null) {
                achievementTriggerService.triggerCommentAchievements(comment.getUserId());
            }
        } else if (status != 1 && oldStatus == 1) {
            articleMapper.updateCommentCount(comment.getArticleId(), -1);
        }
    }

    @Override
    @Transactional
    public void deleteComment(Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            return;
        }

        // 如果已审核通过，减少文章评论数
        if (comment.getStatus() == 1) {
            articleMapper.updateCommentCount(comment.getArticleId(), -1);
        }

        commentMapper.deleteById(id);
    }

    @Override
    public Long countPending() {
        return commentMapper.selectCount(new LambdaQueryWrapper<Comment>().eq(Comment::getStatus, 0));
    }

    private List<CommentVO> convertToVOList(List<Comment> comments, Long articleId) {
        if (comments.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取所有子评论
        List<Long> parentIds = comments.stream().map(Comment::getId).collect(Collectors.toList());
        List<Comment> children = commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getArticleId, articleId)
                .in(Comment::getParentId, parentIds)
                .eq(Comment::getStatus, 1));

        Map<Long, List<Comment>> childrenMap = children.stream().collect(Collectors.groupingBy(Comment::getParentId));

        return comments.stream()
                .map(comment -> {
                    CommentVO vo = BeanCopyUtils.copy(comment, CommentVO.class);

                    if (comment.getUserId() != null) {
                        User user = userMapper.selectById(comment.getUserId());
                        if (user != null) {
                            vo.setAvatar(user.getAvatar());
                        }
                    }

                    // 设置子评论
                    List<Comment> childComments = childrenMap.get(comment.getId());
                    if (childComments != null) {
                        vo.setChildren(childComments.stream()
                                .map(child -> {
                                    CommentVO childVo = BeanCopyUtils.copy(child, CommentVO.class);
                                    if (child.getUserId() != null) {
                                        User user = userMapper.selectById(child.getUserId());
                                        if (user != null) {
                                            childVo.setAvatar(user.getAvatar());
                                        }
                                    }
                                    return childVo;
                                })
                                .collect(Collectors.toList()));
                    }

                    return vo;
                })
                .collect(Collectors.toList());
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            return ((LoginUser) authentication.getPrincipal()).getUserId();
        }
        return null;
    }

    // ========== 新增方法 ==========

    @Override
    public PageResult<CommentVO> listComments(
            Long articleId, CommentSortType sortType, int page, int size, Long currentUserId) {
        // 构建排序条件
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getArticleId, articleId).eq(Comment::getStatus, 1).eq(Comment::getParentId, 0);

        switch (sortType) {
            case HOT:
                wrapper.orderByDesc(Comment::getLikeCount);
                break;
            case NEWEST:
                wrapper.orderByDesc(Comment::getCreateTime);
                break;
            case OLDEST:
                wrapper.orderByAsc(Comment::getCreateTime);
                break;
        }

        Page<Comment> commentPage = commentMapper.selectPage(new Page<>(page, size), wrapper);

        // 获取所有评论ID用于批量检查点赞状态
        List<Long> commentIds =
                commentPage.getRecords().stream().map(Comment::getId).collect(Collectors.toList());
        Map<Long, Boolean> likedMap = commentLikeService.batchCheckLiked(commentIds, currentUserId);

        // 转换为VO
        List<CommentVO> vos = commentPage.getRecords().stream()
                .map(comment -> convertToNewVO(comment, likedMap, currentUserId))
                .collect(Collectors.toList());

        return PageResult.of(vos, commentPage.getTotal(), (long) size, (long) page);
    }

    @Override
    @Transactional
    public CommentVO createComment(CommentDTO dto, Long currentUserId) {
        Comment comment = new Comment();
        comment.setArticleId(dto.getArticleId());
        comment.setParentId(dto.getParentId() != null ? dto.getParentId() : 0L);
        comment.setReplyId(dto.getReplyId());
        comment.setReplyToUserId(dto.getReplyToUserId());

        // 敏感词过滤
        String filteredContent = sensitiveWordService.filter(dto.getContent());
        comment.setContent(filteredContent);
        comment.setLikeCount(0);
        comment.setIpAddress(IpUtils.getIpAddress());

        if (currentUserId != null) {
            comment.setUserId(currentUserId);
            User user = userMapper.selectById(currentUserId);
            if (user != null) {
                comment.setNickname(user.getNickname());
                comment.setEmail(user.getEmail());
            }
        } else {
            comment.setNickname(dto.getNickname());
            comment.setEmail(dto.getEmail());
        }

        comment.setStatus(1); // 默认通过
        commentMapper.insert(comment);

        // 更新文章评论数
        articleMapper.updateCommentCount(dto.getArticleId(), 1);

        // 异步处理@提及
        if (currentUserId != null) {
            mentionService.createMentions(MentionSourceType.COMMENT, comment.getId(), currentUserId, filteredContent);
        }

        return convertToNewVO(comment, Map.of(), currentUserId);
    }

    @Override
    public PageResult<ReplyVO> listReplies(
            Long commentId, CommentSortType sortType, int page, int size, Long currentUserId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getParentId, commentId).eq(Comment::getStatus, 1);

        switch (sortType) {
            case HOT:
                wrapper.orderByDesc(Comment::getLikeCount);
                break;
            case NEWEST:
                wrapper.orderByDesc(Comment::getCreateTime);
                break;
            case OLDEST:
                wrapper.orderByAsc(Comment::getCreateTime);
                break;
        }

        Page<Comment> replyPage = commentMapper.selectPage(new Page<>(page, size), wrapper);

        // 批量检查点赞状态
        List<Long> replyIds =
                replyPage.getRecords().stream().map(Comment::getId).collect(Collectors.toList());
        Map<Long, Boolean> likedMap = commentLikeService.batchCheckLiked(replyIds, currentUserId);

        List<ReplyVO> vos = replyPage.getRecords().stream()
                .map(reply -> convertToReplyVO(reply, likedMap))
                .collect(Collectors.toList());

        return PageResult.of(vos, replyPage.getTotal(), (long) size, (long) page);
    }

    @Override
    public LikeResultDTO toggleLike(Long commentId, Long userId) {
        return commentLikeService.toggleLike(commentId, userId);
    }

    @Override
    public PageResult<UserSimpleVO> listLikes(Long commentId, int page, int size) {
        return commentLikeService.listLikes(commentId, page, size);
    }

    // === 私有方法 ===

    private CommentVO convertToNewVO(Comment comment, Map<Long, Boolean> likedMap, Long currentUserId) {
        CommentVO vo = BeanCopyUtils.copy(comment, CommentVO.class);

        if (comment.getUserId() != null) {
            User user = userMapper.selectById(comment.getUserId());
            if (user != null) {
                vo.setAvatar(user.getAvatar());
            }
        }

        vo.setIsLiked(likedMap.getOrDefault(comment.getId(), false));

        // 查询回复数
        Integer replyCount = commentMapper.countReplies(comment.getId());
        vo.setReplyCount(replyCount != null ? replyCount : 0);

        // 加载前3条回复
        if (replyCount != null && replyCount > 0) {
            List<Comment> topReplies = commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                    .eq(Comment::getParentId, comment.getId())
                    .eq(Comment::getStatus, 1)
                    .orderByDesc(Comment::getLikeCount)
                    .last("LIMIT 3"));
            vo.setReplies(
                    topReplies.stream().map(r -> convertToReplyVO(r, likedMap)).collect(Collectors.toList()));
        }

        return vo;
    }

    private ReplyVO convertToReplyVO(Comment reply, Map<Long, Boolean> likedMap) {
        ReplyVO vo = new ReplyVO();
        vo.setId(reply.getId());
        vo.setUserId(reply.getUserId());
        vo.setContent(reply.getContent());
        vo.setLikeCount(reply.getLikeCount());
        vo.setCreateTime(reply.getCreateTime());
        vo.setReplyToUserId(reply.getReplyToUserId());

        if (reply.getUserId() != null) {
            User user = userMapper.selectById(reply.getUserId());
            if (user != null) {
                vo.setNickname(user.getNickname());
                vo.setAvatar(user.getAvatar());
            }
        }

        if (reply.getReplyToUserId() != null) {
            User replyToUser = userMapper.selectById(reply.getReplyToUserId());
            if (replyToUser != null) {
                vo.setReplyToNickname(replyToUser.getNickname());
            }
        }

        vo.setIsLiked(likedMap.getOrDefault(reply.getId(), false));
        return vo;
    }
}
