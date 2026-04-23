package com.example.member.application.service;

import com.example.member.application.dto.MemberCreateCommand;
import com.example.member.application.event.MemberEventPublisher;
import com.example.member.application.support.ProfileImageUrlResolver;
import com.example.member.application.usecase.MemberUsecase;
import com.example.member.common.exception.DuplicateMemberEmailException;
import com.example.member.common.exception.MemberNotFoundException;
import com.example.member.config.MemberSignupProperties;
import com.example.member.domain.entity.Member;
import com.example.member.domain.enumtype.MemberStatus;
import com.example.member.infrastructure.repository.MemberRepository;
import com.example.member.presentation.dto.CreateMemberRequest;
import com.example.member.presentation.dto.CreateMemberResponse;
import com.example.member.presentation.dto.MemberResponse;
import com.example.member.presentation.dto.UpdateMemberRequest;
import com.todaylunch.common.security.auth.enumtype.MemberRole;
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
    private final ProfileImageUrlResolver profileImageUrlResolver;
    private final EmailVerificationService emailVerificationService;
    private final MemberSignupProperties memberSignupProperties;

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
        MemberStatus initialStatus = memberSignupProperties.requireEmailVerification()
                ? MemberStatus.PENDING_VERIFICATION
                : MemberStatus.ACTIVE;

        Member member = Member.create(
                UUID.randomUUID(),
                email,
                passwordEncoder.encode(normalizeRequired(command.password(), "password")),
                normalizeRequired(command.nickname(), "nickname"),
                normalizeNullable(command.phone()),
                normalizeNullable(command.address()),
                normalizeProfileImageKey(command.profileImageKey()),
                command.role() == null ? MemberRole.USER : command.role(),
                initialStatus,
                now,
                now
        );

        Member savedMember = memberRepository.save(member);
        if (memberSignupProperties.requireEmailVerification()) {
            emailVerificationService.createSignupVerification(savedMember);
        }
        memberEventPublisher.publishMemberSignedUp(savedMember);

        return CreateMemberResponse.from(savedMember, resolveProfileImageUrl(savedMember));
    }

    @Override
    public MemberResponse getMember(UUID memberId) {
        Member member = getMemberEntity(memberId);
        return MemberResponse.from(member, resolveProfileImageUrl(member));
    }

    @Override
    public MemberResponse getCurrentMember(UUID memberId) {
        Member member = getMemberEntity(memberId);
        return MemberResponse.from(member, resolveProfileImageUrl(member));
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
                request.profileImageKey() == null
                        ? member.getProfileImageKey()
                        : normalizeProfileImageKey(request.profileImageKey()),
                LocalDateTime.now()
        );

        return MemberResponse.from(member, resolveProfileImageUrl(member));
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
            throw new IllegalArgumentException("회원 생성 요청 본문은 필수입니다.");
        }
    }

    private void validateUpdateRequest(UpdateMemberRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("회원 수정 요청 본문은 필수입니다.");
        }
    }

    private String normalizeRequired(String value, String fieldName) {
        String normalized = normalizeNullable(value);
        if (normalized == null) {
            throw new IllegalArgumentException(fieldName + "은(는) 필수입니다.");
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

    private String normalizeProfileImageKey(String profileImageKey) {
        String normalized = normalizeNullable(profileImageKey);
        if (normalized == null) {
            return null;
        }
        if (!profileImageUrlResolver.isSupportedKey(normalized)) {
            throw new IllegalArgumentException("profileImageKey가 올바르지 않습니다.");
        }
        return normalized;
    }

    private String resolveProfileImageUrl(Member member) {
        return profileImageUrlResolver.resolve(member.getProfileImageKey());
    }
}
