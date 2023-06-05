package com.example.redislock.service;

import com.example.redislock.domain.Member;
import com.example.redislock.repository.MemberRepository;
import com.example.redislock.service.dto.MemberDto;
import com.example.redislock.service.dto.MemberIdDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("redisson")
@SpringBootTest
class FacadeServiceRedissonTest {
    @Autowired private FacadeService facadeService;
    @Autowired private MemberService memberService;
    @Autowired private MemberRepository memberRepository;
    private Long memberId;

    @BeforeEach
    public void init() {
        memberId = facadeService.save(new MemberDto("gosekose@naver.com", "1234"));
    }

    @AfterEach
    public void clear() {
        memberRepository.deleteAll();
    }

    @Test
    @DisplayName("단일 스레드 저장 테스트")
    public void save_single() throws Exception {
        //given
        String email = "kose@naver.com";
        String password = "12345";

        MemberDto memberDto = new MemberDto(email, password);

        //when
        Long saveId = facadeService.save(memberDto);
        Member member = memberService.findById(new MemberIdDto(saveId));

        //then
        assertThat(member.getEmail()).isEqualTo(email);
        assertThat(member.getPassword()).isEqualTo(password);
    }

    @Test
    @DisplayName("멀티 스레드 상황에서 저장 테스트")
    public void save_multi() throws Exception {
        //given
        int threadCount = 32;
        String email = "kose@naver.com";
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        List<Future<?>> futures = new ArrayList<>();

        //when
        for (int i = 0; i < threadCount; i++) {
            int finalI = i;
            Future<?> future = executorService.submit(() -> {
                try {
                    facadeService.save(new MemberDto(email, "1234" + finalI));
                } catch (Exception e) {
                    throw new RuntimeException("에러", e);
                }
            });
            futures.add(future);
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                e.getCause().printStackTrace();
            }
        }

        //then
        List<Member> members = memberRepository.findAllByEmail(email);
        assertThat(members.size()).isEqualTo(1);
        System.out.println(members.get(0).getPassword());
    }

    @Test
    @DisplayName("멀티 스레드 pay")
    public void pay_multi() throws Exception {
        //given
        int threadCount = 32;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        List<Future<?>> futures = new ArrayList<>();

        //when
        for (int i = 0; i < threadCount; i++) {
            Future<?> future = executorService.submit(() -> {
                try {
                    facadeService.pay(memberId, 1);
                } catch (Exception e) {
                    throw new RuntimeException("에러", e);
                }
            });
            futures.add(future);
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                e.getCause().printStackTrace();
            }
        }

        //then
        Member findMember = memberService.findById(memberId);
        assertThat(findMember.getMoney()).isEqualTo(1_000 - threadCount);
    }
}