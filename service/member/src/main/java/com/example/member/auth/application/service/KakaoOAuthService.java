package com.example.member.auth.application.service;

import com.example.member.auth.application.dto.command.AuthSessionMetadata;
import com.example.member.auth.application.dto.result.KakaoOAuthLinkResult;
import com.example.member.auth.application.dto.result.KakaoOAuthResult;
import com.example.member.auth.application.port.out.MemberOauthEventPort;
import com.example.member.auth.application.port.out.MemberOauthAccountPersistencePort;
import com.example.member.member.application.port.out.MemberPersistencePort;
import com.example.member.auth.exception.InvalidLoginException;
import com.example.member.common.config.KakaoOAuthProperties;
import com.example.member.member.domain.entity.Member;
import com.example.member.auth.domain.entity.MemberOauthAccount;
import com.example.member.auth.domain.enumtype.OAuthProvider;
import com.example.member.auth.infrastructure.kakao.KakaoOAuthClient;
import com.example.member.auth.infrastructure.kakao.KakaoTokenResponse;
import com.example.member.auth.infrastructure.kakao.KakaoUserProfileResponse;
import com.example.member.auth.infrastructure.redis.oauth.KakaoOAuthAuthorizeState;
import com.example.member.auth.infrastructure.redis.oauth.KakaoOAuthAuthorizeStateStore;
import com.example.member.auth.infrastructure.redis.oauth.KakaoOAuthFlowType;
import com.example.member.auth.infrastructure.redis.oauth.KakaoOAuthPendingLink;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KakaoOAuthService {

    private static final OAuthProvider PROVIDER = OAuthProvider.KAKAO;

    private final KakaoOAuthProperties kakaoOAuthProperties;
    private final KakaoOAuthClient kakaoOAuthClient;
    private final KakaoOAuthAuthorizeStateStore kakaoOAuthAuthorizeStateStore;
    private final MemberPersistencePort memberPersistencePort;
    private final MemberOauthAccountPersistencePort memberOauthAccountPersistencePort;
    private final AuthService authService;
    private final MemberOauthEventPort memberOauthEventPort;

    public String createLoginAuthorizeState() {
        String state = UUID.randomUUID().toString();
        kakaoOAuthAuthorizeStateStore.createAuthorizeState(
                new KakaoOAuthAuthorizeState(state, KakaoOAuthFlowType.LOGIN, null, Instant.now()),
                kakaoOAuthProperties.stateTtl()
        );
        return state;
    }

    public String createLinkAuthorizeState(UUID memberId) {
        String state = UUID.randomUUID().toString();
        kakaoOAuthAuthorizeStateStore.createAuthorizeState(
                new KakaoOAuthAuthorizeState(state, KakaoOAuthFlowType.LINK, memberId, Instant.now()),
                kakaoOAuthProperties.stateTtl()
        );
        return state;
    }

    public KakaoOAuthAuthorizeState consumeAuthorizeState(String state) {
        return kakaoOAuthAuthorizeStateStore.consumeAuthorizeState(normalizeRequired(state, "state"))
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않거나 만료된 카카오 OAuth state입니다."));
    }

    public String buildAuthorizeUrl(String state) {
        return kakaoOAuthClient.buildAuthorizeUrl(state);
    }

    @Transactional
    public KakaoOAuthResult loginByCode(String code, AuthSessionMetadata metadata) {
        KakaoUserProfileResponse profile = fetchProfile(code);
        return loginByProfile(profile, metadata);
    }

    @Transactional
    public KakaoOAuthResult linkByCode(UUID memberId, String code) {
        KakaoUserProfileResponse profile = fetchProfile(code);
        return linkByCurrentMember(memberId, profile);
    }

    public String createOAuthResultKey(KakaoOAuthResult result) {
        return kakaoOAuthAuthorizeStateStore.createOAuthResult(result, kakaoOAuthProperties.resultTtl())
                .orElseThrow(() -> new IllegalStateException("카카오 OAuth 결과를 저장하지 못했습니다."));
    }

    public KakaoOAuthResult consumeOAuthResult(String resultKey) {
        return kakaoOAuthAuthorizeStateStore.consumeOAuthResult(normalizeRequired(resultKey, "resultKey"))
                .orElseThrow(() -> new IllegalStateException("카카오 OAuth 결과가 없거나 만료되었습니다."));
    }

    public String getFrontendCallbackUrl() {
        return normalizeRequired(kakaoOAuthProperties.frontendCallbackUrl(), "frontendCallbackUrl");
    }

    public KakaoOAuthResult createErrorResult(KakaoOAuthFlowType flowType, Exception exception) {
        if (exception instanceof IllegalArgumentException) {
            return KakaoOAuthResult.error(
                    "KAKAO_OAUTH_INVALID_REQUEST",
                    "카카오 로그인 요청이 올바르지 않거나 만료되었습니다."
            );
        }

        if (exception instanceof InvalidLoginException) {
            return KakaoOAuthResult.error(
                    flowType == KakaoOAuthFlowType.LINK
                            ? "KAKAO_OAUTH_LINK_MEMBER_INVALID"
                            : "KAKAO_OAUTH_MEMBER_LOGIN_FAILED",
                    flowType == KakaoOAuthFlowType.LINK
                            ? "현재 회원은 카카오 계정 연동을 계속 진행할 수 없습니다."
                            : "연동된 회원 계정으로 로그인할 수 없습니다."
            );
        }

        String message = exception.getMessage();
        if ("KAKAO_TOKEN_EXCHANGE_FAILED".equals(message)) {
            return KakaoOAuthResult.error(
                    "KAKAO_OAUTH_TOKEN_EXCHANGE_FAILED",
                    "카카오 OAuth 토큰 교환에 실패했습니다."
            );
        }
        if ("KAKAO_PROFILE_FETCH_FAILED".equals(message)) {
            return KakaoOAuthResult.error(
                    "KAKAO_OAUTH_PROFILE_FETCH_FAILED",
                    "카카오 사용자 프로필 조회에 실패했습니다."
            );
        }
        if ("KAKAO_ALREADY_LINKED_TO_ANOTHER_MEMBER".equals(message)) {
            return KakaoOAuthResult.error(
                    "KAKAO_OAUTH_ALREADY_LINKED_TO_ANOTHER_MEMBER",
                    "이미 다른 회원에 연동된 카카오 계정입니다."
            );
        }
        if ("KAKAO_ALREADY_LINKED".equals(message)) {
            return KakaoOAuthResult.error(
                    "KAKAO_OAUTH_ALREADY_LINKED",
                    "현재 회원은 이미 카카오 계정을 연동했습니다."
            );
        }

        return KakaoOAuthResult.error(
                "KAKAO_OAUTH_UNKNOWN_ERROR",
                "카카오 로그인 처리 중 알 수 없는 오류가 발생했습니다."
        );
    }

    @Transactional
    public KakaoOAuthLinkResult linkPendingSignupMember(UUID memberId, String linkToken) {
        KakaoOAuthPendingLink pendingLink = consumePendingLink(linkToken);
        linkPendingAccount(memberId, pendingLink);
        return new KakaoOAuthLinkResult(true, PROVIDER.name(), pendingLink.providerUserId());
    }

    @Transactional
    public KakaoOAuthLinkResult linkCurrentMember(UUID memberId, String linkToken) {
        KakaoOAuthPendingLink pendingLink = consumePendingLink(linkToken);
        linkPendingAccount(memberId, pendingLink);
        return new KakaoOAuthLinkResult(true, PROVIDER.name(), pendingLink.providerUserId());
    }

    private KakaoOAuthResult loginByProfile(KakaoUserProfileResponse profile, AuthSessionMetadata metadata) {
        String providerUserId = profile.id().toString();
        String email = kakaoEmail(profile);
        String nickname = kakaoNickname(profile);

        return memberOauthAccountPersistencePort.findByProviderAndProviderUserId(PROVIDER, providerUserId)
                .map(linkedAccount -> {
                    Member member = memberPersistencePort.findById(linkedAccount.getMemberId())
                            .orElseThrow(InvalidLoginException::new);
                    if (!member.isActive()) {
                        throw new InvalidLoginException();
                    }
                    var loginResponse = authService.login(member, metadata);
                    return KakaoOAuthResult.success(
                            PROVIDER.name(),
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
                .orElseGet(() -> createPendingLink(profile));
    }

    private KakaoOAuthResult linkByCurrentMember(UUID memberId, KakaoUserProfileResponse profile) {
        String providerUserId = profile.id().toString();
        String email = kakaoEmail(profile);
        String nickname = kakaoNickname(profile);

        Member member = memberPersistencePort.findById(memberId).orElseThrow(InvalidLoginException::new);
        if (!member.isActive()) {
            throw new InvalidLoginException();
        }

        var existingAccount = memberOauthAccountPersistencePort.findByProviderAndProviderUserId(PROVIDER, providerUserId);
        if (existingAccount.isPresent()) {
            if (!existingAccount.get().getMemberId().equals(memberId)) {
                throw new IllegalStateException("KAKAO_ALREADY_LINKED_TO_ANOTHER_MEMBER");
            }

            return KakaoOAuthResult.success(
                    PROVIDER.name(),
                    providerUserId,
                    email,
                    nickname,
                    null,
                    null,
                    null,
                    null,
                    0L,
                    0L
            );
        }

        if (memberOauthAccountPersistencePort.existsByMemberIdAndProvider(memberId, PROVIDER)) {
            throw new IllegalStateException("KAKAO_ALREADY_LINKED");
        }

        LocalDateTime now = LocalDateTime.now();
        saveLinkedAccount(memberId, providerUserId, email, nickname, now);

        return KakaoOAuthResult.success(
                PROVIDER.name(),
                providerUserId,
                email,
                nickname,
                null,
                null,
                null,
                null,
                0L,
                0L
        );
    }

    private KakaoUserProfileResponse fetchProfile(String code) {
        KakaoTokenResponse tokenResponse = kakaoOAuthClient.exchangeCode(normalizeRequired(code, "code"));
        if (tokenResponse == null || tokenResponse.accessToken() == null || tokenResponse.accessToken().isBlank()) {
            throw new IllegalStateException("KAKAO_TOKEN_EXCHANGE_FAILED");
        }
        KakaoUserProfileResponse profile = kakaoOAuthClient.fetchUserProfile(tokenResponse.accessToken());
        if (profile == null || profile.id() == null) {
            throw new IllegalStateException("KAKAO_PROFILE_FETCH_FAILED");
        }
        return profile;
    }

    private KakaoOAuthResult createPendingLink(KakaoUserProfileResponse profile) {
        String linkToken = UUID.randomUUID().toString();
        String providerUserId = profile.id().toString();
        KakaoOAuthPendingLink pendingLink = new KakaoOAuthPendingLink(
                linkToken,
                providerUserId,
                kakaoEmail(profile),
                kakaoNickname(profile),
                kakaoProfileImageUrl(profile),
                Instant.now()
        );
        kakaoOAuthAuthorizeStateStore.createPendingLink(
                pendingLink,
                kakaoOAuthProperties.pendingLinkTtl()
        );
        return KakaoOAuthResult.linkRequired(
                linkToken,
                PROVIDER.name(),
                providerUserId,
                pendingLink.email(),
                pendingLink.nickname()
        );
    }

    private KakaoOAuthPendingLink consumePendingLink(String linkToken) {
        return kakaoOAuthAuthorizeStateStore
                .consumePendingLink(normalizeRequired(linkToken, "linkToken"))
                .orElseThrow(() -> new IllegalStateException("대기 중인 카카오 연동 토큰을 찾을 수 없습니다."));
    }

    private void linkPendingAccount(UUID memberId, KakaoOAuthPendingLink pendingLink) {
        if (memberOauthAccountPersistencePort.existsByProviderAndProviderUserId(PROVIDER, pendingLink.providerUserId())) {
            throw new IllegalStateException("KAKAO_ALREADY_LINKED_TO_ANOTHER_MEMBER");
        }
        if (memberOauthAccountPersistencePort.existsByMemberIdAndProvider(memberId, PROVIDER)) {
            throw new IllegalStateException("KAKAO_ALREADY_LINKED");
        }
        memberPersistencePort.findById(memberId).orElseThrow(InvalidLoginException::new);

        LocalDateTime now = LocalDateTime.now();
        saveLinkedAccount(
                memberId,
                pendingLink.providerUserId(),
                pendingLink.email(),
                pendingLink.nickname(),
                now
        );
    }

    private void saveLinkedAccount(
            UUID memberId,
            String providerUserId,
            String email,
            String nickname,
            LocalDateTime linkedAt
    ) {
        memberOauthAccountPersistencePort.save(MemberOauthAccount.create(
                UUID.randomUUID(),
                memberId,
                PROVIDER,
                providerUserId,
                email,
                nickname,
                linkedAt,
                linkedAt
        ));

        memberOauthEventPort.publishMemberOauthLinked(
                memberId,
                PROVIDER.name(),
                providerUserId,
                email,
                nickname,
                linkedAt
        );
    }

    private String kakaoEmail(KakaoUserProfileResponse profile) {
        return profile.kakaoAccount() == null ? null : profile.kakaoAccount().email();
    }

    private String kakaoNickname(KakaoUserProfileResponse profile) {
        return profile.kakaoAccount() == null || profile.kakaoAccount().profile() == null
                ? null
                : profile.kakaoAccount().profile().nickname();
    }

    private String kakaoProfileImageUrl(KakaoUserProfileResponse profile) {
        return profile.kakaoAccount() == null || profile.kakaoAccount().profile() == null
                ? null
                : profile.kakaoAccount().profile().profileImageUrl();
    }

    private String normalizeRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + "은(는) 필수입니다.");
        }
        return value.trim();
    }
}
