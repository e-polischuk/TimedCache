package com.polischuk.util;

import java.time.LocalDateTime;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * In real project has to be something like org.apache.log4j.Logger, but for demonstration needs is used this imitation
 * to print in console work process of the cache api.
 */
public class Logger {
    private static final Map<Class, Logger> LOGGER_MAP = new IdentityHashMap<>();
    private final Class user;

    private Logger(Class user) {
        this.user = user;
    }

    public static Logger getLogger(Class user) {
        return LOGGER_MAP.computeIfAbsent(user, Logger::new);
    }

    public void info(String infoMsg) {
        printMsg("INFO", infoMsg);
    }

    public void warn(String warnMsg) {
        printMsg("WARN", warnMsg);
    }

    public void error(String errorMsg) {
        printMsg("ERROR", errorMsg);
    }

    public void error(String errorMsg, Throwable throwable) {
        error(errorMsg);
        System.out.println(throwable.getClass().getName() + ": " + throwable.getMessage());
        for (StackTraceElement e : throwable.getStackTrace()) System.out.println("   > " + e);
    }

    private void printMsg(String type, String msg) {
        System.out.println(type + " " + LocalDateTime.now() + " " + user.getName() + ": " + msg);
    }

}
