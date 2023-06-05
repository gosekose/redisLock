package com.example.redislock.redis.factory;

import com.example.redislock.redis.DistributeLock;

public interface DistributeLockFactory {
    DistributeLock createLock(String lockKey);
}
