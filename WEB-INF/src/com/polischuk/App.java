package com.polischuk;

import com.polischuk.cache.CacheProvider;

import java.time.LocalDateTime;

/**
 * Demo for work process of the cache api (run and see console logs)
 */
public class App {

    public static void main(String[] args) {
        CacheProvider.setCacheTime(5);
        testCache();
        CacheProvider.setHolder(true);
        testCache();
    }

    static void testCache() {
        for (int i = 1; i < 11; i++) {
            CacheProvider.of(i, k -> LocalDateTime.now());
            try {
                Thread.sleep(1000 * i);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
