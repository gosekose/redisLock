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
public class Member {

    @Id @GeneratedValue
    private Long id;

    private String email;
    private String password;
    private Long money = 1_000L;

    @Builder
    public Member(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public Long minusMoney(long fee) {
        if (money < fee) throw new IllegalArgumentException();
        return money -= fee;
    }
}
