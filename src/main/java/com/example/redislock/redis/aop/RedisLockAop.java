package com.example.redislock.redis.aop;

import com.example.redislock.redis.DistributeLock;
import com.example.redislock.redis.DistributeLockManager;
import com.example.redislock.redis.exception.RedisLockException;
import com.example.redislock.service.dto.MemberDto;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
public class RedisLockAop {

    private final DistributeLockManager lockManager;

    @Around("execution(* com.example.redislock.service.FacadeService.save(..)) && args(dto)")
    public Long save(ProceedingJoinPoint joinPoint, MemberDto dto) throws Throwable {
        String lockKey = "Save:" + dto.getEmail();
        return (Long) executeWithRedisLock(joinPoint, lockKey);
    }

    @Around("execution(* com.example.redislock.service.FacadeService.pay(..)) && args(id, fee)")
    public Long pay(ProceedingJoinPoint joinPoint, Long id, Long fee) throws Throwable {
        String lockKey = "Pay:" + id;
        return (Long) executeWithRedisLock(joinPoint, lockKey);
    }

    public Object executeWithRedisLock(ProceedingJoinPoint joinPoint, String lockKey) throws Throwable {

        DistributeLock lock = lockManager.getLock(lockKey);

        try {
            boolean isLocked = lock.tryLock(50, TimeUnit.MILLISECONDS);
            if (!isLocked) throw new RedisLockException();
            return joinPoint.proceed();

        } finally {
            if (lock.isLocked()) {
                lock.unLock();
            }
        }
    }
}
