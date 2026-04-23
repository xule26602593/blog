# Spring Cache + Redisson 缓存优化实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 使用 Spring Cache 抽象简化缓存代码，引入 Redisson 获得分布式锁能力

**Architecture:** 替换 Lettuce 为 Redisson 客户端，使用 @Cacheable 注解替代手动缓存管理，新增分布式锁工具类

**Tech Stack:** Spring Boot 3.2, Spring Cache, Redisson 3.27.0, MyBatis Plus

---

## 文件结构

**新增文件：**
- `blog-server/src/main/resources/redisson-config.yml` - Redisson 单机配置
- `blog-server/src/main/java/com/blog/config/CacheConfig.java` - Spring Cache 配置
- `blog-server/src/main/java/com/blog/common/utils/DistributedLockUtils.java` - 分布式锁工具类

**修改文件：**
- `blog-server/pom.xml` - 依赖变更
- `blog-server/src/main/resources/application-dev.yml` - Redis 配置调整
- `blog-server/src/main/java/com/blog/common/utils/RedisUtils.java` - 底层改为 Redisson
- `blog-server/src/main/java/com/blog/service/impl/CategoryServiceImpl.java` - 使用 @Cacheable
- `blog-server/src/main/java/com/blog/service/impl/TagServiceImpl.java` - 使用 @Cacheable
- `blog-server/src/main/java/com/blog/service/impl/ArticleServiceImpl.java` - 使用 @Cacheable + 分布式锁

---

## Task 1: 修改 Maven 依赖

**Files:**
- Modify: `blog-server/pom.xml`

- [ ] **Step 1: 添加新依赖，移除旧依赖**

在 `pom.xml` 的 `<dependencies>` 中：

1. 在 `spring-boot-starter-aop` 依赖后添加：
```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-cache</artifactId>
        </dependency>
        <dependency>
            <groupId>org.redisson</groupId>
            <artifactId>redisson-spring-boot-starter</artifactId>
            <version>3.27.0</version>
        </dependency>
```

2. 删除 `spring-boot-starter-data-redis` 依赖：
```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
```

- [ ] **Step 2: 编译验证**

Run: `cd blog-server && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add blog-server/pom.xml
git commit -m "build: replace lettuce with redisson, add spring-cache"
```

---

## Task 2: 创建 Redisson 配置文件

**Files:**
- Create: `blog-server/src/main/resources/redisson-config.yml`

- [ ] **Step 1: 创建 Redisson 单机配置文件**

在 `blog-server/src/main/resources/` 目录下创建 `redisson-config.yml`：

```yaml
singleServerConfig:
  address: "redis://localhost:6379"
  database: 0
  connectionPoolSize: 10
  connectionMinimumIdleSize: 2
  idleConnectionTimeout: 10000
  connectTimeout: 10000
  timeout: 3000
  retryAttempts: 3
  retryInterval: 1500
```

- [ ] **Step 2: 提交**

```bash
git add blog-server/src/main/resources/redisson-config.yml
git commit -m "config: add redisson single server config"
```

---

## Task 3: 修改 application-dev.yml

**Files:**
- Modify: `blog-server/src/main/resources/application-dev.yml`

- [ ] **Step 1: 替换 Redis 配置为 Redisson 配置**

将 `application-dev.yml` 中的 Redis 配置：

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password:
      database: 0
      timeout: 10000ms
      lettuce:
        pool:
          max-active: 8
          max-wait: -1ms
          max-idle: 8
          min-idle: 0
```

替换为：

```yaml
spring:
  data:
    redis:
      redisson:
        file: classpath:redisson-config.yml
  cache:
    type: redis
    redis:
      time-to-live: 300000
      cache-null-values: false
