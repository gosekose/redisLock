package com.example.redislock.repository;

import com.example.redislock.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Member findMemberByEmail(String email);
    List<Member> findAllByEmail(String email);
}
