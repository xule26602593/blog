# 内容智能化系统 - 阶段三：智能推荐

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现基于用户画像和文章内容的智能推荐功能。

**Architecture:** 用户阅读画像 + 基于内容推荐 + Redis 缓存

**Prerequisites:**
- 完成阶段一：智能摘要 + 标签
- UserReadingProfile 实体和 Mapper 已创建
- Redis 已配置

---

## Task 3.1: 创建推荐服务

**Files:**
- Create: `blog-server/src/main/java/com/blog/service/ai/RecommendService.java`
- Create: `blog-server/src/main/java/com/blog/service/impl/ai/RecommendServiceImpl.java`

- [ ] **Step 1: 创建 RecommendService 接口**

```java
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
```

- [ ] **Step 2: 创建 RecommendServiceImpl**

```java
package com.blog.service.impl.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.blog.domain.entity.Article;
import com.blog.domain.entity.Tag;
import com.blog.domain.entity.UserReadingProfile;
import com.blog.repository.mapper.ArticleMapper;
import com.blog.repository.mapper.UserReadingProfileMapper;
import com.blog.service.ArticleService;
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
    
    private final ArticleService articleService;
    private final ArticleMapper articleMapper;
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
            return articleService.listByIds(cachedIds.stream().limit(limit).toList());
        }
        
        List<Article> recommendations = new ArrayList<>();
        
        // 基于内容的推荐
        if (articleId != null) {
            Article currentArticle = articleService.getById(articleId);
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
            Article article = articleService.getById(articleId);
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
            
            // 更新标签偏好
            Map<String, Double> tagWeights = parseWeights(profile.getPreferredTags());
            if (article.getTags() != null) {
                for (Tag tag : article.getTags()) {
                    tagWeights.merge(tag.getName(), 1.0, Double::sum);
                }
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
        // 基于分类和标签查找相似文章
        return articleMapper.selectList(
            new LambdaQueryWrapper<Article>()
                .eq(Article::getCategoryId, article.getCategoryId())
                .eq(Article::getStatus, 1)
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
                .orderByDesc(Article::getViewCount)
                .last("LIMIT " + limit)
        );
    }
    
    private List<Article> findHotArticles(int limit) {
        return articleMapper.selectList(
            new LambdaQueryWrapper<Article>()
                .eq(Article::getStatus, 1)
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
```

- [ ] **Step 3: 提交变更**

```bash
git add src/main/java/com/blog/service/
git commit -m "feat: add recommendation service"
```

---

## Task 3.2: 创建推荐 Controller 端点

**Files:**
- Modify: `blog-server/src/main/java/com/blog/controller/portal/ArticleController.java` (或创建新的)

- [ ] **Step 1: 在 Portal Controller 中添加推荐端点**

```java
// 在现有的 ArticlePortalController 或新建 AiPortalController 中添加

private final RecommendService recommendService;

/**
 * 获取推荐文章
 */
@GetMapping("/recommendations")
public Result<List<ArticleVO>> getRecommendations(
    @RequestParam(required = false) Long articleId,
    @RequestParam(defaultValue = "5") int limit
) {
    Long userId = getCurrentUserId(); // 从 SecurityContext 获取
    List<Article> articles = recommendService.getRecommendations(userId, articleId, limit);
    List<ArticleVO> vos = articles.stream()
        .map(this::convertToVO)
        .toList();
    return Result.success(vos);
}

/**
 * 记录阅读（更新用户画像）
 */
@PostMapping("/reading/{articleId}")
public Result<Void> recordReading(@PathVariable Long articleId) {
    Long userId = getCurrentUserId();
    if (userId != null) {
        recommendService.updateUserProfile(userId, articleId);
    }
    return Result.success();
}
```

- [ ] **Step 2: 提交变更**

```bash
git add src/main/java/com/blog/controller/
git commit -m "feat: add recommendation endpoints"
```

---

## Task 3.3: 前端集成推荐功能

**Files:**
- Modify: `blog-web/src/views/portal/ArticleDetail.vue`
- Modify: `blog-web/src/api/article.js`

- [ ] **Step 1: 添加推荐 API**

```javascript
// 在 api/article.js 中添加

/**
 * 获取推荐文章
 */
export function getRecommendations(articleId, limit = 5) {
  return request.get('/api/portal/articles/recommendations', {
    params: { articleId, limit }
  })
}

/**
 * 记录阅读
 */
export function recordReading(articleId) {
  return request.post(`/api/portal/articles/reading/${articleId}`)
}
```

- [ ] **Step 2: 在文章详情页添加推荐区域**

```vue
<!-- 在 ArticleDetail.vue 中添加 -->
<template>
  <!-- ... 现有内容 ... -->
  
  <!-- 推荐文章区域 -->
  <div v-if="recommendArticles.length > 0" class="recommend-section">
    <h3>推荐阅读</h3>
    <div class="recommend-list">
      <div
        v-for="article in recommendArticles"
        :key="article.id"
        class="recommend-item"
        @click="goToArticle(article.id)"
      >
        <h4>{{ article.title }}</h4>
        <p>{{ article.summary }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getRecommendations, recordReading } from '@/api/article'

const recommendArticles = ref([])

onMounted(async () => {
  // 记录阅读
  if (userStore.isLoggedIn) {
    recordReading(articleId)
  }
  
  // 获取推荐
  const res = await getRecommendations(articleId)
  recommendArticles.value = res.data
})

const goToArticle = (id) => {
  router.push(`/article/${id}`)
}
</script>

<style scoped>
.recommend-section {
  margin-top: 24px;
  padding: 16px;
  background: #f7f8fa;
  border-radius: 8px;
}

.recommend-section h3 {
  margin-bottom: 16px;
  font-size: 16px;
}

.recommend-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.recommend-item {
  padding: 12px;
  background: white;
  border-radius: 4px;
  cursor: pointer;
}

.recommend-item h4 {
  font-size: 14px;
  margin-bottom: 4px;
}

.recommend-item p {
  font-size: 12px;
  color: #666;
}
</style>
```

- [ ] **Step 3: 提交变更**

```bash
git add blog-web/src/
git commit -m "feat: integrate recommendation in article detail"
```

---

## 完成检查

- [ ] 推荐服务正常工作
- [ ] 用户画像更新正常
- [ ] Redis 缓存生效
- [ ] 前端推荐区域显示正常
- [ ] 阅读记录触发画像更新

## 下一步

完成本阶段后，继续执行 `2026-04-28-content-intelligence-phase4-chat-assistant.md`
