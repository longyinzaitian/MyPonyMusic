package com.music.app.utils;

import android.support.annotation.NonNull;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author .
 * @date 2019/3/18
 */
public class ThreadCenter {

    private ThreadCenter() {}

    private static ThreadCenter mInstance = new ThreadCenter();
    private ExecutorService mExecutorService;

    public static ThreadCenter getInstance() {
        return mInstance;
    }

    private void initExecutor() {
        if (mExecutorService == null) {
            mExecutorService = new ThreadPoolExecutor(1, 2, 3,
                    TimeUnit.SECONDS, new PriorityBlockingQueue<>(), new ThreadFactory() {
                @Override
                public Thread newThread(@NonNull Runnable r) {
                    return new Thread(r);
                }
            });
        }
    }


    public <T> Future<T> executeTask(Callable<T> task) {
        initExecutor();
        return mExecutorService.submit(task);
    }
}