```

- [ ] **Step 2: 编译验证**

Run: `cd blog-server && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add blog-server/src/main/resources/application-dev.yml
git commit -m "config: switch to redisson config in application-dev"
```

---

## Task 4: 创建 Spring Cache 配置类

**Files:**
- Create: `blog-server/src/main/java/com/blog/config/CacheConfig.java`

- [ ] **Step 1: 创建 CacheConfig.java**

在 `blog-server/src/main/java/com/blog/config/` 目录下创建 `CacheConfig.java`：

```java
package com.blog.config;

import org.redisson.api.RedissonClient;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(RedissonClient redissonClient) {
        Map<String, CacheConfig> config = new HashMap<>();

        // 分类列表缓存 - 永久存储，手动失效
        config.put("category", new CacheConfig(0, 0));

        // 标签列表缓存 - 永久存储，手动失效
        config.put("tag", new CacheConfig(0, 0));

        // 热门文章缓存 - 5 分钟
        config.put("hotArticles", new CacheConfig(5 * 60 * 1000, 0));

        // 置顶文章缓存 - 10 分钟
        config.put("topArticles", new CacheConfig(10 * 60 * 1000, 0));

        return new RedissonSpringCacheManager(redissonClient, config);
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `cd blog-server && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add blog-server/src/main/java/com/blog/config/CacheConfig.java
git commit -m "feat(cache): add Spring Cache config with Redisson"
```

---

## Task 5: 创建分布式锁工具类

**Files:**
- Create: `blog-server/src/main/java/com/blog/common/utils/DistributedLockUtils.java`

- [ ] **Step 1: 创建 DistributedLockUtils.java**

在 `blog-server/src/main/java/com/blog/common/utils/` 目录下创建 `DistributedLockUtils.java`：

```java
package com.blog.common.utils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLockUtils {

    private final RedissonClient redissonClient;

    /**
     * 加锁执行任务（无返回值）
     */
    public void executeWithLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit, Runnable task) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean acquired = lock.tryLock(waitTime, leaseTime, unit);
            if (!acquired) {
                log.warn("获取锁失败: {}", lockKey);
                throw new RuntimeException("系统繁忙，请稍后重试");
            }
            task.run();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("获取锁被中断", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 加锁执行任务（有返回值）
     */
    public <T> T executeWithLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit, Supplier<T> task) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean acquired = lock.tryLock(waitTime, leaseTime, unit);
            if (!acquired) {
                log.warn("获取锁失败: {}", lockKey);
                throw new RuntimeException("系统繁忙，请稍后重试");
            }
            return task.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("获取锁被中断", e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 加锁执行任务（使用默认参数）
     * 默认等待 3 秒，持有 30 秒
     */
    public <T> T executeWithLock(String lockKey, Supplier<T> task) {
        return executeWithLock(lockKey, 3, 30, TimeUnit.SECONDS, task);
    }

    /**
     * 加锁执行任务（使用默认参数）
     * 默认等待 3 秒，持有 30 秒
     */
    public void executeWithLock(String lockKey, Runnable task) {
        executeWithLock(lockKey, 3, 30, TimeUnit.SECONDS, task);
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `cd blog-server && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add blog-server/src/main/java/com/blog/common/utils/DistributedLockUtils.java
git commit -m "feat(lock): add distributed lock utility with Redisson"
```

---

## Task 6: 改造 RedisUtils 底层实现

**Files:**
- Modify: `blog-server/src/main/java/com/blog/common/utils/RedisUtils.java`

- [ ] **Step 1: 替换 RedisUtils 实现为 Redisson**

将 `RedisUtils.java` 的全部内容替换为：

```java
package com.blog.common.utils;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RList;
import org.redisson.api.RMap;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class RedisUtils {

    private final RedissonClient redissonClient;

    // ========== String ==========

    public void set(String key, Object value) {
        redissonClient.getBucket(key).set(value);
    }

    public void set(String key, Object value, long seconds) {
        redissonClient.getBucket(key).set(value, Duration.ofSeconds(seconds));
    }

    public Object get(String key) {
        return redissonClient.getBucket(key).get();
    }

    public boolean delete(String key) {
        return redissonClient.getBucket(key).delete();
    }

    public long delete(Collection<String> keys) {
        long count = 0;
        for (String key : keys) {
            if (redissonClient.getBucket(key).delete()) {
                count++;
            }
        }
        return count;
    }

    public boolean hasKey(String key) {
        return redissonClient.getBucket(key).isExists();
    }

    public boolean expire(String key, long seconds) {
        RBucket<Object> bucket = redissonClient.getBucket(key);
        if (bucket.isExists()) {
            bucket.set(bucket.get(), Duration.ofSeconds(seconds));
            return true;
        }
        return false;
    }

    public long getExpire(String key) {
        long remainTime = redissonClient.getBucket(key).remainTimeToLive();
        return remainTime > 0 ? remainTime / 1000 : -2;
    }

    public long increment(String key, long delta) {
        return redissonClient.getAtomicLong(key).addAndGet(delta);
    }

    public long decrement(String key, long delta) {
        return redissonClient.getAtomicLong(key).addAndGet(-delta);
    }

    // ========== Hash ==========

    public void hSet(String key, String hashKey, Object value) {
        redissonClient.getMap(key).put(hashKey, value);
    }

    public Object hGet(String key, String hashKey) {
        return redissonClient.getMap(key).get(hashKey);
    }

    public void hDelete(String key, Object... hashKeys) {
        redissonClient.getMap(key).fastRemove(hashKeys);
    }

    public boolean hHasKey(String key, String hashKey) {
        return redissonClient.getMap(key).containsKey(hashKey);
    }

    public long hIncrement(String key, String hashKey, long delta) {
        RMap<Object, Object> map = redissonClient.getMap(key);
        Object value = map.get(hashKey);
        long newValue = (value != null ? ((Number) value).longValue() : 0) + delta;
        map.put(hashKey, newValue);
        return newValue;
    }

    // ========== Set ==========

    public long sAdd(String key, Object... values) {
        RSet<Object> set = redissonClient.getSet(key);
        long count = 0;
        for (Object value : values) {
            if (set.add(value)) {
                count++;
            }
        }
        return count;
    }

    public long sRemove(String key, Object... values) {
        RSet<Object> set = redissonClient.getSet(key);
        long count = 0;
        for (Object value : values) {
            if (set.remove(value)) {
                count++;
            }
        }
        return count;
    }

    public long sSize(String key) {
        return redissonClient.getSet(key).size();
    }

    // ========== List ==========

    public long lPush(String key, Object value) {
        redissonClient.getList(key).add(0, value);
        return redissonClient.getList(key).size();
    }

    public long rPush(String key, Object value) {
        redissonClient.getList(key).add(value);
        return redissonClient.getList(key).size();
    }

    public Object lPop(String key) {
        RList<Object> list = redissonClient.getList(key);
        if (list.isEmpty()) {
            return null;
        }
        return list.remove(0);
    }

    public Object rPop(String key) {
        RList<Object> list = redissonClient.getList(key);
        if (list.isEmpty()) {
            return null;
        }
        return list.remove(list.size() - 1);
    }

    public long lSize(String key) {
        return redissonClient.getList(key).size();
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `cd blog-server && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add blog-server/src/main/java/com/blog/common/utils/RedisUtils.java
git commit -m "refactor(redis): migrate RedisUtils from RedisTemplate to Redisson"
```

---

## Task 7: 改造 CategoryServiceImpl 使用 @Cacheable

**Files:**
- Modify: `blog-server/src/main/java/com/blog/service/impl/CategoryServiceImpl.java`

- [ ] **Step 1: 添加导入和修改类实现**

将 `CategoryServiceImpl.java` 的全部内容替换为：

```java
package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.exception.BusinessException;
import com.blog.common.result.ErrorCode;
import com.blog.common.utils.BeanCopyUtils;
import com.blog.domain.dto.CategoryDTO;
import com.blog.domain.entity.Category;
import com.blog.domain.vo.CategoryVO;
import com.blog.repository.mapper.CategoryMapper;
import com.blog.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    @Cacheable(value = "category", key = "'list'")
    public List<CategoryVO> listAll() {
        List<Category> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getStatus, 1)
                        .orderByAsc(Category::getSort));

        return categories.stream().map(category -> {
            CategoryVO vo = BeanCopyUtils.copy(category, CategoryVO.class);
            vo.setArticleCount(categoryMapper.countArticles(category.getId()));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public Page<CategoryVO> pageCategory(int pageNum, int pageSize) {
        Page<Category> page = new Page<>(pageNum, pageSize);
        Page<Category> categoryPage = categoryMapper.selectPage(page,
                new LambdaQueryWrapper<Category>().orderByAsc(Category::getSort));

        Page<CategoryVO> voPage = new Page<>(pageNum, pageSize, categoryPage.getTotal());
        voPage.setRecords(categoryPage.getRecords().stream().map(category -> {
            CategoryVO vo = BeanCopyUtils.copy(category, CategoryVO.class);
            vo.setArticleCount(categoryMapper.countArticles(category.getId()));
            return vo;
        }).collect(Collectors.toList()));

        return voPage;
    }

    @Override
    public CategoryVO getById(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        CategoryVO vo = BeanCopyUtils.copy(category, CategoryVO.class);
        vo.setArticleCount(categoryMapper.countArticles(id));
        return vo;
    }

    @Override
    @Transactional
    @CacheEvict(value = "category", key = "'list'")
    public void saveOrUpdate(CategoryDTO dto) {
        Category category;
        if (dto.getId() == null) {
            category = new Category();
        } else {
            category = categoryMapper.selectById(dto.getId());
            if (category == null) {
                throw new BusinessException(ErrorCode.CATEGORY_NOT_FOUND);
            }
        }

        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setSort(dto.getSort() != null ? dto.getSort() : 0);
        category.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);

        if (dto.getId() == null) {
            categoryMapper.insert(category);
        } else {
            categoryMapper.updateById(category);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "category", key = "'list'")
    public void delete(Long id) {
        Integer count = categoryMapper.countArticles(id);
        if (count > 0) {
            throw new BusinessException("该分类下存在文章，无法删除");
        }
        categoryMapper.deleteById(id);
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `cd blog-server && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add blog-server/src/main/java/com/blog/service/impl/CategoryServiceImpl.java
git commit -m "refactor(category): use @Cacheable instead of manual cache management"
```

---

## Task 8: 改造 TagServiceImpl 使用 @Cacheable

**Files:**
- Modify: `blog-server/src/main/java/com/blog/service/impl/TagServiceImpl.java`

- [ ] **Step 1: 添加导入和修改类实现**

将 `TagServiceImpl.java` 的全部内容替换为：

```java
package com.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.common.exception.BusinessException;
import com.blog.common.result.ErrorCode;
import com.blog.common.utils.BeanCopyUtils;
import com.blog.domain.dto.TagDTO;
import com.blog.domain.entity.ArticleTag;
import com.blog.domain.entity.Tag;
import com.blog.domain.vo.TagVO;
import com.blog.repository.mapper.ArticleTagMapper;
import com.blog.repository.mapper.TagMapper;
import com.blog.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagMapper tagMapper;
    private final ArticleTagMapper articleTagMapper;

    @Override
    @Cacheable(value = "tag", key = "'list'")
    public List<TagVO> listAll() {
        List<Tag> tags = tagMapper.selectList(null);
        return tags.stream()
                .map(tag -> BeanCopyUtils.copy(tag, TagVO.class))
                .collect(Collectors.toList());
    }

    @Override
    public Page<TagVO> pageTag(int pageNum, int pageSize) {
        Page<Tag> page = new Page<>(pageNum, pageSize);
        Page<Tag> tagPage = tagMapper.selectPage(page, null);
        
        Page<TagVO> voPage = new Page<>(pageNum, pageSize, tagPage.getTotal());
        voPage.setRecords(tagPage.getRecords().stream()
                .map(tag -> BeanCopyUtils.copy(tag, TagVO.class))
                .collect(Collectors.toList()));
        
        return voPage;
    }

    @Override
    @Transactional
    @CacheEvict(value = "tag", key = "'list'")
    public void saveOrUpdate(TagDTO dto) {
        Tag tag;
        if (dto.getId() == null) {
            tag = new Tag();
            // 检查标签名是否已存在
            Long count = tagMapper.selectCount(new LambdaQueryWrapper<Tag>()
                    .eq(Tag::getName, dto.getName()));
            if (count > 0) {
                throw new BusinessException("标签名称已存在");
            }
        } else {
            tag = tagMapper.selectById(dto.getId());
            if (tag == null) {
                throw new BusinessException(ErrorCode.TAG_NOT_FOUND);
            }
        }
        
        tag.setName(dto.getName());
        tag.setColor(dto.getColor() != null ? dto.getColor() : "#409EFF");
        
        if (dto.getId() == null) {
            tagMapper.insert(tag);
        } else {
            tagMapper.updateById(tag);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "tag", key = "'list'")
    public void delete(Long id) {
        // 删除标签与文章的关联
        articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>()
                .eq(ArticleTag::getTagId, id));
        tagMapper.deleteById(id);
    }
}
```

- [ ] **Step 2: 编译验证**

Run: `cd blog-server && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: 提交**

```bash
git add blog-server/src/main/java/com/blog/service/impl/TagServiceImpl.java
git commit -m "refactor(tag): use @Cacheable instead of manual cache management"
```

---

## Task 9: 改造 ArticleServiceImpl 使用 @Cacheable + 分布式锁

**Files:**
- Modify: `blog-server/src/main/java/com/blog/service/impl/ArticleServiceImpl.java`

- [ ] **Step 1: 添加导入**

在文件顶部的导入区域，删除 `RedisUtils` 导入，添加以下导入：

```java
import com.blog.common.utils.DistributedLockUtils;
import org.springframework.cache.annotation.Cacheable;
```

- [ ] **Step 2: 修改字段声明**

将字段声明中的：
```java
    private final RedisUtils redisUtils;

    private static final String CACHE_KEY_HOT_ARTICLES = "blog:article:hot:";
    private static final String CACHE_KEY_TOP_ARTICLES = "blog:article:top";
```

替换为：
```java
    private final DistributedLockUtils lockUtils;
```

- [ ] **Step 3: 修改 getHotArticles 方法**

将 `getHotArticles` 方法替换为：

```java
    @Override
    @Cacheable(value = "hotArticles", key = "#limit")
    public List<ArticleListVO> getHotArticles(int limit) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getDeleted, 0)
               .eq(Article::getStatus, 1)
               .orderByDesc(Article::getViewCount)
               .last("LIMIT " + limit);

        List<Article> articles = articleMapper.selectList(wrapper);
        return convertToVOList(articles);
    }
```

- [ ] **Step 4: 修改 getTopArticles 方法**

将 `getTopArticles` 方法替换为：

```java
    @Override
    @Cacheable(value = "topArticles", key = "'default'")
    public List<ArticleListVO> getTopArticles() {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getDeleted, 0)
               .eq(Article::getStatus, 1)
               .eq(Article::getIsTop, 1)
               .orderByDesc(Article::getPublishTime);

        List<Article> articles = articleMapper.selectList(wrapper);
        return convertToVOList(articles);
    }
```

- [ ] **Step 5: 修改 saveOrUpdateArticle 方法添加分布式锁**

将 `saveOrUpdateArticle` 方法替换为：

```java
    @Override
    @Transactional
    public void saveOrUpdateArticle(ArticleDTO dto) {
        String lockKey = "lock:article:" + (dto.getId() != null ? dto.getId() : "new");
        lockUtils.executeWithLock(lockKey, () -> doSaveOrUpdateArticle(dto));
    }

    private void doSaveOrUpdateArticle(ArticleDTO dto) {
        Article article;
        
        if (dto.getId() == null) {
            article = new Article();
            article.setViewCount(0L);
            article.setLikeCount(0L);
            article.setCommentCount(0);
            article.setAuthorId(getCurrentUserId());
        } else {
            article = articleMapper.selectById(dto.getId());
            if (article == null) {
                throw new BusinessException(ErrorCode.ARTICLE_NOT_FOUND);
            }
        }
        
        article.setTitle(dto.getTitle());
        article.setSummary(dto.getSummary());
        article.setContent(dto.getContent());
        article.setCoverImage(dto.getCoverImage());
        article.setCategoryId(dto.getCategoryId());
        article.setIsTop(dto.getIsTop() != null ? dto.getIsTop() : 0);
        article.setStatus(dto.getStatus() != null ? dto.getStatus() : 0);
        
        // 发布时设置发布时间
        if (article.getStatus() == 1 && article.getPublishTime() == null) {
            article.setPublishTime(LocalDateTime.now());
        }
        
        if (dto.getId() == null) {
            articleMapper.insert(article);
        } else {
            articleMapper.updateById(article);
            // 删除旧的标签关联
            articleTagMapper.delete(new LambdaQueryWrapper<ArticleTag>()
                    .eq(ArticleTag::getArticleId, article.getId()));
        }
        
        // 保存标签关联
        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            for (Long tagId : dto.getTagIds()) {
                ArticleTag articleTag = new ArticleTag();
                articleTag.setArticleId(article.getId());
                articleTag.setTagId(tagId);
                articleTagMapper.insert(articleTag);
            }
        }
    }
```

- [ ] **Step 6: 编译验证**

Run: `cd blog-server && mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 7: 提交**

```bash
git add blog-server/src/main/java/com/blog/service/impl/ArticleServiceImpl.java
git commit -m "refactor(article): use @Cacheable and distributed lock for article operations"
```

---

## Task 10: 验证和测试

**Files:**
- 无文件修改，仅测试验证

- [ ] **Step 1: 启动服务**

确保 Docker 环境运行：

```bash
make up
# 或
docker-compose up -d
```

Run: `make up`
Expected: 所有服务正常启动

- [ ] **Step 2: 验证缓存效果**

访问以下接口验证功能正常：
- GET `http://localhost:8080/api/portal/categories` - 分类列表
- GET `http://localhost:8080/api/portal/tags` - 标签列表
- GET `http://localhost:8080/api/portal/articles/hot` - 热门文章
- GET `http://localhost:8080/api/portal/articles/top` - 置顶文章

- [ ] **Step 3: 检查 Redis 缓存**

连接 Redis 检查缓存 key：

```bash
docker exec -it blog-redis redis-cli
> KEYS *
```

Expected: 看到以下 keys：
- `category::list`
- `tag::list`
- `hotArticles::5`（或其他 limit 值）
- `topArticles::default`

- [ ] **Step 4: 最终提交**

如果所有测试通过，推送代码：

```bash
git push origin master
```

---

## 测试要点

- [ ] 分类列表缓存正常读写，Admin 更新后缓存自动失效
- [ ] 标签列表缓存正常读写，Admin 更新后缓存自动失效
- [ ] 热门文章缓存正常读写，5 分钟后自动过期
- [ ] 置顶文章缓存正常读写，10 分钟后自动过期
- [ ] 文章编辑时分布式锁生效
- [ ] RedisUtils 复杂操作（Hash、List 等）正常
- [ ] 服务启动无报错
