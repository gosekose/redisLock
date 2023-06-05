package com.example.redislock.service;

import com.example.redislock.domain.Member;
import com.example.redislock.repository.MemberRepository;
import com.example.redislock.service.dto.MemberDto;
import com.example.redislock.service.dto.MemberIdDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Transactional
@ActiveProfiles("redisson")
@SpringBootTest
class MemberServiceTest {
    @Autowired private FacadeService facadeService;
    @Autowired private MemberService memberService;
    @Autowired private MemberRepository memberRepository;

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
        Assertions.assertThat(member.getEmail()).isEqualTo(email);
        Assertions.assertThat(member.getPassword()).isEqualTo(password);
    }

    @Test
    @DisplayName("멀티 스레드 상황에서 저장 테스트")
    public void save_multi() throws Exception {
        //given
        String email = "kose@naver.com";
        int threadCount = 32;
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
        Assertions.assertThat(members.size()).isEqualTo(1);
        System.out.println(members.get(0).getPassword());
    }

    @Test
    @DisplayName("멀티 스레드 pay")
    public void pay_multi() throws Exception {
        //given
        String email = "kose@naver.com";
        int threadCount = 32;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        List<Future<?>> futures = new ArrayList<>();
        Long memberId = facadeService.save(new MemberDto(email, "1234"));


        //when
        for (int i = 0; i < threadCount; i++) {
            int finalI = i;
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
        List<Member> members = memberRepository.findAllByEmail(email);
        Assertions.assertThat(members.size()).isEqualTo(1);
        System.out.println(members.get(0).getPassword());

    }
}