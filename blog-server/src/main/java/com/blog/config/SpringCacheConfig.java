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

        return new RedissonSpringCacheManager(redissonClient, config);
    }
}
