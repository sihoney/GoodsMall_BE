package com.example.member.infrastructure.persistence.jpa;

import com.example.member.application.port.out.MemberRestrictionPersistencePort;
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
public class MemberRestrictionJpaAdapter implements MemberRestrictionPersistencePort {

    private final MemberRestrictionJpaRepository memberRestrictionJpaRepository;

    @Override
    public MemberRestriction save(MemberRestriction memberRestriction) {
        return memberRestrictionJpaRepository.save(memberRestriction);
    }

    @Override
    public Optional<MemberRestriction> findById(UUID restrictionId) {
        return memberRestrictionJpaRepository.findById(restrictionId);
    }

    @Override
    public boolean existsActiveRestriction(UUID memberId, RestrictionType restrictionType, LocalDateTime now) {
        return memberRestrictionJpaRepository.existsByMemberIdAndRestrictionTypeAndActiveTrueAndEndAtAfter(
                memberId,
                restrictionType,
                now
        );
    }

    @Override
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

    @Override
    public List<MemberRestriction> findAllByMemberId(UUID memberId) {
        return memberRestrictionJpaRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId);
    }
}
