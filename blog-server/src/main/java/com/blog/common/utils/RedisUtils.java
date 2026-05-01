package com.blog.common.utils;

import java.time.Duration;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBucket;
import org.redisson.api.RList;
import org.redisson.api.RMap;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

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
