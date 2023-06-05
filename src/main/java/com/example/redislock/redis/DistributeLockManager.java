package com.example.redislock.redis;

import com.example.redislock.redis.factory.DistributeLockFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class DistributeLockManager {
    private final DistributeLockFactory distributeLockFactory;
    private final Map<String, DistributeLock> lockMap = new ConcurrentHashMap<>();

    public DistributeLock getLock(String lockKey) {
        return lockMap.computeIfAbsent(lockKey, distributeLockFactory::createLock);
    }
}
