package com.example.member.infrastructure.persistence.jpa;

import java.util.UUID;
import java.util.Optional;
import com.example.member.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberJpaRepository extends JpaRepository<Member, UUID> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndMemberIdNot(String email, UUID memberId);

    Optional<Member> findByEmail(String email);
}
