package com.polischuk.cache.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class CacheHolder<K, V>  extends Cache<K, V> {
    private static final Map<Class, CacheHolder> HOLDERS = new ConcurrentHashMap<>();
    private final Thread holder;

    private CacheHolder(Class user, Map<K, Cache<K, V>> store, V value, int...time) {
        super(user, store, value, time);
        holder = time.length < 1 ? null : new Thread(() -> {
           String keyName = Thread.currentThread().getName();
           try {
               LOG.info("CacheHolder STARTED to hold -> " + keyName);
               Thread.sleep(sleepTimeOf(getTime()));
           } catch (InterruptedException e) {
               LOG.error("CacheHolder Key Reference error -> ", e);
           } finally {
               store.keySet().stream().filter(k -> k.toString().equals(keyName)).findFirst()
                       .ifPresent(k -> LOG.info(user.getName() + " -> Was removed " + k + " from " + store.remove(k)));
               LOG.info("CacheHolder FINISHED to hold -> " + keyName);
           }
        });
    }

    public synchronized static CacheHolder get(Class user) {
        return HOLDERS.computeIfAbsent(user, u -> new CacheHolder<>(u, new ConcurrentHashMap<>(), null));
    }

    @Override
    public synchronized V of(K key, int time, Function<K, V> valueFetcher) {
        setTime(time);
        CacheHolder<K, V> removed = (CacheHolder<K, V>) store.remove(key);
        if (removed != null && removed.holder != null && removed.holder.isAlive()) removed.holder.interrupt();
        V preVal = removed == null ? null : removed.getValue();
        V actualVal = preVal == null ? valueFetcher.apply(key) : preVal;
        CacheHolder<K, V> actual = new CacheHolder<>(user, store, actualVal, time);
        store.put(key, actual);
        actual.holder.setName(key.toString());
        actual.holder.setDaemon(true);
        actual.holder.start();
        return actualVal;
    }

    @Override
    public synchronized Cache<K, V> stopCache() {
        store.values().forEach(storedCache -> {
            try {
                CacheHolder<K, V> cache = (CacheHolder<K, V>) storedCache;
                if (cache != null && cache.holder != null && cache.holder.isAlive()) cache.holder.interrupt();
            } catch (Exception e) {
                LOG.error("Interrupt CacheThread error -> ", e);
            }
        });
        store.clear();
        return HOLDERS.remove(user);
    }

}
