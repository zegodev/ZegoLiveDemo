package com.zego.zegolivedemo.utils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by Mark on 2016/3/15
 *
 * Des: 线程池工具类.
 */
public class ExecutorUtil {

    public static ExecutorUtil sInstance;

    private Executor mExecutor;

    private ExecutorUtil(){
        mExecutor = Executors.newFixedThreadPool(5);
    }

    public static ExecutorUtil getInstance(){
        if(sInstance == null){
            synchronized (ExecutorUtil.class){
                if(sInstance == null){
                    sInstance = new ExecutorUtil();
                }
            }
        }

        return sInstance;
    }

    public Executor getExecutor() {
        return mExecutor;
    }
}
