package com.example.member.member.application.service;

import com.example.member.verification.application.service.EmailVerificationService;

import com.example.member.auth.application.service.KakaoOAuthService;

import com.example.member.auth.application.dto.command.ChangePasswordCommand;
import com.example.member.member.application.dto.command.CreateMemberCommand;
import com.example.member.member.application.dto.command.UpdateMemberCommand;
import com.example.member.member.application.dto.command.WithdrawMemberCommand;
import com.example.member.member.application.dto.query.GetMemberQuery;
import com.example.member.auth.application.dto.result.ChangePasswordResult;
import com.example.member.member.application.dto.result.CreateMemberResult;
import com.example.member.member.application.dto.result.MemberResult;
import com.example.member.member.application.dto.result.WithdrawMemberResult;
import com.example.member.auth.application.port.in.AuthUsecase;
import com.example.member.member.application.port.in.MemberUsecase;
import com.example.member.member.application.port.out.MemberWithdrawalCheckPort;
import com.example.member.member.application.port.out.MemberEventPort;
import com.example.member.auth.application.port.out.MemberOauthAccountPersistencePort;
import com.example.member.member.application.port.out.MemberPersistencePort;
import com.example.member.member.application.port.out.ProfileImageUrlPort;
import com.example.member.member.exception.DuplicateMemberEmailException;
import com.example.member.member.exception.InvalidCurrentPasswordException;
import com.example.member.member.exception.MemberWithdrawalException;
import com.example.member.member.exception.MemberNotFoundException;
import com.example.member.common.config.MemberSignupProperties;
import com.example.member.member.domain.entity.Member;
import com.example.member.auth.domain.entity.MemberOauthAccount;
import com.example.member.member.domain.enumtype.MemberStatus;
import com.todaylunch.common.security.auth.enumtype.MemberRole;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    private final MemberWithdrawalCheckPort memberWithdrawalCheckPort;
    private final MemberOauthAccountPersistencePort memberOauthAccountPersistencePort;
    private final ProfileImageUrlPort profileImageUrlPort;
    private final EmailVerificationService emailVerificationService;
    private final KakaoOAuthService kakaoOAuthService;
    private final MemberSignupProperties memberSignupProperties;
    private final AuthUsecase authUsecase;

    @Transactional
    @Override
    public CreateMemberResult createMember(CreateMemberCommand command) {
        // 1. command == null
        validateCreateCommand(command);

        // 2. 필수값 정규화
        String email = normalizeRequired(command.email(), "email");

        // 3. 중복 이메일 확인
        if (memberPersistencePort.existsByEmail(email)) {
            throw new DuplicateMemberEmailException();
        }

        // 4. 이메일 검증 on/off에 따른 status 값 초기화
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

        // 5. 영속 저장
        Member savedMember = memberPersistencePort.save(member);

        // 6. 대기 중인 카카오 계정 연동
        linkPendingKakaoAccountIfPresent(savedMember.getMemberId(), command.kakaoLinkToken());

        // 7. 이메일 검증 on/off에 따른 이메일 발송
        if (memberSignupProperties.requireEmailVerification()) {
            emailVerificationService.createSignupVerification(savedMember);
        }

        // 8. 회원 생성 이벤트 발행 (payment 서비스 지갑 생성)
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

    @Transactional
    @Override
    public WithdrawMemberResult withdrawCurrentMember(WithdrawMemberCommand command) {
        validateWithdrawCommand(command);

        Member member = getMemberEntity(command.memberId());
        validateWithdrawableMember(member);
        memberWithdrawalCheckPort.validateWithdrawable(
                member,
                normalizeRequired(command.authorizationHeader(), "authorizationHeader")
        );

        String currentPassword = normalizeRequired(command.currentPassword(), "currentPassword");
        if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
            throw new MemberWithdrawalException(
                    "MEMBER_WITHDRAWAL_PASSWORD_INVALID",
                    HttpStatus.BAD_REQUEST,
                    "현재 비밀번호가 올바르지 않습니다."
            );
        }

        LocalDateTime withdrawnAt = LocalDateTime.now();
        deleteOauthAccounts(member.getMemberId());
        member.withdraw(createWithdrawnEmail(member), withdrawnAt);
        authUsecase.logoutAllSessions(normalizeRequired(command.authorizationHeader(), "authorizationHeader"));

        return new WithdrawMemberResult(
                member.getMemberId(),
                member.getStatus(),
                member.getUpdatedAt(),
                "회원탈퇴가 완료되었습니다."
        );
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

    private void validateWithdrawCommand(WithdrawMemberCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("회원탈퇴 요청 본문은 필수입니다.");
        }
        if (command.memberId() == null) {
            throw new IllegalArgumentException("memberId는 필수입니다.");
        }
    }

    private void validateWithdrawableMember(Member member) {
        if (member.getRole() == MemberRole.ADMIN) {
            throw new MemberWithdrawalException(
                    "MEMBER_WITHDRAWAL_ADMIN_FORBIDDEN",
                    HttpStatus.FORBIDDEN,
                    "관리자 계정은 셀프 탈퇴를 지원하지 않습니다."
            );
        }
        if (member.getStatus() == MemberStatus.WITHDRAWN) {
            throw new MemberWithdrawalException(
                    "MEMBER_ALREADY_WITHDRAWN",
                    HttpStatus.CONFLICT,
                    "이미 탈퇴한 회원입니다."
            );
        }
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new MemberWithdrawalException(
                    "MEMBER_WITHDRAWAL_NOT_ACTIVE",
                    HttpStatus.CONFLICT,
                    "ACTIVE 상태 회원만 탈퇴할 수 있습니다."
            );
        }
    }

    private String normalizeRequired(String value, String fieldName) {
        String normalized = normalizeNullable(value);
        if (normalized == null) {
            throw new IllegalArgumentException(fieldName + "는(은) 필수입니다.");
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

    private String createWithdrawnEmail(Member member) {
        return "withdrawn+" + member.getMemberId() + "@deleted.local";
    }

    private void deleteOauthAccounts(UUID memberId) {
        List<MemberOauthAccount> oauthAccounts = memberOauthAccountPersistencePort.findAllByMemberId(memberId);
        oauthAccounts.forEach(memberOauthAccountPersistencePort::delete);
    }

    private void linkPendingKakaoAccountIfPresent(UUID memberId, String kakaoLinkToken) {
        String normalizedLinkToken = normalizeNullable(kakaoLinkToken);
        if (normalizedLinkToken == null) {
            return;
        }

        kakaoOAuthService.linkPendingSignupMember(memberId, normalizedLinkToken);
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
