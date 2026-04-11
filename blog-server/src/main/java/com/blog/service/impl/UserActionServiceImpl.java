package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.domain.entity.UserAction;
import com.blog.repository.mapper.ArticleMapper;
import com.blog.repository.mapper.UserActionMapper;
import com.blog.security.LoginUser;
import com.blog.service.UserActionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserActionServiceImpl implements UserActionService {

    private final UserActionMapper userActionMapper;
    private final ArticleMapper articleMapper;

    @Override
    @Transactional
    public boolean toggleLike(Long articleId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return false;
        }

        UserAction existing = userActionMapper.selectOne(new LambdaQueryWrapper<UserAction>()
                .eq(UserAction::getUserId, userId)
                .eq(UserAction::getArticleId, articleId)
                .eq(UserAction::getActionType, 1));

        if (existing != null) {
            // 取消点赞
            userActionMapper.deleteById(existing.getId());
            articleMapper.updateLikeCount(articleId, -1);
            return false;
        } else {
            // 添加点赞
            UserAction action = new UserAction();
            action.setUserId(userId);
            action.setArticleId(articleId);
            action.setActionType(1);
            userActionMapper.insert(action);
            articleMapper.updateLikeCount(articleId, 1);
            return true;
        }
    }

    @Override
    @Transactional
    public boolean toggleFavorite(Long articleId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return false;
        }

        UserAction existing = userActionMapper.selectOne(new LambdaQueryWrapper<UserAction>()
                .eq(UserAction::getUserId, userId)
                .eq(UserAction::getArticleId, articleId)
                .eq(UserAction::getActionType, 2));

        if (existing != null) {
            // 取消收藏
            userActionMapper.deleteById(existing.getId());
            return false;
        } else {
            // 添加收藏
            UserAction action = new UserAction();
            action.setUserId(userId);
            action.setArticleId(articleId);
            action.setActionType(2);
            userActionMapper.insert(action);
            return true;
        }
    }

    @Override
    public boolean checkLiked(Long articleId, Long userId) {
        if (userId == null) {
            return false;
        }
        Long count = userActionMapper.selectCount(new LambdaQueryWrapper<UserAction>()
                .eq(UserAction::getUserId, userId)
                .eq(UserAction::getArticleId, articleId)
                .eq(UserAction::getActionType, 1));
        return count > 0;
    }

    @Override
    public boolean checkFavorited(Long articleId, Long userId) {
        if (userId == null) {
            return false;
        }
        Long count = userActionMapper.selectCount(new LambdaQueryWrapper<UserAction>()
                .eq(UserAction::getUserId, userId)
                .eq(UserAction::getArticleId, articleId)
                .eq(UserAction::getActionType, 2));
        return count > 0;
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            return ((LoginUser) authentication.getPrincipal()).getUserId();
        }
        return null;
    }
}
