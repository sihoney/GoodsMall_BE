package com.example.member.application.port.out;

import com.example.member.domain.entity.MemberRestriction;
import com.example.member.domain.enumtype.RestrictionType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MemberRestrictionPersistencePort {

    MemberRestriction save(MemberRestriction memberRestriction);

    Optional<MemberRestriction> findById(UUID restrictionId);

    boolean existsActiveRestriction(UUID memberId, RestrictionType restrictionType, LocalDateTime now);

    Optional<MemberRestriction> findActiveRestriction(UUID memberId, RestrictionType restrictionType, LocalDateTime now);

    List<MemberRestriction> findAllByMemberId(UUID memberId);
}
