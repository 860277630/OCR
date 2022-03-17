package com.example.demo.utils;


import java.util.concurrent.*;

/**
 * Description: ThreadPoolUtil <br>
 *
 * @author lxl
 */
public class ThreadPoolUtil {


    private volatile static ExecutorService pool = null;

//    private static final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("pool-%d").build();

    /**
     * 获取单实例线程池
     * corePoolSize 核心池大小
     * maximumPoolSize 最大线程数
     * keepAliveTime    存活时间（默认60秒回收部分空闲的线程）
     * TimeUnit.SECONDS 时间单位
     * 在执行任务之前用于保留任务的队列。
     * 执行程序*创建新线程时要使用的工厂
     * 处理程序当执行被阻止时要使用的处理程序*因为达到了线程界限和队列容量
     *
     * @return
     */
    public static ExecutorService getThreadPoolExecutor() {
        if (pool == null) {
            synchronized (ThreadPoolUtil.class) {
                if (pool == null || pool.isShutdown()) {
                    pool = new ThreadPoolExecutor(
                            10,
                            18,
                            30,
                            TimeUnit.SECONDS,
                            new ArrayBlockingQueue<Runnable>(1),
                            Executors.defaultThreadFactory(),
                            new CustomRejectedExecutionHandler());
                }
            }
        }
        return pool;
    }


}
