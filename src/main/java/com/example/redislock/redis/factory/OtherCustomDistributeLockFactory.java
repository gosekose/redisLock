package com.example.redislock.redis.factory;

import com.example.redislock.redis.DistributeLock;

import java.util.concurrent.TimeUnit;

public class OtherCustomDistributeLock implements DistributeLock {

    @Override
    public void unLock() {
        // 다른 락이 구현된다면, unlock 오버라이딩
    }

    @Override
    public boolean tryLock(long timeOut, TimeUnit unit) throws InterruptedException {
        return false; // 다른 락이 구현된다면, tryLock 오버라이딩
    }

    @Override
    public boolean isLocked() {
        return false;
    }
}
