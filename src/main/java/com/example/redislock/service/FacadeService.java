package com.example.redislock.service;

import com.example.redislock.domain.Member;
import com.example.redislock.service.MemberService;
import com.example.redislock.service.dto.MemberDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FacadeService {
    private final MemberService memberService;

    public Long save(MemberDto memberDto) {
        return memberService.save(memberDto);
    }

    public Long pay(Long id, long fee) {
        return memberService.pay(id, fee);
    }

}
