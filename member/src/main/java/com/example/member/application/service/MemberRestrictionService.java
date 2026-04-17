package com.example.member.application.service;

import com.example.member.application.usecase.MemberRestrictionUsecase;
import com.example.member.common.exception.DuplicateActiveRestrictionException;
import com.example.member.common.exception.MemberNotFoundException;
import com.example.member.common.exception.MemberRestrictionNotFoundException;
import com.example.member.domain.entity.MemberRestriction;
import com.example.member.domain.enumtype.RestrictionType;
import com.example.member.infrastructure.repository.MemberRepository;
import com.example.member.infrastructure.repository.MemberRestrictionRepository;
import com.example.member.presentation.dto.CreateMemberRestrictionRequest;
import com.example.member.presentation.dto.MemberRestrictionResponse;
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

    private final MemberRepository memberRepository;
    private final MemberRestrictionRepository memberRestrictionRepository;

    @Transactional
    @Override
    public MemberRestrictionResponse createRestriction(
            AuthenticatedMember authenticatedMember,
            CreateMemberRestrictionRequest request
    ) {
        RoleGuard.requireAdmin(authenticatedMember);
        validateCreateRequest(request);

        UUID memberId = request.memberId();
        memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);

        LocalDateTime now = LocalDateTime.now();
        RestrictionType restrictionType = request.restrictionType();
        if (memberRestrictionRepository.existsActiveRestriction(memberId, restrictionType, now)) {
            throw new DuplicateActiveRestrictionException();
        }

        MemberRestriction memberRestriction = MemberRestriction.create(
                UUID.randomUUID(),
                memberId,
                authenticatedMember.memberId(),
                request.reason(),
                restrictionType,
                request.durationHours(),
                now
        );

        MemberRestriction savedRestriction = memberRestrictionRepository.save(memberRestriction);
        return MemberRestrictionResponse.from(savedRestriction);
    }

    @Transactional
    @Override
    public MemberRestrictionResponse deactivateRestriction(
            AuthenticatedMember authenticatedMember,
            UUID restrictionId
    ) {
        RoleGuard.requireAdmin(authenticatedMember);

        MemberRestriction memberRestriction = memberRestrictionRepository.findById(restrictionId)
                .orElseThrow(MemberRestrictionNotFoundException::new);

        LocalDateTime now = LocalDateTime.now();
        memberRestriction.deactivate(now);
        return MemberRestrictionResponse.from(memberRestriction);
    }

    @Override
    public List<MemberRestrictionResponse> getMemberRestrictions(
            AuthenticatedMember authenticatedMember,
            UUID memberId
    ) {
        RoleGuard.requireAdmin(authenticatedMember);
        memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);

        return memberRestrictionRepository.findAllByMemberId(memberId).stream()
                .map(MemberRestrictionResponse::from)
                .toList();
    }

    public MemberRestriction getActiveLoginRestriction(UUID memberId, LocalDateTime now) {
        return memberRestrictionRepository.findActiveRestriction(memberId, RestrictionType.LOGIN_BAN, now)
                .orElse(null);
    }

    private void validateCreateRequest(CreateMemberRestrictionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Create member restriction request body is required.");
        }
        if (request.memberId() == null) {
            throw new IllegalArgumentException("memberId is required.");
        }
        if (request.restrictionType() == null) {
            throw new IllegalArgumentException("restrictionType is required.");
        }
    }
}
