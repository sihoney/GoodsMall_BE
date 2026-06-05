package com.example.member.member.application.service;

import com.example.member.verification.application.service.EmailVerificationService;

import com.example.member.auth.application.dto.command.ChangePasswordCommand;
import com.example.member.member.application.dto.command.CreateMemberCommand;
import com.example.member.member.application.dto.command.UpdateMemberCommand;
import com.example.member.member.application.dto.command.WithdrawMemberCommand;
import com.example.member.member.application.dto.query.GetMemberQuery;
import com.example.member.auth.application.dto.result.ChangePasswordResult;
import com.example.member.member.application.dto.result.CreateMemberResult;
import com.example.member.member.application.dto.result.MemberResult;
import com.example.member.member.application.dto.result.WithdrawMemberResult;
import com.example.member.auth.application.port.in.AuthSessionUsecase;
import com.example.member.common.exception.BusinessException;
import com.example.member.member.application.port.in.MemberUsecase;
import com.example.member.member.application.port.out.MemberWithdrawalCheckPort;
import com.example.member.member.application.port.out.MemberEventPort;
import com.example.member.auth.application.port.out.MemberOauthAccountPersistencePort;
import com.example.member.member.application.port.out.MemberPersistencePort;
import com.example.member.member.application.port.out.ProfileImageUrlPort;
import com.example.member.member.exception.MemberErrorCode;
import com.example.member.member.config.MemberSignupProperties;
import com.example.member.member.domain.entity.Member;
import com.example.member.auth.domain.entity.MemberOauthAccount;
import com.example.member.member.domain.enumtype.MemberStatus;
import com.todaylunch.common.security.auth.enumtype.MemberRole;
import java.time.LocalDateTime;
import java.util.List;
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
    private final MemberWithdrawalCheckPort memberWithdrawalCheckPort;
    private final MemberOauthAccountPersistencePort memberOauthAccountPersistencePort;
    private final ProfileImageUrlPort profileImageUrlPort;
    private final EmailVerificationService emailVerificationService;
    private final MemberSignupProperties memberSignupProperties;
    private final AuthSessionUsecase authSessionUsecase;

    @Transactional
    @Override
    public CreateMemberResult createMember(CreateMemberCommand command) {
        // [1] 요청 검증
        validateCreateCommand(command);

        // [2] 이메일 정규화
        String email = normalizeRequired(command.email(), "email");

        // [3] 이메일 중복 확인
        if (memberPersistencePort.existsByEmail(email)) {
            throw new BusinessException(MemberErrorCode.DUPLICATE_MEMBER_EMAIL);
        }

        // [4] 초기 상태 결정
        LocalDateTime now = LocalDateTime.now();
        MemberStatus initialStatus = memberSignupProperties.requireEmailVerification()
                ? MemberStatus.PENDING_VERIFICATION
                : MemberStatus.ACTIVE;

        // [5] 회원 생성
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

        // [6] 회원 저장
        Member savedMember = memberPersistencePort.save(member);

        // [7] 이메일 인증 생성
        if (memberSignupProperties.requireEmailVerification()) {
            emailVerificationService.createSignupVerification(savedMember);
        }

        // [8] 가입 이벤트 발행
        memberEventPort.publishMemberSignedUp(savedMember);

        // [9] 결과 반환
        return toCreateMemberResult(savedMember);
    }

    @Override
    public MemberResult getMember(GetMemberQuery query) {
        // [1] 조회 조건 검증
        validateGetMemberQuery(query);

        // [2] 회원 조회
        return toMemberResult(getMemberEntity(query.memberId()));
    }

    @Override
    public MemberResult getCurrentMember(GetMemberQuery query) {
        // [1] 조회 조건 검증
        validateGetMemberQuery(query);

        // [2] 회원 조회
        return toMemberResult(getMemberEntity(query.memberId()));
    }

    @Transactional
    @Override
    public MemberResult updateCurrentMember(UpdateMemberCommand command) {
        // [1] 요청 검증
        validateUpdateCommand(command);

        // [2] 회원 조회
        Member member = getMemberEntity(command.memberId());

        // [3] 닉네임 변경
        member.changeNickname(
                normalizeRequired(command.nickname(), "nickname"),
                LocalDateTime.now()
        );

        // [4] 프로필 변경
        member.updateProfile(
                normalizeNullable(command.phone()),
                normalizeNullable(command.address()),
                command.profileImageKey() == null
                        ? member.getProfileImageKey()
                        : normalizeProfileImageKey(command.profileImageKey()),
                LocalDateTime.now()
        );

        // [5] 결과 반환
        return toMemberResult(member);
    }

    @Transactional
    @Override
    public ChangePasswordResult changeCurrentMemberPassword(ChangePasswordCommand command) {
        // [1] 요청 검증
        validateChangePasswordCommand(command);

        // [2] 회원 조회
        Member member = getMemberEntity(command.memberId());

        // [3] 현재 비밀번호 검증
        String currentPassword = normalizeRequired(command.currentPassword(), "currentPassword");
        if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
            throw new BusinessException(MemberErrorCode.INVALID_CURRENT_PASSWORD);
        }

        // [4] 새 비밀번호 검증
        String newPassword = normalizeRequired(command.newPassword(), "newPassword");
        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("새 비밀번호는 8자 이상이어야 합니다.");
        }

        // [5] 비밀번호 변경
        member.changePassword(passwordEncoder.encode(newPassword), LocalDateTime.now());

        // [6] 결과 반환
        return new ChangePasswordResult("비밀번호가 변경되었습니다.");
    }

    @Transactional
    @Override
    public WithdrawMemberResult withdrawCurrentMember(WithdrawMemberCommand command) {
        // [1] 요청 검증
        validateWithdrawCommand(command);

        // [2] 회원 조회
        Member member = getMemberEntity(command.memberId());

        // [3] 내부 탈퇴 조건 검증
        validateWithdrawableMember(member);

        // [4] 외부 탈퇴 조건 검증
        memberWithdrawalCheckPort.validateWithdrawable(
                member,
                normalizeRequired(command.authorizationHeader(), "authorizationHeader")
        );

        // [5] 현재 비밀번호 검증
        String currentPassword = normalizeRequired(command.currentPassword(), "currentPassword");
        if (!passwordEncoder.matches(currentPassword, member.getPassword())) {
            throw new BusinessException(MemberErrorCode.MEMBER_WITHDRAWAL_PASSWORD_INVALID);
        }

        // [6] 탈퇴 시각 생성
        LocalDateTime withdrawnAt = LocalDateTime.now();

        // [7] OAuth 계정 삭제
        deleteOauthAccounts(member.getMemberId());

        // [8] 회원 탈퇴 처리
        member.withdraw(createWithdrawnEmail(member), withdrawnAt);

        // [9] 전체 세션 로그아웃
        authSessionUsecase.logoutAllSessions(normalizeRequired(command.authorizationHeader(), "authorizationHeader"));

        // [10] 결과 반환
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
            throw new BusinessException(MemberErrorCode.MEMBER_WITHDRAWAL_ADMIN_FORBIDDEN);
        }
        if (member.getStatus() == MemberStatus.WITHDRAWN) {
            throw new BusinessException(MemberErrorCode.MEMBER_ALREADY_WITHDRAWN);
        }
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new BusinessException(MemberErrorCode.MEMBER_WITHDRAWAL_NOT_ACTIVE);
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
