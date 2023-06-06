package com.example.redislock.redis.factory.lettuce;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@NoArgsConstructor
@Component
public class ServerInstance {

    @Value("#{T(java.util.UUID).randomUUID().toString()}")
    private String instanceId;
}