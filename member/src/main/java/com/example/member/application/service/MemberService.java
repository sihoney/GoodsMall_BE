package com.example.member.application.service;

import com.example.member.application.dto.MemberCreateCommand;
import com.example.member.application.event.MemberEventPublisher;
import com.example.member.application.support.ProfileImageUrlResolver;
import com.example.member.application.usecase.MemberUsecase;
import com.example.member.common.exception.DuplicateMemberEmailException;
import com.example.member.common.exception.InvalidCurrentPasswordException;
import com.example.member.common.exception.MemberNotFoundException;
import com.example.member.config.MemberSignupProperties;
import com.example.member.domain.entity.Member;
import com.example.member.domain.enumtype.MemberStatus;
import com.example.member.infrastructure.repository.MemberRepository;
import com.example.member.presentation.dto.ChangePasswordRequest;
import com.example.member.presentation.dto.ChangePasswordResponse;
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
        LocalDateTime now = LocalDateTime.now();

        member.changeNickname(
                normalizeRequired(request.nickname(), "nickname"),
                now
        );
        member.updateProfile(
                normalizeNullable(request.phone()),
                normalizeNullable(request.address()),
                request.profileImageKey() == null
                        ? member.getProfileImageKey()
                        : normalizeProfileImageKey(request.profileImageKey()),
                now
        );

        return MemberResponse.from(member, resolveProfileImageUrl(member));
    }

    @Transactional
    @Override
    public MemberResponse updateCurrentMember(UUID memberId, UpdateMemberRequest request) {
        return updateMember(memberId, request);
    }

    @Transactional
    @Override
    public ChangePasswordResponse changeCurrentMemberPassword(UUID memberId, ChangePasswordRequest request) {
        validateChangePasswordRequest(request);

        Member member = getMemberEntity(memberId);
        String currentPassword = normalizeRequired(request.currentPassword(), "currentPassword");
        if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
            throw new InvalidCurrentPasswordException();
        }

        String newPassword = normalizeRequired(request.newPassword(), "newPassword");
        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("새 비밀번호는 8자 이상이어야 합니다.");
        }

        member.changePassword(passwordEncoder.encode(newPassword), LocalDateTime.now());
        return ChangePasswordResponse.success();
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

    private void validateChangePasswordRequest(ChangePasswordRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("비밀번호 변경 요청 본문은 필수입니다.");
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
