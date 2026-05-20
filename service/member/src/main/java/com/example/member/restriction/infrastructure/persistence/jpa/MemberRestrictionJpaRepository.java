package com.example.member.restriction.infrastructure.persistence.jpa;

import com.example.member.restriction.domain.entity.MemberRestriction;
import com.example.member.restriction.domain.enumtype.RestrictionType;
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

    List<MemberRestriction> findAllByOrderByCreatedAtDesc();

    List<MemberRestriction> findAllByMemberIdOrderByCreatedAtDesc(UUID memberId);
}
