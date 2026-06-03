package com.example.member.auth.application.service.oauth;

import com.example.member.auth.application.dto.OAuthUserProfile;
import com.example.member.auth.application.dto.result.AuthTokenResult;
import com.example.member.auth.application.dto.result.OAuthResult;
import com.example.member.auth.application.port.in.AuthLoginUsecase;
import com.example.member.auth.application.port.out.MemberOauthAccountPersistencePort;
import com.example.member.auth.application.port.out.MemberOauthEventPort;
import com.example.member.auth.domain.entity.MemberOauthAccount;
import com.example.member.auth.domain.enumtype.OAuthProvider;
import com.example.member.auth.exception.InvalidLoginException;
import com.example.member.common.application.dto.AuthSessionMetadata;
import com.example.member.member.application.port.out.MemberEventPort;
import com.example.member.member.application.port.out.MemberPersistencePort;
import com.example.member.member.domain.entity.Member;
import com.example.member.member.domain.enumtype.MemberStatus;
import com.todaylunch.common.security.auth.enumtype.MemberRole;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OAuthLoginSignupService {

    private final MemberPersistencePort memberPersistencePort;
    private final MemberOauthAccountPersistencePort memberOauthAccountPersistencePort;
    private final MemberEventPort memberEventPort;
    private final MemberOauthEventPort memberOauthEventPort;
    private final AuthLoginUsecase authLoginUsecase;
    private final PasswordEncoder passwordEncoder;

    public OAuthResult loginOrSignupByProfile(OAuthUserProfile profile, AuthSessionMetadata metadata) {
        // [1] Provider 정보 추출
        OAuthProvider provider = requireProvider(profile);
        String providerUserId = normalizeRequired(profile.providerUserId(), "providerUserId");

        // [2] Provider Profile 추출
        String email = normalizeNullable(profile.email());
        String nickname = normalizeNullable(profile.nickname());

        // [3] OAuth 식별자 매핑 조회
        return memberOauthAccountPersistencePort.findByProviderAndProviderUserId(provider, providerUserId)
                .map(linkedAccount -> {
                    // [4] 회원 조회
                    Member member = memberPersistencePort.findById(linkedAccount.getMemberId())
                            .orElseThrow(InvalidLoginException::new);

                    // [5] 회원 상태 검증
                    if (!member.isActive()) {
                        throw new InvalidLoginException();
                    }

                    // [6] 인증 토큰 발급
                    AuthTokenResult loginResponse = authLoginUsecase.loginAuthenticatedMember(member, metadata);

                    // [7] 로그인 결과 반환
                    return OAuthResult.success(
                            provider.name(),
                            providerUserId,
                            email,
                            nickname,
                            loginResponse.accessToken(),
                            loginResponse.refreshToken(),
                            loginResponse.sessionId(),
                            loginResponse.tokenType(),
                            loginResponse.accessTokenExpiresIn(),
                            loginResponse.refreshTokenExpiresIn()
                    );
                })
                // [8] 신규 OAuth 회원 생성
                .orElseGet(() -> signupAndLogin(profile, provider, providerUserId, email, nickname, metadata));
    }

    private OAuthResult signupAndLogin(
            OAuthUserProfile profile,
            OAuthProvider provider,
            String providerUserId,
            String email,
            String nickname,
            AuthSessionMetadata metadata
    ) {
        // [1] Email 제공 검증
        String requiredEmail = normalizeRequiredEmailForSignup(email);

        // [2] Email 중복 검증
        if (memberPersistencePort.existsByEmail(requiredEmail)) {
            throw new IllegalStateException("OAUTH_EMAIL_ALREADY_REGISTERED");
        }

        // [3] 생성 시각 생성
        LocalDateTime now = LocalDateTime.now();

        // [4] 회원 생성
        Member member = Member.create(
                UUID.randomUUID(),
                requiredEmail,
                passwordEncoder.encode(UUID.randomUUID().toString()),
                resolveNickname(provider, nickname, providerUserId),
                null,
                null,
                null,
                MemberRole.USER,
                MemberStatus.ACTIVE,
                now,
                now
        );

        // [5] 회원 저장
        Member savedMember = memberPersistencePort.save(member);

        // [6] OAuth 식별자 매핑 저장
        saveOauthAccount(savedMember.getMemberId(), profile, providerUserId, requiredEmail, nickname, now);

        // [7] 가입 이벤트 발행
        memberEventPort.publishMemberSignedUp(savedMember);

        // [8] 인증 토큰 발급
        AuthTokenResult loginResponse = authLoginUsecase.loginAuthenticatedMember(savedMember, metadata);

        // [9] 로그인 결과 반환
        return OAuthResult.success(
                provider.name(),
                providerUserId,
                requiredEmail,
                nickname,
                loginResponse.accessToken(),
                loginResponse.refreshToken(),
                loginResponse.sessionId(),
                loginResponse.tokenType(),
                loginResponse.accessTokenExpiresIn(),
                loginResponse.refreshTokenExpiresIn()
        );
    }

    private void saveOauthAccount(
            UUID memberId,
            OAuthUserProfile profile,
            String providerUserId,
            String email,
            String nickname,
            LocalDateTime now
    ) {
        memberOauthAccountPersistencePort.save(MemberOauthAccount.create(
                UUID.randomUUID(),
                memberId,
                profile.provider(),
                providerUserId,
                email,
                nickname,
                now,
                now
        ));

        memberOauthEventPort.publishMemberOauthLinked(
                memberId,
                profile.provider().name(),
                providerUserId,
                email,
                nickname,
                now
        );
    }

    private OAuthProvider requireProvider(OAuthUserProfile profile) {
        if (profile == null || profile.provider() == null) {
            throw new IllegalArgumentException("provider is required");
        }
        return profile.provider();
    }

    private String normalizeRequiredEmailForSignup(String email) {
        String normalized = normalizeNullable(email);
        if (normalized == null) {
            throw new IllegalStateException("OAUTH_EMAIL_REQUIRED");
        }
        return normalized;
    }

    private String resolveNickname(OAuthProvider provider, String nickname, String providerUserId) {
        String normalized = normalizeNullable(nickname);
        if (normalized != null) {
            return normalized;
        }
        return provider.name().toLowerCase(Locale.ROOT) + "-"
                + providerUserId.substring(0, Math.min(providerUserId.length(), 12));
    }

    private String normalizeRequired(String value, String fieldName) {
        String normalized = normalizeNullable(value);
        if (normalized == null) {
            throw new IllegalArgumentException(fieldName + " is required");
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
