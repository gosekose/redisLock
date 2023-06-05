package com.example.redislock.redis.factory;

import com.example.redislock.redis.DistributeLock;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Profile("others")
@Component
public class OtherCustomDistributeLockFactory implements DistributeLockFactory {
    @Override
    public DistributeLock createLock(String lockKey) {
        // lock을 얻는 값
        return new OtherCustomDistributeLock();
    }

    private record OtherCustomDistributeLock() implements DistributeLock {
        @Override
        public boolean tryLock(long timeOut, TimeUnit unit) throws InterruptedException {
            return false;
        }

        @Override
        public void unLock() {

        }

        @Override
        public boolean isLocked() {
            return false;
        }
    }
}
