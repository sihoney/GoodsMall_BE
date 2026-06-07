package com.example.member.member.application.service;

import com.example.member.auth.application.dto.command.ChangePasswordCommand;
import com.example.member.auth.application.port.in.AuthSessionUsecase;
import com.example.member.auth.application.port.out.MemberOauthAccountPersistencePort;
import com.example.member.auth.application.dto.result.ChangePasswordResult;
import com.example.member.auth.domain.entity.MemberOauthAccount;
import com.example.member.common.exception.BusinessException;
import com.example.member.member.application.dto.command.CreateMemberCommand;
import com.example.member.member.application.dto.command.UpdateMemberCommand;
import com.example.member.member.application.dto.command.WithdrawMemberCommand;
import com.example.member.member.application.dto.query.GetMemberQuery;
import com.example.member.member.application.dto.result.CreateMemberResult;
import com.example.member.member.application.dto.result.MemberResult;
import com.example.member.member.application.dto.result.WithdrawMemberResult;
import com.example.member.member.application.port.in.MemberUsecase;
import com.example.member.member.application.port.out.MemberEventPort;
import com.example.member.member.application.port.out.MemberPersistencePort;
import com.example.member.member.application.port.out.MemberWithdrawalCheckPort;
import com.example.member.member.application.port.out.ProfileImageUrlPort;
import com.example.member.member.config.MemberSignupProperties;
import com.example.member.member.domain.entity.Member;
import com.example.member.member.domain.enumtype.MemberStatus;
import com.example.member.member.exception.MemberErrorCode;
import com.example.member.verification.application.service.EmailVerificationService;
import com.todaylunch.common.security.auth.enumtype.MemberRole;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Validated
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService implements MemberUsecase {

    private final MemberPersistencePort memberPersistencePort;
    private final PasswordEncoder passwordEncoder;
    private final MemberEventPort memberEventPort;
    private final MemberWithdrawalCheckPort memberWithdrawalCheckPort;
    private final MemberOauthAccountPersistencePort memberOauthAccountPersistencePort;
    private final ProfileImageUrlPort profileImageUrlPort;
    private final EmailVerificationService emailVerificationService;
    private final MemberSignupProperties memberSignupProperties;
    private final AuthSessionUsecase authSessionUsecase;

    @Transactional
    @Override
    public CreateMemberResult createMember(CreateMemberCommand command) {
        String email = command.email().trim();

        if (memberPersistencePort.existsByEmail(email)) {
            throw new BusinessException(MemberErrorCode.DUPLICATE_MEMBER_EMAIL);
        }

        LocalDateTime now = LocalDateTime.now();
        MemberStatus initialStatus = memberSignupProperties.requireEmailVerification()
                ? MemberStatus.PENDING_VERIFICATION
                : MemberStatus.ACTIVE;

        Member member = Member.create(
                UUID.randomUUID(),
                email,
                passwordEncoder.encode(command.password().trim()),
                command.nickname().trim(),
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
        return toMemberResult(getMemberEntity(query.memberId()));
    }

    @Override
    public MemberResult getCurrentMember(GetMemberQuery query) {
        return toMemberResult(getMemberEntity(query.memberId()));
    }

    @Transactional
    @Override
    public MemberResult updateCurrentMember(UpdateMemberCommand command) {
        Member member = getMemberEntity(command.memberId());

        member.changeNickname(
                command.nickname().trim(),
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
    public ChangePasswordResult changeCurrentMemberPassword(ChangePasswordCommand command) {
        Member member = getMemberEntity(command.memberId());

        String currentPassword = command.currentPassword().trim();
        if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
            throw new BusinessException(MemberErrorCode.INVALID_CURRENT_PASSWORD);
        }

        String newPassword = command.newPassword().trim();
        member.changePassword(passwordEncoder.encode(newPassword), LocalDateTime.now());

        return new ChangePasswordResult("비밀번호가 변경되었습니다.");
    }

    @Transactional
    @Override
    public WithdrawMemberResult withdrawCurrentMember(WithdrawMemberCommand command) {
        Member member = getMemberEntity(command.memberId());
        validateWithdrawableMember(member);

        String authorizationHeader = command.authorizationHeader().trim();
        memberWithdrawalCheckPort.validateWithdrawable(member, authorizationHeader);

        String currentPassword = command.currentPassword().trim();
        if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
            throw new BusinessException(MemberErrorCode.MEMBER_WITHDRAWAL_PASSWORD_INVALID);
        }

        LocalDateTime withdrawnAt = LocalDateTime.now();
        deleteOauthAccounts(member.getMemberId());
        member.withdraw(createWithdrawnEmail(member), withdrawnAt);
        authSessionUsecase.logoutAllSessions(authorizationHeader);

        return new WithdrawMemberResult(
                member.getMemberId(),
                member.getStatus(),
                member.getUpdatedAt(),
                "회원탈퇴가 완료되었습니다."
        );
    }

    private Member getMemberEntity(UUID memberId) {
        return memberPersistencePort.findById(memberId)
                .orElseThrow(() -> new BusinessException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    private void validateWithdrawableMember(Member member) {
        if (member.getRole() == MemberRole.ADMIN) {
            throw new BusinessException(MemberErrorCode.MEMBER_WITHDRAWAL_ADMIN_FORBIDDEN);
        }
        if (member.getStatus() == MemberStatus.WITHDRAWN) {
            throw new BusinessException(MemberErrorCode.MEMBER_ALREADY_WITHDRAWN);
        }
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new BusinessException(MemberErrorCode.MEMBER_WITHDRAWAL_NOT_ACTIVE);
        }
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
            throw new BusinessException(MemberErrorCode.INVALID_PROFILE_IMAGE_KEY);
        }
        return normalized;
    }

    private String resolveProfileImageUrl(Member member) {
        return profileImageUrlPort.resolve(member.getProfileImageKey());
    }

    private String createWithdrawnEmail(Member member) {
        return "withdrawn+" + member.getMemberId() + "@deleted.local";
    }

    private void deleteOauthAccounts(UUID memberId) {
        List<MemberOauthAccount> oauthAccounts = memberOauthAccountPersistencePort.findAllByMemberId(memberId);
        oauthAccounts.forEach(memberOauthAccountPersistencePort::delete);
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
