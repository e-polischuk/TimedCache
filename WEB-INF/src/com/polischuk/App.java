package com.polischuk;

import com.polischuk.cache.CacheProvider;

import java.time.LocalDateTime;

/**
 * Demo for work process of the cache api (run and see console logs)
 */
public class App {

    public static void main(String[] args) {
        try {
            for (int i = 5; i < 11; i = i + 5) {
                String cacheType = CacheProvider.isHolder() ? "HOLDER" : "CLEANER";
                System.out.println("======================== " + cacheType + " (time = " + i + " s) ========================");
                //CacheProvider.setCacheTime(i);
                test(10);
                System.out.println("=====>>>> " + cacheType + " is closed......");
                CacheProvider.setCacheTime(0);
                CacheProvider.of(0, k -> LocalDateTime.now());
                Thread.sleep(1500);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized static void test(int timeLimit) {
        for (int i = 1; i < 10; i++) {
            CacheProvider.setCacheTime((int) (timeLimit * Math.random() + 1));
            CacheProvider.of(i, k -> LocalDateTime.now());
            try {
                int sleepTime = 1000 * i * 3 / 4;
                System.out.println("=======> Test is sleeping " + sleepTime + " ms ......");
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        CacheProvider.setHolder(!CacheProvider.isHolder());
    }

}
