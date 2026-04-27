package com.example.member.application.service;

import com.example.member.application.dto.command.CreateMemberRestrictionCommand;
import com.example.member.application.dto.result.MemberRestrictionResult;
import com.example.member.application.port.out.MemberPersistencePort;
import com.example.member.application.port.out.MemberRestrictionPersistencePort;
import com.example.member.application.port.in.MemberRestrictionUsecase;
import com.example.member.common.exception.DuplicateActiveRestrictionException;
import com.example.member.common.exception.MemberNotFoundException;
import com.example.member.common.exception.MemberRestrictionNotFoundException;
import com.example.member.domain.entity.MemberRestriction;
import com.example.member.domain.enumtype.RestrictionType;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import com.todaylunch.common.security.auth.util.RoleGuard;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberRestrictionService implements MemberRestrictionUsecase {

    private final MemberPersistencePort memberPersistencePort;
    private final MemberRestrictionPersistencePort memberRestrictionPersistencePort;

    @Transactional
    @Override
    public MemberRestrictionResult createRestriction(
            AuthenticatedMember authenticatedMember,
            CreateMemberRestrictionCommand command
    ) {
        RoleGuard.requireAdmin(authenticatedMember);
        validateCreateCommand(command);

        UUID memberId = command.memberId();
        memberPersistencePort.findById(memberId).orElseThrow(MemberNotFoundException::new);

        LocalDateTime now = LocalDateTime.now();
        RestrictionType restrictionType = command.restrictionType();
        if (memberRestrictionPersistencePort.existsActiveRestriction(memberId, restrictionType, now)) {
            throw new DuplicateActiveRestrictionException();
        }

        MemberRestriction memberRestriction = MemberRestriction.create(
                UUID.randomUUID(),
                memberId,
                authenticatedMember.memberId(),
                command.reason(),
                restrictionType,
                command.durationHours(),
                now
        );

        return toResult(memberRestrictionPersistencePort.save(memberRestriction));
    }

    @Transactional
    @Override
    public MemberRestrictionResult deactivateRestriction(
            AuthenticatedMember authenticatedMember,
            UUID restrictionId
    ) {
        RoleGuard.requireAdmin(authenticatedMember);

        MemberRestriction memberRestriction = memberRestrictionPersistencePort.findById(restrictionId)
                .orElseThrow(MemberRestrictionNotFoundException::new);

        LocalDateTime now = LocalDateTime.now();
        memberRestriction.deactivate(now);
        return toResult(memberRestriction);
    }

    @Override
    public List<MemberRestrictionResult> getMemberRestrictions(
            AuthenticatedMember authenticatedMember,
            UUID memberId
    ) {
        RoleGuard.requireAdmin(authenticatedMember);
        memberPersistencePort.findById(memberId).orElseThrow(MemberNotFoundException::new);

        return memberRestrictionPersistencePort.findAllByMemberId(memberId).stream()
                .map(this::toResult)
                .toList();
    }

    public MemberRestriction getActiveLoginRestriction(UUID memberId, LocalDateTime now) {
        return memberRestrictionPersistencePort.findActiveRestriction(memberId, RestrictionType.LOGIN_BAN, now)
                .orElse(null);
    }

    private void validateCreateCommand(CreateMemberRestrictionCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("회원 제재 생성 요청은 필수입니다.");
        }
        if (command.memberId() == null) {
            throw new IllegalArgumentException("memberId는 필수입니다.");
        }
        if (command.restrictionType() == null) {
            throw new IllegalArgumentException("restrictionType은 필수입니다.");
        }
    }

    private MemberRestrictionResult toResult(MemberRestriction memberRestriction) {
        return new MemberRestrictionResult(
                memberRestriction.getRestrictionId(),
                memberRestriction.getMemberId(),
                memberRestriction.getAdminId(),
                memberRestriction.getReason(),
                memberRestriction.getRestrictionType(),
                memberRestriction.getDurationHours(),
                memberRestriction.getEndAt(),
                memberRestriction.isActive(),
                memberRestriction.getCreatedAt(),
                memberRestriction.getUpdatedAt()
        );
    }
}

