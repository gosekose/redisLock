package com.example.redislock.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Members {

    @Id @GeneratedValue
    private Long id;

    private String name;
    private int age;

    @Builder
    public Members(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
