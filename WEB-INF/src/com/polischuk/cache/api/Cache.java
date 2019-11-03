package com.polischuk.cache.api;

import com.polischuk.util.Logger;

import java.lang.ref.SoftReference;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Function;

public abstract class Cache<K, V> {
    static final Logger LOG = Logger.getLogger(Cache.class);
    final LocalDateTime start = LocalDateTime.now();
    final Class user;
    final Map<K, Cache<K, V>> store;
    private final SoftReference<V> valueRef;
    private volatile int time;

    Cache(Class user, Map<K, Cache<K, V>> store, V value, int...time) {
        if (user == null || store == null) throw new IllegalStateException("Cache user or store can't be null!");
        this.user = user;
        this.store = store;
        this.valueRef = value == null ? null : new SoftReference<>(value);
        this.time = time.length > 0 ? time[0] : 5;
    }


    public abstract V of(K key, int time, Function<K, V> valueFetcher);

    public abstract Cache<K, V> stopCache();

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
        return 1000 * seconds; //1000 * Math.max(5, Math.min(seconds, 3600));
    }

    @Override
    public String toString() {
        V value = getValue();
        String type = value == null ? "Was cleaned by GC!" : value.getClass().getName();
        return "Cache{start=" + start + "; valueType=" + type + "; time=" + time + "s; keptKeys=" + store.keySet() + '}';
    }

}
