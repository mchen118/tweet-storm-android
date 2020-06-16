package com.muchen.tweetstormmaker.concurrent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppExecutorServices {
    private static AppExecutorServices soleInstance = new AppExecutorServices(Executors.newSingleThreadExecutor(),
            Executors.newSingleThreadExecutor());
    private ExecutorService diskIO;
    private ExecutorService networkIO;

    private AppExecutorServices(ExecutorService diskIO, ExecutorService networkIO) {
        this.diskIO = diskIO;
        this.networkIO = networkIO;
    }

    public static AppExecutorServices soleInstance() { return soleInstance; }
    public ExecutorService diskIO() { return diskIO; }
    public ExecutorService networkIO() { return networkIO; }
}
