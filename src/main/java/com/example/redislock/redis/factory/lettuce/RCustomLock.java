package com.example.redislock.redis.factory.lettuce;

import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public interface RCustomLock {

    void setLock(String lockKey);
    boolean tryLock(long timeOut, TimeUnit unit) throws InterruptedException;
    void unLock();

    boolean isLocked();
}
