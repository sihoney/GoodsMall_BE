package com.example.member.infrastructure.repository;

import com.example.member.domain.entity.MemberRestriction;
import com.example.member.domain.enumtype.RestrictionType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberRestrictionRepository {

    private final MemberRestrictionJpaRepository memberRestrictionJpaRepository;

    public MemberRestriction save(MemberRestriction memberRestriction) {
        return memberRestrictionJpaRepository.save(memberRestriction);
    }

    public Optional<MemberRestriction> findById(UUID restrictionId) {
        return memberRestrictionJpaRepository.findById(restrictionId);
    }

    public boolean existsActiveRestriction(UUID memberId, RestrictionType restrictionType, LocalDateTime now) {
        return memberRestrictionJpaRepository.existsByMemberIdAndRestrictionTypeAndActiveTrueAndEndAtAfter(
                memberId,
                restrictionType,
                now
        );
    }

    public Optional<MemberRestriction> findActiveRestriction(
            UUID memberId,
            RestrictionType restrictionType,
            LocalDateTime now
    ) {
        return memberRestrictionJpaRepository.findFirstByMemberIdAndRestrictionTypeAndActiveTrueAndEndAtAfterOrderByEndAtDesc(
                memberId,
                restrictionType,
                now
        );
    }

    public List<MemberRestriction> findAllByMemberId(UUID memberId) {
        return memberRestrictionJpaRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId);
    }
}
