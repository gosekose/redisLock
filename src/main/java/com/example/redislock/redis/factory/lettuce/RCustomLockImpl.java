package com.example.redislock.redis.factory.lettuce;

import com.example.redislock.config.ServerInstance;
import com.example.redislock.redis.exception.RedisLockException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RCustomLockImpl implements RCustomLock {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ServerInstance serverInstance;
    private String lockKey;

    public RCustomLockImpl(RedisTemplate<String, Object> redisTemplate, ServerInstance serverInstance) {
        this.redisTemplate = redisTemplate;
        this.serverInstance = serverInstance;
    }

    @Override
    public void setLock(String lockKey) {
        this.lockKey = lockKey;
    }

    @Override
    public boolean tryLock(long timeOut, TimeUnit unit) throws InterruptedException {
        int retryCount = 0;
        int maxRetryCount = 10;

        while (retryCount < maxRetryCount) {
            try {
                boolean success = lock(lockKey, timeOut, unit);
                if (success) {
                    return true;
                } else {
                    retryCount++;
                    Thread.sleep(getTimeToMillis(timeOut, unit));
                }
            } catch (Exception e) {
                throw new RedisLockException();
            }
        }
        throw new RedisLockException();
    }

    @Override
    public void unLock() {
        String value = (String) redisTemplate.opsForValue().get(lockKey);
        if (value != null) redisTemplate.delete(lockKey);
    }

    @Override
    public boolean isLocked() {
        String currentThread = Thread.currentThread().toString();
        String value = (String) redisTemplate.opsForValue().get(lockKey);
        return value != null && value.equals(serverInstance + currentThread);
    }

    private boolean lock(String lockKey, long timeOut, TimeUnit unit) {
        String currentThread = Thread.currentThread().toString();
        Boolean success = redisTemplate.opsForValue().setIfAbsent(lockKey, serverInstance.getInstanceId() + currentThread, timeOut, unit);
        return success != null && success;
    }

    private long getTimeToMillis(long timeOut, TimeUnit unit) {
        long time;
        if (unit == TimeUnit.DAYS) time = TimeUnit.DAYS.toMillis(timeOut);
        else if (unit == TimeUnit.HOURS) time = TimeUnit.HOURS.toMillis(timeOut);
        else if (unit == TimeUnit.MINUTES) time = TimeUnit.DAYS.toMillis(timeOut);
        else if (unit == TimeUnit.SECONDS) time = TimeUnit.SECONDS.toMillis(timeOut);
        else if (unit == TimeUnit.MICROSECONDS) time = TimeUnit.MICROSECONDS.toMillis(timeOut);
        else if (unit == TimeUnit.NANOSECONDS) time = TimeUnit.NANOSECONDS.toMillis(timeOut);
        else time = timeOut;

        return time;
    }
}
