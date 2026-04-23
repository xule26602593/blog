# 缓存系统优化设计 - Spring Cache + Redisson

## 背景

当前项目使用 `spring-boot-starter-data-redis` + Lettuce 客户端，通过自定义 `RedisUtils` 工具类手动管理缓存。存在以下问题：

1. 缓存代码冗余 - 每个 Service 方法都要手动调用 `get/set/delete`
2. 缺乏分布式锁 - 无法防止缓存击穿和并发更新问题
3. 缺乏高级功能 - 无法使用限流器、布隆过滤器等 Redisson 特性

## 优化目标

- 使用 Spring Cache 抽象简化缓存代码
- 引入 Redisson 获得分布式锁能力
- 保持 `RedisUtils` 用于复杂操作场景

---

## 一、依赖变更

### pom.xml

```xml
<!-- 新增 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson-spring-boot-starter</artifactId>
    <version>3.27.0</version>
</dependency>

<!-- 移除 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

### 版本说明

- Redisson 3.27.0 支持 Spring Boot 3.x
- Redisson 内置 Netty 连接池，无需额外配置连接池

---

## 二、配置变更

### application-dev.yml

```yaml
spring:
  data:
    redis:
      redisson:
        file: classpath:redisson-config.yml

  cache:
    type: redis
    redis:
      time-to-live: 300000  # 默认 5 分钟
      cache-null-values: false
```

### 新增 redisson-config.yml

放置在 `src/main/resources/` 目录下：

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

### application-prod.yml

生产环境使用集群模式：

```yaml
spring:
  data:
    redis:
      redisson:
        file: classpath:redisson-config-prod.yml
```

### 新增 redisson-config-prod.yml

```yaml
clusterServersConfig:
  nodeAddresses:
    - "redis://redis-node1:6379"
    - "redis://redis-node2:6379"
    - "redis://redis-node3:6379"
  masterConnectionPoolSize: 10
  slaveConnectionPoolSize: 10
  idleConnectionTimeout: 10000
  connectTimeout: 10000
  timeout: 3000
  retryAttempts: 3
  retryInterval: 1500
```

---

## 三、缓存配置类

### 新增 CacheConfig.java

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

### 缓存策略说明

| 缓存名称 | TTL | 失效策略 | 说明 |
|---------|-----|---------|------|
| category | 永久 | Admin 更新/删除时失效 | 分类列表变化频率低 |
| tag | 永久 | Admin 更新/删除时失效 | 标签列表变化频率低 |
| hotArticles | 5 分钟 | 自然过期 | 热门文章实时性要求低 |
| topArticles | 10 分钟 | 自然过期 | 置顶文章实时性要求低 |

---

## 四、分布式锁工具类

### 新增 DistributedLockUtils.java

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

---

## 五、Service 层改造

### CategoryServiceImpl 改造

```java
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final DistributedLockUtils lockUtils;

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
    @Transactional
    @CacheEvict(value = "category", key = "'list'")
    public void saveOrUpdate(CategoryDTO dto) {
        // 原有逻辑不变
    }

    @Override
    @Transactional
    @CacheEvict(value = "category", key = "'list'")
    public void delete(Long id) {
        // 原有逻辑不变
    }
}
```

### TagServiceImpl 改造

```java
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
    @Transactional
    @CacheEvict(value = "tag", key = "'list'")
    public void saveOrUpdate(TagDTO dto) {
        // 原有逻辑不变
    }

    @Override
    @Transactional
    @CacheEvict(value = "tag", key = "'list'")
    public void delete(Long id) {
        // 原有逻辑不变
    }
}
```

### ArticleServiceImpl 改造

热门文章和置顶文章使用分布式锁防止缓存击穿：

```java
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final ArticleTagMapper articleTagMapper;
    private final UserMapper userMapper;
    private final UserActionMapper userActionMapper;
    private final SeriesMapper seriesMapper;
    private final SeriesArticleMapper seriesArticleMapper;
    private final DistributedLockUtils lockUtils;

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

    @Override
    @Transactional
    public void saveOrUpdateArticle(ArticleDTO dto) {
        String lockKey = "lock:article:" + (dto.getId() != null ? dto.getId() : "new");
        lockUtils.executeWithLock(lockKey, () -> {
            // 原有保存逻辑
            doSaveOrUpdateArticle(dto);
        });
    }

    private void doSaveOrUpdateArticle(ArticleDTO dto) {
        // 将原有 saveOrUpdateArticle 方法体中的逻辑移到这里
        // 包括：创建/更新文章、设置发布时间、保存标签关联等
    }

    // convertToVOList 方法保持不变（已实现批量查询优化）
}
```

---

## 六、RedisUtils 改造

保留 `RedisUtils` 用于复杂操作，底层改为 Redisson：

```java
@Component
@RequiredArgsConstructor
public class RedisUtils {

    private final RedissonClient redissonClient;

    // String 操作
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

    // Hash 操作
    public void hSet(String key, String hashKey, Object value) {
        redissonClient.getMap(key).put(hashKey, value);
    }

    public Object hGet(String key, String hashKey) {
        return redissonClient.getMap(key).get(hashKey);
    }

    // ... 其他操作方法
}
```

---

## 七、缓存 Key 规范

| Key 模式 | 说明 | 示例 |
|---------|------|------|
| `category::list` | 分类列表 | Spring Cache 自动添加前缀 |
| `tag::list` | 标签列表 | Spring Cache 自动添加前缀 |
| `hotArticles::5` | 热门文章（参数 5） | Spring Cache 自动添加前缀 |
| `topArticles::default` | 置顶文章 | Spring Cache 自动添加前缀 |
| `lock:article:{id}` | 文章编辑锁 | 分布式锁 Key |
| `lock:cache:hotArticles` | 缓存重建锁 | 防止缓存击穿 |

---

## 八、改动文件清单

| 文件 | 操作 | 说明 |
|-----|------|------|
| `pom.xml` | 修改 | 添加/移除依赖 |
| `application-dev.yml` | 修改 | 添加 Redisson 配置 |
| `application-prod.yml` | 修改 | 添加 Redisson 集群配置 |
| `redisson-config.yml` | 新增 | Redisson 单机配置 |
| `redisson-config-prod.yml` | 新增 | Redisson 集群配置 |
| `CacheConfig.java` | 新增 | Spring Cache 配置 |
| `DistributedLockUtils.java` | 新增 | 分布式锁工具类 |
| `RedisUtils.java` | 修改 | 底层改为 Redisson |
| `CategoryServiceImpl.java` | 修改 | 使用 @Cacheable 注解 |
| `TagServiceImpl.java` | 修改 | 使用 @Cacheable 注解 |
| `ArticleServiceImpl.java` | 修改 | 使用 @Cacheable + 分布式锁 |

---

## 九、测试要点

- [ ] 分类/标签列表缓存正常读写
- [ ] Admin 更新分类/标签后缓存自动失效
- [ ] 热门文章/置顶文章缓存正常读写
- [ ] 文章编辑时分布式锁生效
- [ ] RedisUtils 复杂操作（Hash、List 等）正常
- [ ] 开发环境单机模式正常启动
- [ ] 生产环境集群配置正确（如适用）
