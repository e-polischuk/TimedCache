package com.polischuk.cache;

import com.polischuk.cache.api.Cache;
import com.polischuk.cache.api.CacheCleaner;
import com.polischuk.cache.api.CacheHolder;

import java.time.LocalDateTime;
import java.util.function.Function;

/**
 * Use case example of the cache api. In real conditions can be used any type as a cached value (here it's LocalDateTime)
 * and any type which properly implements hashCode() and equals() to be used as key (here it's Integer).
 * Also expected the key is an input parameter of getUpdatedValue function - function getUpdatedValue has to represent
 * a method, which return the updated value by the input parameter - key.
 */
public class CacheProvider {
    private static volatile Cache<Integer, LocalDateTime> cache;
    private static volatile int cacheTime = 5;
    private static volatile boolean isCacheHolder;

    public static void setCacheTime(int seconds) {
        cacheTime = seconds;
    }

    public static void setHolder(boolean isHolder) {
        isCacheHolder = isHolder;
    }

    public static boolean isHolder() {
        return isCacheHolder;
    }

    public static LocalDateTime of(int key, Function<Integer, LocalDateTime> getUpdatedValue) {
        if (cacheTime < 1) {
            if (cache != null) {
                cache.stopCache();
                cache = null;
            }
            return getUpdatedValue.apply(key);
        } else {
            Class<? extends Cache> required = isCacheHolder ? CacheHolder.class : CacheCleaner.class;
            if (cache == null || cache.getClass() !=  required) {
                if (cache != null) cache.stopCache();
                cache = isCacheHolder ? CacheHolder.of(CacheProvider.class) : CacheCleaner.of(CacheProvider.class);
            }
            return cache.get(key, cacheTime, getUpdatedValue);
        }
    }

}
