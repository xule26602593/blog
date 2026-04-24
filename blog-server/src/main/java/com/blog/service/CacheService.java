package com.blog.service;

import java.util.concurrent.TimeUnit;

public interface CacheService {

    void set(String key, Object value);

    void set(String key, Object value, long timeout, TimeUnit unit);

    <T> T get(String key, Class<T> type);

    void delete(String key);

    void deleteByPattern(String pattern);

    boolean hasKey(String key);

    boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit unit);

    void unlock(String key);
}
