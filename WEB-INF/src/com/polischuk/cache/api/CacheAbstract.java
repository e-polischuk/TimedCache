package com.polischuk.cache.api;

import com.polischuk.util.Logger;

import java.lang.ref.SoftReference;
import java.time.LocalDateTime;
import java.util.Map;

public abstract class CacheAbstract<K, V> implements Cache<K, V> {
    static final Logger LOG = Logger.getLogger(Cache.class);
    final LocalDateTime start = LocalDateTime.now();
    final Class user;
    final Map<K, Cache<K, V>> store;
    private final SoftReference<V> valueRef;
    private volatile int time;

    CacheAbstract(Class user, Map<K, Cache<K, V>> store, V value, int...time) {
        if (user == null || store == null) throw new IllegalStateException("Cache user or store can't be null!");
        this.user = user;
        this.store = store;
        this.valueRef = value == null ? null : new SoftReference<>(value);
        this.time = time.length < 1 ? 1 : time[0];
    }

    void setTime(int time) {
        this.time = time;
    }


    int getTime() {
        return time;
    }

    V getValue() {
        return valueRef == null ? null : valueRef.get();
    }

    int sleepTimeOf(int seconds) {
        return seconds < 1 ? 1000 : 1000 * seconds;
    }

    @Override
    public String toString() {
        V value = getValue();
        String type = value == null ? "Was cleaned by GC!" : value.getClass().getName();
        return "Cache{start=" + start + "; time=" + time + "s; valueType=" + type + "; keptKeys=" + store.keySet() + '}';
    }

}
