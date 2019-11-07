package com.polischuk.cache.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class CacheHolder<K, V>  extends CacheAbstract<K, V> {
    private static final Map<Class, CacheHolder> HOLDERS = new ConcurrentHashMap<>();
    private final Thread holder;

    private CacheHolder(Class user, Map<K, Cache<K, V>> store, V value, int...time) {
        super(user, store, value, time);
        holder = time.length < 1 ? null : new Thread(() -> {
           String keyName = Thread.currentThread().getName();
           try {
               LOG.info("CacheHolder STARTED to hold -> " + keyName);
               pauseFor(this::getTime);
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
    public synchronized V of(K key, int time, Function<K, V> getUpdatedValue) {
        if (time < 1) return getUpdatedValue.apply(key);
        setTime(time);
        CacheHolder<K, V> current = (CacheHolder<K, V>) store.get(key);
        V currentVal = current == null ? null : current.getValue();
        if (currentVal == null) {
            currentVal = getUpdatedValue.apply(key);
            CacheHolder<K, V> actual = new CacheHolder<>(user, store, currentVal, time);
            store.put(key, actual);
            actual.holder.setName(key.toString());
            actual.holder.setDaemon(true);
            actual.holder.start();
        } else current.setTime(time);
        return currentVal;
    }

    @Override
    public synchronized void stopCache() {
        store.values().forEach(storedCache -> {
            try {
                CacheHolder<K, V> cache = (CacheHolder<K, V>) storedCache;
                if (cache != null && cache.holder != null && cache.holder.isAlive()) cache.holder.interrupt();
            } catch (Exception e) {
                LOG.error("Interrupt CacheThread error -> ", e);
            }
        });
        HOLDERS.remove(user).store.clear();
    }

}
