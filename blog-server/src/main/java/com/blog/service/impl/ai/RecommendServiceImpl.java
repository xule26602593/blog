package com.blog.service.impl.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.domain.entity.Article;
import com.blog.domain.entity.ArticleTag;
import com.blog.domain.entity.Tag;
import com.blog.domain.entity.UserReadingProfile;
import com.blog.repository.mapper.ArticleMapper;
import com.blog.repository.mapper.ArticleTagMapper;
import com.blog.repository.mapper.TagMapper;
import com.blog.repository.mapper.UserReadingProfileMapper;
import com.blog.service.ai.RecommendService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendServiceImpl implements RecommendService {

    private final ArticleMapper articleMapper;
    private final ArticleTagMapper articleTagMapper;
    private final TagMapper tagMapper;
    private final UserReadingProfileMapper profileMapper;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public List<Article> getRecommendations(Long userId, Long articleId, int limit) {
        String cacheKey = "ai:recommend:" + userId;

        // 尝试从缓存获取
        @SuppressWarnings("unchecked")
        List<Long> cachedIds = (List<Long>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedIds != null && !cachedIds.isEmpty()) {
            return articleMapper.selectBatchIds(cachedIds.stream().limit(limit).toList());
        }

        List<Article> recommendations = new ArrayList<>();

        // 基于内容的推荐
        if (articleId != null) {
            Article currentArticle = articleMapper.selectById(articleId);
            if (currentArticle != null) {
                recommendations.addAll(findSimilarArticles(currentArticle, limit * 2));
            }
        }

        // 基于用户画像的推荐
        if (userId != null) {
            UserReadingProfile profile = profileMapper.selectOne(
                new LambdaQueryWrapper<UserReadingProfile>()
                    .eq(UserReadingProfile::getUserId, userId)
            );
            if (profile != null) {
                recommendations.addAll(findPreferredArticles(profile, limit));
            }
        }

        // 热门文章补充
        if (recommendations.size() < limit) {
            recommendations.addAll(findHotArticles(limit - recommendations.size()));
        }

        // 去重
        Set<Long> seen = new HashSet<>();
        List<Article> unique = new ArrayList<>();
        for (Article a : recommendations) {
            if (!seen.contains(a.getId()) && !a.getId().equals(articleId)) {
                seen.add(a.getId());
                unique.add(a);
                if (unique.size() >= limit) break;
            }
        }

        // 缓存结果
        List<Long> ids = unique.stream().map(Article::getId).toList();
        redisTemplate.opsForValue().set(cacheKey, ids, 10, TimeUnit.MINUTES);

        return unique;
    }

    @Override
    @Async
    public void updateUserProfile(Long userId, Long articleId) {
        try {
            Article article = articleMapper.selectById(articleId);
            if (article == null) return;

            UserReadingProfile profile = profileMapper.selectOne(
                new LambdaQueryWrapper<UserReadingProfile>()
                    .eq(UserReadingProfile::getUserId, userId)
            );

            if (profile == null) {
                profile = new UserReadingProfile();
                profile.setUserId(userId);
                profile.setPreferredTags("{}");
                profile.setPreferredCategories("{}");
                profileMapper.insert(profile);
            }

            // 获取文章标签
            List<Long> tagIds = articleTagMapper.selectTagIdsByArticleId(articleId);
            List<String> tagNames = new ArrayList<>();
            if (tagIds != null && !tagIds.isEmpty()) {
                List<Tag> tags = tagMapper.selectBatchIds(tagIds);
                tagNames = tags.stream().map(Tag::getName).toList();
            }

            // 更新标签偏好
            Map<String, Double> tagWeights = parseWeights(profile.getPreferredTags());
            for (String tagName : tagNames) {
                tagWeights.merge(tagName, 1.0, Double::sum);
            }
            profile.setPreferredTags(toJson(tagWeights));

            // 更新分类偏好
            Map<String, Double> categoryWeights = parseWeights(profile.getPreferredCategories());
            if (article.getCategoryName() != null) {
                categoryWeights.merge(article.getCategoryName(), 1.0, Double::sum);
            }
            profile.setPreferredCategories(toJson(categoryWeights));

            profileMapper.updateById(profile);

            // 清除推荐缓存
            redisTemplate.delete("ai:recommend:" + userId);
        } catch (Exception e) {
            log.error("更新用户画像失败", e);
        }
    }

    private List<Article> findSimilarArticles(Article article, int limit) {
        // 基于分类查找相似文章
        return articleMapper.selectList(
            new LambdaQueryWrapper<Article>()
                .eq(Article::getCategoryId, article.getCategoryId())
                .eq(Article::getStatus, 1)
                .eq(Article::getDeleted, 0)
                .ne(Article::getId, article.getId())
                .orderByDesc(Article::getViewCount)
                .last("LIMIT " + limit)
        );
    }

    private List<Article> findPreferredArticles(UserReadingProfile profile, int limit) {
        Map<String, Double> categoryWeights = parseWeights(profile.getPreferredCategories());
        if (categoryWeights.isEmpty()) return Collections.emptyList();

        // 找出权重最高的分类
        String topCategory = categoryWeights.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);

        if (topCategory == null) return Collections.emptyList();

        return articleMapper.selectList(
            new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, 1)
                .eq(Article::getDeleted, 0)
                .orderByDesc(Article::getViewCount)
                .last("LIMIT " + limit)
        );
    }

    private List<Article> findHotArticles(int limit) {
        return articleMapper.selectList(
            new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, 1)
                .eq(Article::getDeleted, 0)
                .orderByDesc(Article::getViewCount)
                .last("LIMIT " + limit)
        );
    }

    private Map<String, Double> parseWeights(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Double>>() {});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private String toJson(Map<String, Double> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            return "{}";
        }
    }
}
