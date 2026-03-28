package com.example.member.infrastructure.repository;

import com.example.member.domain.entity.MemberRestriction;
import com.example.member.domain.enumtype.RestrictionType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRestrictionJpaRepository extends JpaRepository<MemberRestriction, UUID> {

    boolean existsByMemberIdAndRestrictionTypeAndActiveTrueAndEndAtAfter(
            UUID memberId,
            RestrictionType restrictionType,
            LocalDateTime now
    );

    Optional<MemberRestriction> findFirstByMemberIdAndRestrictionTypeAndActiveTrueAndEndAtAfterOrderByEndAtDesc(
            UUID memberId,
            RestrictionType restrictionType,
            LocalDateTime now
    );

    List<MemberRestriction> findAllByMemberIdOrderByCreatedAtDesc(UUID memberId);
}
