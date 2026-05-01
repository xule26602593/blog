package com.blog.common.config;

import java.util.HashMap;
import java.util.Map;
import org.redisson.api.RedissonClient;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class SpringCacheConfig {

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

        // 文章详情缓存 - 1 小时
        config.put("articleDetail", new CacheConfig(60 * 60 * 1000, 0));

        // 文章列表缓存 - 10 分钟
        config.put("articleList", new CacheConfig(10 * 60 * 1000, 0));

        // 系统配置缓存 - 永久存储，手动失效
        config.put("sysConfig", new CacheConfig(0, 0));

        // 敏感词库缓存 - 永久存储，手动失效
        config.put("sensitiveWords", new CacheConfig(0, 0));

        // 相关推荐缓存 - 10 分钟
        config.put("relatedArticles", new CacheConfig(10 * 60 * 1000, 0));

        return new RedissonSpringCacheManager(redissonClient, config);
    }
}
