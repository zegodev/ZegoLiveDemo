package com.zego.livedemo3.performance;

import android.os.Debug;
import android.util.Log;

/**
 * Copyright © 2016 Zego. All rights reserved.
 */
public class MemoryTest {

    private static MemoryTest sInstance;

    private boolean mIsTaskRuning = false;

    private Runnable mTask;

    private long mSamplingCount;

    private long mNativeHeapAllocatedTotal;

    private long mDalvikHeapAllocatedTotal;

    private MemoryTest() {
        mTask = new Runnable() {
            @Override
            public void run() {
                while (mIsTaskRuning) {
                    try {
                        Thread.sleep(1000);
                        samplingProcessMemory();
                        mSamplingCount += 1;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    }

    public static MemoryTest getInstance() {
        if(sInstance == null){
            synchronized (MemoryTest.class){
                if(sInstance == null){
                    sInstance = new MemoryTest();
                }
            }
        }
        return sInstance;
    }

    public void start(){
        synchronized (sInstance){
            if(!mIsTaskRuning){
                mIsTaskRuning = true;
                mSamplingCount = 0;
                mNativeHeapAllocatedTotal = 0;
                mDalvikHeapAllocatedTotal = 0;
                new Thread(mTask).start();
            }
        }
    }

    public void stop(){
        synchronized (sInstance){
            mIsTaskRuning = false;
        }
    }

    public long getAverDalvikHeapAllocated(){
        return mDalvikHeapAllocatedTotal / mSamplingCount;
    }

    public long getAverNativeHeapAllocated(){
        return mNativeHeapAllocatedTotal / mSamplingCount;
    }

    private void samplingProcessMemory() {
        Log.i(PerformanceTest.TAG, "DalvikHeapTotal:" + Runtime.getRuntime().totalMemory() / 1024 / 1024 + "MB");
        long dalvikAllocated = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024 - 37;
        mDalvikHeapAllocatedTotal += dalvikAllocated;
        Log.i(PerformanceTest.TAG, "DalvikHeapAllocated:" + dalvikAllocated + "MB");

        Log.i(PerformanceTest.TAG, "NativeHeapTotal: " + (Debug.getNativeHeapSize() / 1024 / 1024) + "MB");
        long nativeAllocated = Debug.getNativeHeapAllocatedSize() / 1024 / 1024 - 16;
        mNativeHeapAllocatedTotal += nativeAllocated;
        Log.i(PerformanceTest.TAG, "NativeHeapAllocated:" + nativeAllocated + "MB");
    }
}
