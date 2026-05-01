package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.exception.BusinessException;
import com.blog.common.result.ErrorCode;
import com.blog.common.utils.BeanCopyUtils;
import com.blog.common.utils.IpUtils;
import com.blog.domain.dto.CommentDTO;
import com.blog.domain.entity.Article;
import com.blog.domain.entity.Comment;
import com.blog.domain.entity.User;
import com.blog.domain.vo.CommentVO;
import com.blog.repository.mapper.ArticleMapper;
import com.blog.repository.mapper.CommentMapper;
import com.blog.repository.mapper.UserMapper;
import com.blog.security.LoginUser;
import com.blog.service.AchievementTriggerService;
import com.blog.service.CommentService;
import com.blog.service.SensitiveWordService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;
    private final SensitiveWordService sensitiveWordService;
    private final AchievementTriggerService achievementTriggerService;

    @Override
    public Page<CommentVO> pageComment(Long articleId, int pageNum, int pageSize) {
        // 查询顶级评论
        Page<Comment> page = new Page<>(pageNum, pageSize);
        Page<Comment> commentPage = commentMapper.selectPage(page,
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
        voPage.setRecords(commentPage.getRecords().stream().map(comment -> {
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
        }).collect(Collectors.toList()));
        
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
        return commentMapper.selectCount(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getStatus, 0));
    }
    
    private List<CommentVO> convertToVOList(List<Comment> comments, Long articleId) {
        if (comments.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 获取所有子评论
        List<Long> parentIds = comments.stream().map(Comment::getId).collect(Collectors.toList());
        List<Comment> children = commentMapper.selectList(
                new LambdaQueryWrapper<Comment>()
                        .eq(Comment::getArticleId, articleId)
                        .in(Comment::getParentId, parentIds)
                        .eq(Comment::getStatus, 1));
        
        Map<Long, List<Comment>> childrenMap = children.stream()
                .collect(Collectors.groupingBy(Comment::getParentId));
        
        return comments.stream().map(comment -> {
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
                vo.setChildren(childComments.stream().map(child -> {
                    CommentVO childVo = BeanCopyUtils.copy(child, CommentVO.class);
                    if (child.getUserId() != null) {
                        User user = userMapper.selectById(child.getUserId());
                        if (user != null) {
                            childVo.setAvatar(user.getAvatar());
                        }
                    }
                    return childVo;
                }).collect(Collectors.toList()));
            }
            
            return vo;
        }).collect(Collectors.toList());
    }
    
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            return ((LoginUser) authentication.getPrincipal()).getUserId();
        }
        return null;
    }
}
