package com.blog.service.ai;

import com.blog.domain.entity.Article;

import java.util.List;

public interface RecommendService {

    /**
     * 获取推荐文章
     */
    List<Article> getRecommendations(Long userId, Long articleId, int limit);

    /**
     * 更新用户阅读画像
     */
    void updateUserProfile(Long userId, Long articleId);
}
