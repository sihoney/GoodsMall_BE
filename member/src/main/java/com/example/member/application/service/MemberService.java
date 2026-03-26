package com.example.member.application.service;

import com.example.member.application.dto.MemberCreateCommand;
import com.example.member.application.usecase.MemberUsecase;
import com.example.member.domain.entity.Member;
import com.example.member.domain.enumtype.MemberRole;
import com.example.member.domain.enumtype.MemberStatus;
import com.example.member.domain.exception.DuplicateMemberEmailException;
import com.example.member.domain.exception.MemberNotFoundException;
import com.example.member.infrastructure.repository.MemberRepository;
import com.example.member.presentation.dto.CreateMemberResponse;
import com.example.member.presentation.dto.CreateMemberRequest;
import com.example.member.presentation.dto.MemberResponse;
import com.example.member.presentation.dto.UpdateMemberRequest;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// TODO: usecase(interface) 분리

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService implements MemberUsecase {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    // 1. 회원가입
    @Transactional
    @Override
    public CreateMemberResponse createMember(CreateMemberRequest request) {
        validateCreateRequest(request);
        MemberCreateCommand command = MemberCreateCommand.from(request);

        // 이메일 중복 검사
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
                request.role() == null ? MemberRole.USER : request.role(), // 기본 역할(MemberRole.USER) 저장
                MemberStatus.ACTIVE, // 상태 저장
                now,
                now
        );

        // TODO: wallet 자동 생성 붙이기

        return CreateMemberResponse.from(memberRepository.save(member)); // email, phone, address, password 제외
    }

    // 회원정보 조회
    public MemberResponse getMember(UUID memberId) {
        return MemberResponse.from(getMemberEntity(memberId));
    }

    // 인증된 회원정보 조회
    @Override
    public MemberResponse getCurrentMember(UUID memberId) {
        return MemberResponse.from(getMemberEntity(memberId));
    }

    // 회원정보 수정
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
    public MemberResponse updateCurrentMember(UUID memberId, UpdateMemberRequest request) {
        return updateMember(memberId, request);
    }

    private Member getMemberEntity(UUID memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
    }

    private void validateCreateRequest(CreateMemberRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("회원가입 요청 본문이 필요합니다.");
        }
    }

    private void validateUpdateRequest(UpdateMemberRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("회원 수정 요청 본문이 필요합니다.");
        }
    }

    private String normalizeRequired(String value, String fieldName) {
        String normalized = normalizeNullable(value);
        if (normalized == null) {
            throw new IllegalArgumentException(fieldName + " 값은 필수입니다.");
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
