package com.example.member.infrastructure.repository;

import com.example.member.domain.entity.Member;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberRepository {

    private final MemberJpaRepository memberJpaRepository;

    public Member save(Member member) {
        return memberJpaRepository.save(member);
    }

    public Optional<Member> findById(UUID memberId) {
        return memberJpaRepository.findById(memberId);
    }

    public Optional<Member> findByEmail(String email) {
        return memberJpaRepository.findByEmail(email);
    }

    public boolean existsByEmail(String email) {
        return memberJpaRepository.existsByEmail(email);
    }

    public boolean existsByEmailAndMemberIdNot(String email, UUID memberId) {
        return memberJpaRepository.existsByEmailAndMemberIdNot(email, memberId);
    }
}
