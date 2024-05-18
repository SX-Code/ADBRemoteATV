package com.swx.adbcontrol.utils;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class ScheduleUtil {
    private static final ScheduledExecutorService executorService =
            new ScheduledThreadPoolExecutor(1,
                    new BasicThreadFactory.Builder().namingPattern("tv-btn-schedule-pool-%d").daemon(true).build());

    public static ScheduledExecutorService getExecutorService() {
        return executorService;
    }
}
