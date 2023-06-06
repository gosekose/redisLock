package com.example.redislock.service;

import com.example.redislock.domain.Member;
import com.example.redislock.repository.MemberRepository;
import com.example.redislock.service.dto.MemberDto;
import com.example.redislock.service.dto.MemberIdDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public Long save(MemberDto memberDto) {

        if (findByEmail(memberDto.getEmail()) != null) {
            log.info("{} 존재", memberDto.getEmail());
            throw new IllegalArgumentException("이메일이 존재합니다.");
        }

        log.info("{} 존재 X, 현재 스레드 = {}", memberDto.getEmail(), Thread.currentThread());
        return memberRepository.save(Member.builder()
                .email(memberDto.getEmail()).password(memberDto.getPassword()).build()).getId();
    }

    @Transactional
    public Long pay(Long id, long fee) {
        Member member = findById(id);
        log.info("현재 금액 = {}, 현재 스레드 = {}", member.getMoney(), Thread.currentThread());
        return member.minusMoney(fee); // 남아있는 금액 반환
    }

    @Transactional(readOnly = true)
    public Member findById(MemberIdDto memberDto) {
        return memberRepository.findById(memberDto.getId()).orElseThrow();
    }

    @Transactional(readOnly = true)
    public Member findById(Long id) {return memberRepository.findById(id).orElseThrow(() -> {throw new IllegalArgumentException("not found member");});}

    @Transactional(readOnly = true)
    public Member findByEmail(String email) {
        return memberRepository.findMemberByEmail(email);
    }

}
