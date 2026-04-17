package com.example.member.application.service;

import com.example.member.common.exception.InvalidLoginException;
import com.example.member.config.KakaoOAuthProperties;
import com.example.member.domain.entity.Member;
import com.example.member.domain.entity.MemberOauthAccount;
import com.example.member.domain.enumtype.OAuthProvider;
import com.example.member.infrastructure.kakao.KakaoOAuthClient;
import com.example.member.infrastructure.kakao.KakaoTokenResponse;
import com.example.member.infrastructure.kakao.KakaoUserProfileResponse;
import com.example.member.infrastructure.redis.KakaoOAuthAuthorizeStateStore;
import com.example.member.infrastructure.redis.KakaoOAuthPendingLink;
import com.example.member.infrastructure.repository.MemberOauthAccountRepository;
import com.example.member.infrastructure.repository.MemberRepository;
import com.example.member.presentation.dto.KakaoOAuthLinkResponse;
import com.example.member.presentation.dto.KakaoOAuthLoginResponse;
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

    public String createAuthorizeState() {
        String state = UUID.randomUUID().toString();
        kakaoOAuthAuthorizeStateStore.create(state, kakaoOAuthProperties.stateTtl());
        return state;
    }

    public String buildAuthorizeUrl(String state) {
        return kakaoOAuthClient.buildAuthorizeUrl(state);
    }

    @Transactional
    public KakaoOAuthLoginResponse loginByCode(String code, String state) {
        validateState(state);
        KakaoUserProfileResponse profile = fetchProfile(code);
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
                    return KakaoOAuthLoginResponse.linked(
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

    @Transactional
    public KakaoOAuthLinkResponse linkCurrentMember(UUID memberId, String linkToken) {
        KakaoOAuthPendingLink pendingLink = kakaoOAuthAuthorizeStateStore.consumePendingLink(normalizeRequired(linkToken, "linkToken"))
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

    private void validateState(String state) {
        boolean consumed = kakaoOAuthAuthorizeStateStore.consume(normalizeRequired(state, "state"));
        if (!consumed) {
            throw new IllegalArgumentException("Kakao OAuth state is invalid or expired.");
        }
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

    private KakaoOAuthLoginResponse createPendingLink(KakaoUserProfileResponse profile) {
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
        kakaoOAuthAuthorizeStateStore.createPendingLink(pendingLink, kakaoOAuthProperties.pendingLinkTtl());
        return KakaoOAuthLoginResponse.linkRequired(
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
