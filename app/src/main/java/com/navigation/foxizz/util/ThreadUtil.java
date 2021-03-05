package com.navigation.foxizz.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ThreadUtil {

    //线程池，最大容量
    public final static ExecutorService mExecutorService = Executors.newCachedThreadPool();

    /**
     * 执行线程
     *
     * @param runnable Runnable接口
     */
    public static void execute(Runnable runnable) {
        mExecutorService.execute(runnable);
    }

    /**
     * 执行线程
     *
     * @param task Callable接口
     */
    public static Future<?> submit(Callable<?> task) {
        return mExecutorService.submit(task);
    }

}
