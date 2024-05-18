package com.swx.adbcontrol.utils;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author sxcode
 * @Date 2024/5/15 22:39
 */
public class ThreadPoolService {
    private static final ThreadFactory namedThreadFactory = new BasicThreadFactory.Builder().namingPattern("tv-btn-schedule-pool-%d").daemon(true).build();
    private static ExecutorService service = new ThreadPoolExecutor(
            2,
            4,
            0L,
            TimeUnit.MICROSECONDS,
            new LinkedBlockingDeque<>(10),
            namedThreadFactory,
            new ThreadPoolExecutor.AbortPolicy()
    );

    public static ExecutorService getEs()  {
        return service;
    }

    public static void newTask(Runnable r) {
        service.execute(r);
    }
}
