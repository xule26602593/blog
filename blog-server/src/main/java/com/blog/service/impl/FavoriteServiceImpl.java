package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.common.result.PageResult;
import com.blog.domain.entity.Article;
import com.blog.domain.entity.UserAction;
import com.blog.domain.vo.FavoriteVO;
import com.blog.repository.mapper.ArticleMapper;
import com.blog.repository.mapper.UserActionMapper;
import com.blog.security.LoginUser;
import com.blog.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final UserActionMapper userActionMapper;
    private final ArticleMapper articleMapper;

    @Override
    public PageResult<FavoriteVO> getFavorites(int pageNum, int pageSize, String keyword) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return PageResult.of(List.of(), 0L, (long) pageSize, (long) pageNum);
        }

        // 查询用户的收藏记录（action_type = 2）
        LambdaQueryWrapper<UserAction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAction::getUserId, userId)
               .eq(UserAction::getActionType, 2)
               .orderByDesc(UserAction::getCreateTime);

        List<UserAction> allFavorites = userActionMapper.selectList(wrapper);

        // 过滤并构建VO
        List<FavoriteVO> allVOs = allFavorites.stream()
                .map(action -> {
                    Article article = articleMapper.selectById(action.getArticleId());
                    if (article == null || article.getDeleted() == 1 || article.getStatus() != 1) {
                        return null;
                    }
                    // 关键词过滤
                    if (StringUtils.hasText(keyword) && !article.getTitle().contains(keyword)) {
                        return null;
                    }
                    return buildFavoriteVO(action, article);
                })
                .filter(vo -> vo != null)
                .collect(Collectors.toList());

        // 分页
        long total = allVOs.size();
        int start = (pageNum - 1) * pageSize;
        int end = Math.min(start + pageSize, allVOs.size());
        List<FavoriteVO> records = start < allVOs.size() ? allVOs.subList(start, end) : List.of();

        return PageResult.of(records, total, (long) pageSize, (long) pageNum);
    }

    private FavoriteVO buildFavoriteVO(UserAction action, Article article) {
        FavoriteVO vo = new FavoriteVO();
        vo.setId(action.getId());
        vo.setArticleId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setSummary(article.getSummary());
        vo.setCoverImage(article.getCoverImage());
        vo.setViewCount(article.getViewCount());
        vo.setLikeCount(article.getLikeCount());
        vo.setFavoriteTime(action.getCreateTime().toString());
        return vo;
    }

    private Long getCurrentUserId() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser.getUserId();
        }
        return null;
    }
}
