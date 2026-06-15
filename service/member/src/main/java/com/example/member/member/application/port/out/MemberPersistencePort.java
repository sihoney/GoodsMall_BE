package com.example.member.member.application.port.out;

import com.example.member.member.domain.entity.Member;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MemberPersistencePort {

    Member save(Member member);

    Optional<Member> findById(UUID memberId);

    Optional<Member> findByEmail(String email);

    List<Member> findAllByIds(Iterable<UUID> memberIds);

    boolean existsByEmail(String email);

    boolean existsByEmailAndMemberIdNot(String email, UUID memberId);
}
