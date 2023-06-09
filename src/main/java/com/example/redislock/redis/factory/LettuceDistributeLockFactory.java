package com.example.redislock.redis.factory;

import com.example.redislock.redis.factory.lettuce.ServerInstance;
import com.example.redislock.redis.DistributeLock;
import com.example.redislock.redis.factory.lettuce.RCustomLockClient;
import com.example.redislock.redis.factory.lettuce.RCustomLockClientImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Profile("lettuce")
@Component
@RequiredArgsConstructor
public class LettuceDistributeLockFactory implements DistributeLockFactory {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ServerInstance serverInstance;

    @Override
    public DistributeLock createLock(String lockKey) {
        // lock을 얻는 값
        RCustomLockClientImpl lock = new RCustomLockClientImpl(redisTemplate, serverInstance);
        lock.setLock(lockKey);
        return new LettuceDistributeLock(lock);
    }

    private record LettuceDistributeLock(RCustomLockClient lock) implements DistributeLock {
        private static final long timeOut = 50L;
        private static final TimeUnit unit = TimeUnit.MILLISECONDS;

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

        @Override
        public long getTimeOut() {
            return timeOut;
        }

        @Override
        public TimeUnit getTimeUnit() {
            return unit;
        }
    }
}
