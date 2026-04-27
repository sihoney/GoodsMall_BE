package com.example.member.application.service;

import com.example.member.application.dto.command.ChangePasswordCommand;
import com.example.member.application.dto.command.CreateMemberCommand;
import com.example.member.application.dto.command.UpdateMemberCommand;
import com.example.member.application.dto.query.GetMemberQuery;
import com.example.member.application.dto.result.ChangePasswordResult;
import com.example.member.application.dto.result.CreateMemberResult;
import com.example.member.application.dto.result.MemberResult;
import com.example.member.application.port.in.MemberUsecase;
import com.example.member.application.port.out.MemberEventPort;
import com.example.member.application.port.out.MemberPersistencePort;
import com.example.member.application.port.out.ProfileImageUrlPort;
import com.example.member.common.exception.DuplicateMemberEmailException;
import com.example.member.common.exception.InvalidCurrentPasswordException;
import com.example.member.common.exception.MemberNotFoundException;
import com.example.member.config.MemberSignupProperties;
import com.example.member.domain.entity.Member;
import com.example.member.domain.enumtype.MemberStatus;
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

    private final MemberPersistencePort memberPersistencePort;
    private final PasswordEncoder passwordEncoder;
    private final MemberEventPort memberEventPort;
    private final ProfileImageUrlPort profileImageUrlPort;
    private final EmailVerificationService emailVerificationService;
    private final MemberSignupProperties memberSignupProperties;

    @Transactional
    @Override
    public CreateMemberResult createMember(CreateMemberCommand command) {
        validateCreateCommand(command);

        String email = normalizeRequired(command.email(), "email");
        if (memberPersistencePort.existsByEmail(email)) {
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

        Member savedMember = memberPersistencePort.save(member);
        if (memberSignupProperties.requireEmailVerification()) {
            emailVerificationService.createSignupVerification(savedMember);
        }
        memberEventPort.publishMemberSignedUp(savedMember);

        return toCreateMemberResult(savedMember);
    }

    @Override
    public MemberResult getMember(GetMemberQuery query) {
        validateGetMemberQuery(query);
        return toMemberResult(getMemberEntity(query.memberId()));
    }

    @Override
    public MemberResult getCurrentMember(GetMemberQuery query) {
        validateGetMemberQuery(query);
        return toMemberResult(getMemberEntity(query.memberId()));
    }

    @Transactional
    @Override
    public MemberResult updateMember(UpdateMemberCommand command) {
        validateUpdateCommand(command);

        Member member = getMemberEntity(command.memberId());
        member.changeNickname(
                normalizeRequired(command.nickname(), "nickname"),
                LocalDateTime.now()
        );
        member.updateProfile(
                normalizeNullable(command.phone()),
                normalizeNullable(command.address()),
                command.profileImageKey() == null
                        ? member.getProfileImageKey()
                        : normalizeProfileImageKey(command.profileImageKey()),
                LocalDateTime.now()
        );

        return toMemberResult(member);
    }

    @Transactional
    @Override
    public MemberResult updateCurrentMember(UpdateMemberCommand command) {
        validateUpdateCommand(command);
        return updateMember(command);
    }

    @Transactional
    @Override
    public ChangePasswordResult changeCurrentMemberPassword(ChangePasswordCommand command) {
        validateChangePasswordCommand(command);

        Member member = getMemberEntity(command.memberId());
        String currentPassword = normalizeRequired(command.currentPassword(), "currentPassword");
        if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
            throw new InvalidCurrentPasswordException();
        }

        String newPassword = normalizeRequired(command.newPassword(), "newPassword");
        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("새 비밀번호는 8자 이상이어야 합니다.");
        }

        member.changePassword(passwordEncoder.encode(newPassword), LocalDateTime.now());
        return new ChangePasswordResult("비밀번호가 변경되었습니다.");
    }

    private Member getMemberEntity(UUID memberId) {
        return memberPersistencePort.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
    }

    private void validateCreateCommand(CreateMemberCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("회원 생성 요청 본문은 필수입니다.");
        }
    }

    private void validateUpdateCommand(UpdateMemberCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("회원 수정 요청 본문은 필수입니다.");
        }
        if (command.memberId() == null) {
            throw new IllegalArgumentException("memberId는 필수입니다.");
        }
    }

    private void validateGetMemberQuery(GetMemberQuery query) {
        if (query == null || query.memberId() == null) {
            throw new IllegalArgumentException("memberId는 필수입니다.");
        }
    }

    private void validateChangePasswordCommand(ChangePasswordCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("비밀번호 변경 요청 본문은 필수입니다.");
        }
        if (command.memberId() == null) {
            throw new IllegalArgumentException("memberId는 필수입니다.");
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
        if (!profileImageUrlPort.isSupportedKey(normalized)) {
            throw new IllegalArgumentException("profileImageKey가 올바르지 않습니다.");
        }
        return normalized;
    }

    private String resolveProfileImageUrl(Member member) {
        return profileImageUrlPort.resolve(member.getProfileImageKey());
    }

    private CreateMemberResult toCreateMemberResult(Member member) {
        return new CreateMemberResult(
                member.getMemberId(),
                member.getNickname(),
                resolveProfileImageUrl(member),
                member.getRole(),
                member.getStatus(),
                member.getCreatedAt()
        );
    }

    private MemberResult toMemberResult(Member member) {
        return new MemberResult(
                member.getMemberId(),
                member.getEmail(),
                member.getNickname(),
                member.getPhone(),
                member.getAddress(),
                resolveProfileImageUrl(member),
                member.getRole(),
                member.getStatus(),
                member.getCreatedAt(),
                member.getUpdatedAt()
        );
    }
}
