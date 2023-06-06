RedisLock 분산락 구현하기
======================
<br/><br/>
# 1. 추상화 계층 도입
- RedissonClient와 같은 외부 라이브러리에 의존하지 않기 위해 DistributeLockManager 도입하였습니다.
- DistributeLockManager는 DistributeLockFactory를 의존성 주입 받아서, 프로파일에서 설정한 lock 구현체 종류를 선택할 수 있습니다.
- AOP에서는 DistributeLockManager를 의존성 주입받아서, 해당 구현체가 무엇인지 알지 못하도록 구성하였습니다.

<br/><br/>
# 2. 구조도
## ![image](https://github.com/gosekose/redisLock/assets/88478829/0b42d37b-f522-4334-b738-97928772dec3)
- RedisAop는 DistributeLockManager를 의존하고 있습니다. 
- DistributeLockManager는 DistributeLockFactory에 의존하며, 현재 프로파일에 설정된 Factory에 의해 DistributeLock을 얻습니다. 
- DistributeLock은 RedissonClient를 사용하는 RedissonDistributeLock, Redistemplate로 원자적 연산으로 구현한 LettuceDistributeLock 구현체가 있습니다. 
- 그 결과, 프로파일 설정시에 두 구현체를 선택할 수 있고, AOP는 어떠한 구현체를 선택했는지 알 수 없기 때문에 코드 수정에 대한 유지보수성을 높일 수 있었습니다. 

<br/><br/>
# 3. 주요 기능 상세

RedisLockAop
- https://github.com/gosekose/redisLock/blob/main/src/main/java/com/example/redislock/redis/aop/RedisLockAop.java
- Aop를 작성하는 로직입니다. DistributeLockManager를 20번째 줄에서 의존받고 있습니다. 
- 36번째 줄에서 lockManager.getLock(lockKey)로 DistributeLock을 생성하여 락을 처리하는 로직을 구성하였습니다.
<br/><br/>

DistributeLockManager
- https://github.com/gosekose/redisLock/blob/main/src/main/java/com/example/redislock/redis/DistributeLockManager.java
- distributeLockFactory를 의존성 주입받으며, ConcurrentHashMap으로 lockMap을 형성하여 주어진 키가 lockMap에 없을 경우 새 값을 추가합니다. 
<br/><br/>
  
DistributeLock
- https://github.com/gosekose/redisLock/blob/main/src/main/java/com/example/redislock/redis/DistributeLock.java
- 분산락을 구현하는 인터페이스로 tryLock(), unLock() 등의 추상화 메서드를 가지고 있습니다.
- getTimeOut(), getTimeUnit()은 테스트 결과, 외부 라이브러리 및 직접 구현한 구현체 간의 시간 측정에 따라 분산락 테스트가 성공 및 실패하는 것을 확인할 수 있었습니다.
- RedissonClient는 getTime()을 길게 준다면 성공하지만, 직접 구현한 로직은 길게 준다면 Thead.sleep()으로 계속 기다리는 문제가 발생하여 두 메서드를 추상화하여 구현체에서 timeOut및 timeUnit을 설정하도록 하였습니다.    
<br/><br/>
 
RedissonDistributeLockFactory
- https://github.com/gosekose/redisLock/blob/main/src/main/java/com/example/redislock/redis/factory/RedissonDistributeLockFactory.java
- DistributeLockFactory의 구현체로, RedissonClient의 라이브러리를 의존 받습니다.
- 특이점은 redissonClient에 timeOut 시간을 짧게하면 테스트를 실패하여, 긴 시간을 주입하기 위해 1_000L을 입력하였습니다.(밀리초)
<br/><br/>

LettuceDistributeLockFactory
- https://github.com/gosekose/redisLock/blob/main/src/main/java/com/example/redislock/redis/factory/LettuceDistributeLockFactory.java
- RCustomLockClient를 생성한 후,  LettuceDistributeLock을 생성하는 인자로 주입합니다.
- RCustomLockClientImpl은 RedissonClient를 대체하는 직접 구현한 구현체입니다.
- 특이점은 락을 획득할 때 까지 여러 번 while문을 돌아야 하기 때문에 Thread.sleep() 시간을 50L으로 작게 설정하였습니다 (밀리초)
<br/><br/>

