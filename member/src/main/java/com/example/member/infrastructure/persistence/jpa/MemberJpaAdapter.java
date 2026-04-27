package com.example.member.infrastructure.persistence.jpa;

import com.example.member.application.port.out.MemberPersistencePort;
import com.example.member.domain.entity.Member;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberJpaAdapter implements MemberPersistencePort {

    private final MemberJpaRepository memberJpaRepository;

    @Override
    public Member save(Member member) {
        return memberJpaRepository.save(member);
    }

    @Override
    public Optional<Member> findById(UUID memberId) {
        return memberJpaRepository.findById(memberId);
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        return memberJpaRepository.findByEmail(email);
    }

    @Override
    public boolean existsByEmail(String email) {
        return memberJpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByEmailAndMemberIdNot(String email, UUID memberId) {
        return memberJpaRepository.existsByEmailAndMemberIdNot(email, memberId);
    }
}
