package com.example.redislock.redis;

import java.util.concurrent.TimeUnit;

public interface DistributeLock {

    boolean tryLock(long timeOut, TimeUnit unit) throws InterruptedException;
    void unLock();

    boolean isLocked();
    long getTimeOut();
    TimeUnit getTimeUnit();
}
