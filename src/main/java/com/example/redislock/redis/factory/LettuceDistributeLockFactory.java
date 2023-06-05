package com.example.redislock.redis.factory;

import com.example.redislock.redis.DistributeLock;
import com.example.redislock.redis.factory.lettuce.RCustomLock;
import com.example.redislock.redis.factory.lettuce.RCustomLockImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Profile("others")
@Component
@RequiredArgsConstructor
public class LettuceDistributeLockFactory implements DistributeLockFactory {
    private final RedisTemplate<String, Object> redisTemplate;
    @Override
    public DistributeLock createLock(String lockKey) {
        // lock을 얻는 값
        RCustomLockImpl lock = new RCustomLockImpl(redisTemplate);
        lock.setLock(lockKey);
        return new LettuceDistributeLock(lock);
    }

    private record LettuceDistributeLock(RCustomLock lock) implements DistributeLock {
        @Override
        public boolean tryLock(long timeOut, TimeUnit unit) throws InterruptedException {
            return lock.tryLock(timeOut, unit);
        }

        @Override
        public void unLock() {
            lock.unLock();
        }

        @Override
        public boolean isLocked() {
            return lock.isLocked();
        }
    }
}