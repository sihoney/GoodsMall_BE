package com.example.member.application.service;

import com.example.member.application.dto.MemberCreateCommand;
import com.example.member.application.event.MemberEventPublisher;
import com.example.member.application.usecase.MemberUsecase;
import com.example.member.common.exception.DuplicateMemberEmailException;
import com.example.member.common.exception.MemberNotFoundException;
import com.example.member.domain.entity.Member;
import com.example.member.domain.enumtype.MemberRole;
import com.example.member.domain.enumtype.MemberStatus;
import com.example.member.infrastructure.repository.MemberRepository;
import com.example.member.presentation.dto.CreateMemberRequest;
import com.example.member.presentation.dto.CreateMemberResponse;
import com.example.member.presentation.dto.MemberResponse;
import com.example.member.presentation.dto.UpdateMemberRequest;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService implements MemberUsecase {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberEventPublisher memberEventPublisher;

    @Transactional
    @Override
    public CreateMemberResponse createMember(CreateMemberRequest request) {
        validateCreateRequest(request);
        MemberCreateCommand command = MemberCreateCommand.from(request);

        String email = normalizeRequired(command.email(), "email");
        if (memberRepository.existsByEmail(email)) {
            throw new DuplicateMemberEmailException();
        }

        LocalDateTime now = LocalDateTime.now();
        Member member = Member.create(
                UUID.randomUUID(),
                email,
                passwordEncoder.encode(normalizeRequired(command.password(), "password")),
                normalizeRequired(command.nickname(), "nickname"),
                normalizeNullable(command.phone()),
                normalizeNullable(command.address()),
                normalizeNullable(command.profileImageUrl()),
                command.role() == null ? MemberRole.USER : command.role(),
                MemberStatus.ACTIVE,
                now,
                now
        );

        Member savedMember = memberRepository.save(member);
        memberEventPublisher.publishMemberSignedUp(savedMember); // 회원 가입 이벤트 발행
        return CreateMemberResponse.from(savedMember);
    }

    @Override
    public MemberResponse getMember(UUID memberId) {
        return MemberResponse.from(getMemberEntity(memberId));
    }

    @Override
    public MemberResponse getCurrentMember(UUID memberId) {
        return MemberResponse.from(getMemberEntity(memberId));
    }

    @Transactional
    @Override
    public MemberResponse updateMember(UUID memberId, UpdateMemberRequest request) {
        validateUpdateRequest(request);

        Member member = getMemberEntity(memberId);
        String email = normalizeRequired(request.email(), "email");
        if (memberRepository.existsByEmailAndMemberIdNot(email, memberId)) {
            throw new DuplicateMemberEmailException();
        }

        member.updateAccount(
                email,
                passwordEncoder.encode(normalizeRequired(request.password(), "password")),
                normalizeRequired(request.nickname(), "nickname"),
                normalizeNullable(request.phone()),
                normalizeNullable(request.address()),
                normalizeNullable(request.profileImageUrl()),
                LocalDateTime.now()
        );

        return MemberResponse.from(member);
    }

    @Transactional
    @Override
    public MemberResponse updateCurrentMember(UUID memberId, UpdateMemberRequest request) {
        return updateMember(memberId, request);
    }

    private Member getMemberEntity(UUID memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
    }

    private void validateCreateRequest(CreateMemberRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Create member request body is required.");
        }
    }

    private void validateUpdateRequest(UpdateMemberRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Update member request body is required.");
        }
    }

    private String normalizeRequired(String value, String fieldName) {
        String normalized = normalizeNullable(value);
        if (normalized == null) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return normalized;
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}
