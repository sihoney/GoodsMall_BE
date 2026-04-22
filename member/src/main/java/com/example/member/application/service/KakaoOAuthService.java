package com.example.member.application.service;

import com.example.member.common.exception.InvalidLoginException;
import com.example.member.config.KakaoOAuthProperties;
import com.example.member.domain.entity.Member;
import com.example.member.domain.entity.MemberOauthAccount;
import com.example.member.domain.enumtype.OAuthProvider;
import com.example.member.infrastructure.kakao.KakaoOAuthClient;
import com.example.member.infrastructure.kakao.KakaoTokenResponse;
import com.example.member.infrastructure.kakao.KakaoUserProfileResponse;
import com.example.member.infrastructure.redis.KakaoOAuthAuthorizeState;
import com.example.member.infrastructure.redis.KakaoOAuthAuthorizeStateStore;
import com.example.member.infrastructure.redis.KakaoOAuthFlowType;
import com.example.member.infrastructure.redis.KakaoOAuthPendingLink;
import com.example.member.infrastructure.repository.MemberOauthAccountRepository;
import com.example.member.infrastructure.repository.MemberRepository;
import com.example.member.presentation.dto.KakaoOAuthLinkResponse;
import com.example.member.presentation.dto.KakaoOAuthResultResponse;
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
    private final MemberRepository memberRepository;
    private final MemberOauthAccountRepository memberOauthAccountRepository;
    private final AuthService authService;

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
                .orElseThrow(() -> new IllegalArgumentException("Kakao OAuth state is invalid or expired."));
    }

    public String buildAuthorizeUrl(String state) {
        return kakaoOAuthClient.buildAuthorizeUrl(state);
    }

    @Transactional
    public KakaoOAuthResultResponse loginByCode(String code) {
        KakaoUserProfileResponse profile = fetchProfile(code);
        return loginByProfile(profile);
    }

    @Transactional
    public KakaoOAuthResultResponse linkByCode(UUID memberId, String code) {
        KakaoUserProfileResponse profile = fetchProfile(code);
        return linkByCurrentMember(memberId, profile);
    }

    public String createOAuthResultKey(KakaoOAuthResultResponse result) {
        return kakaoOAuthAuthorizeStateStore.createOAuthResult(result, kakaoOAuthProperties.resultTtl())
                .orElseThrow(() -> new IllegalStateException("Failed to store Kakao OAuth result."));
    }

    public KakaoOAuthResultResponse consumeOAuthResult(String resultKey) {
        return kakaoOAuthAuthorizeStateStore.consumeOAuthResult(normalizeRequired(resultKey, "resultKey"))
                .orElseThrow(() -> new IllegalStateException("Kakao OAuth result not found or expired."));
    }

    public String getFrontendCallbackUrl() {
        return normalizeRequired(kakaoOAuthProperties.frontendCallbackUrl(), "frontendCallbackUrl");
    }

    public KakaoOAuthResultResponse createErrorResult(KakaoOAuthFlowType flowType, Exception exception) {
        if (exception instanceof IllegalArgumentException) {
            return KakaoOAuthResultResponse.error(
                    "KAKAO_OAUTH_INVALID_REQUEST",
                    "카카오 로그인 요청이 유효하지 않거나 만료되었습니다."
            );
        }

        if (exception instanceof InvalidLoginException) {
            return KakaoOAuthResultResponse.error(
                    flowType == KakaoOAuthFlowType.LINK
                            ? "KAKAO_OAUTH_LINK_MEMBER_INVALID"
                            : "KAKAO_OAUTH_MEMBER_LOGIN_FAILED",
                    flowType == KakaoOAuthFlowType.LINK
                            ? "현재 로그인한 계정으로 카카오 연동을 진행할 수 없습니다."
                            : "연결된 회원 계정으로 로그인할 수 없습니다."
            );
        }

        String message = exception.getMessage();
        if ("Kakao token exchange failed.".equals(message)) {
            return KakaoOAuthResultResponse.error(
                    "KAKAO_OAUTH_TOKEN_EXCHANGE_FAILED",
                    "카카오 토큰 교환에 실패했습니다."
            );
        }
        if ("Kakao user profile not found.".equals(message)) {
            return KakaoOAuthResultResponse.error(
                    "KAKAO_OAUTH_PROFILE_FETCH_FAILED",
                    "카카오 사용자 정보를 가져오지 못했습니다."
            );
        }
        if ("Kakao account already linked to another member.".equals(message)) {
            return KakaoOAuthResultResponse.error(
                    "KAKAO_OAUTH_ALREADY_LINKED_TO_ANOTHER_MEMBER",
                    "이미 다른 계정에 연결된 카카오 계정입니다."
            );
        }
        if ("Member already linked to Kakao.".equals(message)) {
            return KakaoOAuthResultResponse.error(
                    "KAKAO_OAUTH_ALREADY_LINKED",
                    "현재 계정은 이미 카카오와 연결되어 있습니다."
            );
        }

        return KakaoOAuthResultResponse.error(
                "KAKAO_OAUTH_UNKNOWN_ERROR",
                "카카오 로그인 처리 중 오류가 발생했습니다."
        );
    }

    @Transactional
    public KakaoOAuthLinkResponse linkCurrentMember(UUID memberId, String linkToken) {
        KakaoOAuthPendingLink pendingLink = kakaoOAuthAuthorizeStateStore
                .consumePendingLink(normalizeRequired(linkToken, "linkToken"))
                .orElseThrow(() -> new IllegalStateException("Kakao link token not found."));

        if (memberOauthAccountRepository.existsByProviderAndProviderUserId(PROVIDER, pendingLink.providerUserId())) {
            throw new IllegalStateException("Kakao account already linked.");
        }
        if (memberOauthAccountRepository.existsByMemberIdAndProvider(memberId, PROVIDER)) {
            throw new IllegalStateException("Member already linked to Kakao.");
        }
        memberRepository.findById(memberId).orElseThrow(InvalidLoginException::new);

        LocalDateTime now = LocalDateTime.now();
        memberOauthAccountRepository.save(MemberOauthAccount.create(
                UUID.randomUUID(),
                memberId,
                PROVIDER,
                pendingLink.providerUserId(),
                pendingLink.email(),
                pendingLink.nickname(),
                now,
                now
        ));

        return new KakaoOAuthLinkResponse(true, PROVIDER.name(), pendingLink.providerUserId());
    }

    private KakaoOAuthResultResponse loginByProfile(KakaoUserProfileResponse profile) {
        String providerUserId = profile.id().toString();
        String email = kakaoEmail(profile);
        String nickname = kakaoNickname(profile);

        return memberOauthAccountRepository.findByProviderAndProviderUserId(PROVIDER, providerUserId)
                .map(linkedAccount -> {
                    Member member = memberRepository.findById(linkedAccount.getMemberId())
                            .orElseThrow(InvalidLoginException::new);
                    if (!member.isActive()) {
                        throw new InvalidLoginException();
                    }
                    var loginResponse = authService.login(member);
                    return KakaoOAuthResultResponse.success(
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

    private KakaoOAuthResultResponse linkByCurrentMember(UUID memberId, KakaoUserProfileResponse profile) {
        String providerUserId = profile.id().toString();
        String email = kakaoEmail(profile);
        String nickname = kakaoNickname(profile);

        Member member = memberRepository.findById(memberId).orElseThrow(InvalidLoginException::new);
        if (!member.isActive()) {
            throw new InvalidLoginException();
        }

        var existingAccount = memberOauthAccountRepository.findByProviderAndProviderUserId(PROVIDER, providerUserId);
        if (existingAccount.isPresent()) {
            if (!existingAccount.get().getMemberId().equals(memberId)) {
                throw new IllegalStateException("Kakao account already linked to another member.");
            }

            return KakaoOAuthResultResponse.success(
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

        if (memberOauthAccountRepository.existsByMemberIdAndProvider(memberId, PROVIDER)) {
            throw new IllegalStateException("Member already linked to Kakao.");
        }

        LocalDateTime now = LocalDateTime.now();
        memberOauthAccountRepository.save(MemberOauthAccount.create(
                UUID.randomUUID(),
                memberId,
                PROVIDER,
                providerUserId,
                email,
                nickname,
                now,
                now
        ));

        return KakaoOAuthResultResponse.success(
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
            throw new IllegalStateException("Kakao token exchange failed.");
        }
        KakaoUserProfileResponse profile = kakaoOAuthClient.fetchUserProfile(tokenResponse.accessToken());
        if (profile == null || profile.id() == null) {
            throw new IllegalStateException("Kakao user profile not found.");
        }
        return profile;
    }

    private KakaoOAuthResultResponse createPendingLink(KakaoUserProfileResponse profile) {
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
        return KakaoOAuthResultResponse.linkRequired(
                linkToken,
                PROVIDER.name(),
                providerUserId,
                pendingLink.email(),
                pendingLink.nickname()
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
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return value.trim();
    }
}
