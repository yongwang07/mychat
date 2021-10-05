package com.mychat.cocurrent;

import com.mychat.util.ThreadUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
public class FutureTaskScheduler {
    static ThreadPoolExecutor mixPool = null;

    static {
        mixPool = ThreadUtil.getMixedTargetThreadPool();
    }

    private FutureTaskScheduler() { }

    public static void add(Runnable executeTask) {
        mixPool.submit(()->{ executeTask.run(); });
    }
}