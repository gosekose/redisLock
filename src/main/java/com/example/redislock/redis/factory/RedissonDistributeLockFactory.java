package com.example.redislock.redis.factory;

import com.example.redislock.redis.DistributeLock;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Profile("redisson")
@RequiredArgsConstructor
public class RedissonDistributeLockFactory implements DistributeLockFactory {
    private final RedissonClient redissonClient;

    @Override
    public DistributeLock createLock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        return new RedissonDistributeLock(lock);
    }

    private record RedissonDistributeLock(RLock lock) implements DistributeLock {

        @Override
            public boolean tryLock(long timeOut, TimeUnit unit) throws InterruptedException {
                return lock.tryLock(timeOut, unit);
            }

            @Override
            public void unLock() {
                lock.unlock();
            }

            @Override
            public boolean isLocked() {
                return lock.isHeldByCurrentThread();
            }
        }
}
