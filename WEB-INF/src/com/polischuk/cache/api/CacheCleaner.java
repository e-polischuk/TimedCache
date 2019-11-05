package com.polischuk.cache.api;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class CacheCleaner<K, V> extends CacheAbstract<K, V> {
    private static final Map<Class, CacheCleaner> CLEANED = new ConcurrentHashMap<>();
    private static volatile Thread cleaner;

    private CacheCleaner(Class user, Map<K, Cache<K, V>> store, V value, int...time) {
        super(user, store, value, time);
    }

    public synchronized static CacheCleaner get(Class user) {
        return CLEANED.computeIfAbsent(user, u -> new CacheCleaner<>(u, new ConcurrentHashMap<>(), null));
    }

    @Override
    public synchronized V of(K key, int time, Function<K, V> getUpdatedValue) {
        CacheCleaner<K, V> removed = (CacheCleaner<K, V>) store.remove(key);
        V preVal = removed == null ? null : removed.getValue();
        V actualVal = preVal == null ? getUpdatedValue.apply(key) : preVal;
        store.put(key, new CacheCleaner<>(user, store, actualVal, time));
        setTime(getMinTimeOf(store));
        setCleaner();
        return actualVal;
    }

    @Override
    public synchronized void stopCache() {
        try {
            CLEANED.remove(user).store.clear();
            if (CLEANED.isEmpty()) {
                if (cleaner != null && cleaner.isAlive()) cleaner.interrupt();
                cleaner = null;
            }
        } catch (Exception e) {
            LOG.error("Stop DaemonCleaner error -> ", e);
        }
    }

    private synchronized void setCleaner() {
        if (cleaner == null || !cleaner.isAlive()) {
            cleaner = new Thread(() -> {
                LOG.info(">>> STARTED " + Thread.currentThread().getName());
                do try {
                    int minTime = getMinTimeOf(CLEANED);
                    LOG.info(">>> DaemonCacheCleaner fell asleep for " + minTime + " sec");
                    Thread.sleep(sleepTimeOf(minTime));
                    LOG.info(">>> DaemonCacheCleaner woke up after overslept " + minTime + " sec:");
                    LocalDateTime now = LocalDateTime.now();
                    CLEANED.forEach((u, cached) -> {
                        LOG.info(u.getName() + " -> Before clean StoreSize=" + cached.store.size() + " at " + now);
                        //noinspection unchecked
                        cached.store.forEach((k, v) -> {
                            CacheCleaner c = (CacheCleaner) v;
                            if (Duration.between(c.start, now).getSeconds() >= c.getTime() || c.getValue() == null) {
                                LOG.info(u.getName() + " -> Was removed " + k + " from " + cached.store.remove(k));
                            }
                        });
                        LOG.info(u.getName() + " -> After clean StoreSize=" + cached.store.size());
                    });
                } catch (Exception e) {
                    LOG.error("DaemonCacheCleaner error -> ", e);
                } while (CLEANED.values().stream().anyMatch(c -> !c.store.isEmpty()));
                LOG.info(">>> FINISHED " + Thread.currentThread().getName());
                cleaner = null;
            });
            cleaner.setName("CACHE_DAEMON_CLEANER");
            cleaner.setDaemon(true);
            cleaner.start();
        }
    }

    private synchronized int getMinTimeOf(Map caches) {
        //noinspection unchecked
        return caches.values().stream().mapToInt(c -> ((CacheCleaner)c).getTime()).min().orElse(0);
    }

}