ServerInstance
- https://github.com/gosekose/redisLock/blob/main/src/main/java/com/example/redislock/redis/factory/lettuce/ServerInstance.java
- 분산락에서, 스레드 A가 설정한 Key를 다른 스레드인 B가 해당 키를 제거하는 일을 방지할 필요성이 있었습니다.
- 서버가 여러 개라는 가정하에 각 서버의 유니크한 값을 생성하기 위해 런타임 시점에 instanceId를 생성할 수 있도록 하였습니다.
- 해당 인스턴스는 이어지는 RCustomLockImpl에서 Thread.currentThread()의 값과 합쳐져서 유일한 스레드를 식별할 수 있는 값으로 value에 입력하였습니다.
<br/><br/>

RCustomLockClientImpl
- https://github.com/gosekose/redisLock/blob/main/src/main/java/com/example/redislock/redis/factory/lettuce/RCustomLockClientImpl.java
  ``` java
      @Override
    public boolean tryLock(long timeOut, TimeUnit unit) throws InterruptedException {
        int retryCount = 0;
        int maxRetryCount = 1000; // 최대 1000번 반복

        while (retryCount < maxRetryCount) {
            try {
                boolean success = lock(lockKey, timeOut, unit); // 원자적 연산으로 lock을 가지고 있는지 판단하는 로직
                if (success) {
                    return true;
                } else {
                    retryCount++;
                    Thread.sleep(getTimeToMillis(timeOut, unit)); // 앞서 설정한 timeOut만큼 기다리기
                }
            } catch (Exception e) {
                throw new RedisLockException();
            }
        }
        throw new RedisLockException(); // 최종 끝까지 락을 획득하지 못한 경우 예외 발생
    }
    
        private boolean lock(String lockKey, long timeOut, TimeUnit unit) { 
        String currentThread = Thread.currentThread().toString(); // 현재 스레드 번호로 유일한 식별자 값을 value에 넣기 위함 
        Boolean success = redisTemplate.opsForValue().setIfAbsent(lockKey, serverInstance.getInstanceId() + currentThread, timeOut, unit); // setIfAbsent로 redis에 대한 원자적 연산 실행
        return success != null && success; 
    }
    
  ```
    
  - 락을 해제하는 과정은 redissonClient의 isHeldByCurrentThread() 처럼 락을 설정한 스레드가 락을 종료할 수 있도록 value의 값을 검증하도록 하였습니다.
  - 만약 서버가 동일하고 설정한 스레드가 맞다면 value가 equals되어 락이 해제됩니다.
<br/><br/>

<br/><br/>
# 4. 테스트
  - 서버 기동시에 활성화된 프로파일에 redisson 혹은 lettuce를 작성하여야 합니다.
  - 테스트 대상은 동일한 email로 회원가입에 대한 1개의 회원 가입 허용 및, 다수의 pay 요청에 대해 money 차감이 올바르게 되도록 하는 것 입니다.
  
  - Service 로직
    - https://github.com/gosekose/redisLock/blob/main/src/main/java/com/example/redislock/service/MemberService.java
  
  - FacadeService에는 분산락을 적용하고 실제 로직에 대해서는 transaction을 적용하기 위해 작성하였습니다. (분산락 적용, 실제 정책에는 transaction 적용 -> 생명주기 맞추기)

  <br/><br/>
  FacadeServiceRedissonTest
  - https://github.com/gosekose/redisLock/blob/main/src/test/java/com/example/redislock/service/FacadeServiceRedissonTest.java
  - RedissonTest를 위해 @ActiveProfiles("redisson")로 설정하였습니다.
  ![image](https://github.com/gosekose/redisLock/assets/88478829/6e51fed4-906c-43b7-a8f0-4cdc875088f7)
  <br/><br/>

  - FacadeServiceLettuceTest: https://github.com/gosekose/redisLock/blob/main/src/test/java/com/example/redislock/service/FacadeServiceLettuceTest.java
  - 직접 구현한 구현체 테스트를 위해  @ActiveProfiles("lettuce")로 설정하였습니다.
  ![image](https://github.com/gosekose/redisLock/assets/88478829/43bfd89e-7c3b-4f1f-8588-e3932f162359)

<br/><br/>
# 5. 보안점
  - timeOut의 길이에 따라, 테스트를 성공하거나 실패하는 결과를 얻었습니다. 이는 적절한 시간을 계속 테스트하는 과정이 필요합니다.
  - 실제 Thread.sleep()하는 로직이 구성되어 있는데, 이 부분이 가장 취약한 로직이 될 것 같습니다.

