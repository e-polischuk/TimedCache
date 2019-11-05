package com.polischuk.cache.api;

import java.util.function.Function;

public interface Cache<K, V> {

    V of(K key, int time, Function<K, V> getUpdatedValue);

    void stopCache();

}
