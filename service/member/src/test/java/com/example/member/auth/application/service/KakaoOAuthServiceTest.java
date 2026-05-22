package com.example.member.auth.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.member.auth.application.dto.result.KakaoOAuthLinkResult;
import com.example.member.auth.application.dto.result.KakaoOAuthResult;
import com.example.member.auth.application.port.in.AuthLoginUsecase;
import com.example.member.auth.application.port.out.MemberOauthEventPort;
import com.example.member.common.config.KakaoOAuthProperties;
import com.example.member.member.domain.entity.Member;
import com.example.member.auth.domain.entity.MemberOauthAccount;
import com.example.member.member.domain.enumtype.MemberStatus;
import com.example.member.auth.domain.enumtype.OAuthProvider;
import com.example.member.auth.infrastructure.kakao.KakaoOAuthClient;
import com.example.member.auth.infrastructure.kakao.KakaoTokenResponse;
import com.example.member.auth.infrastructure.kakao.KakaoUserProfileResponse;
import com.example.member.member.infrastructure.persistence.jpa.MemberJpaAdapter;
import com.example.member.auth.infrastructure.persistence.jpa.MemberOauthAccountJpaAdapter;
import com.example.member.auth.infrastructure.redis.oauth.KakaoOAuthAuthorizeStateStore;
import com.example.member.auth.infrastructure.redis.oauth.KakaoOAuthPendingLink;
import com.todaylunch.common.security.auth.enumtype.MemberRole;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KakaoOAuthServiceTest {

    @Mock
    private KakaoOAuthProperties kakaoOAuthProperties;

    @Mock
    private KakaoOAuthClient kakaoOAuthClient;

    @Mock
    private KakaoOAuthAuthorizeStateStore kakaoOAuthAuthorizeStateStore;

    @Mock
    private MemberJpaAdapter memberPersistencePort;

    @Mock
    private MemberOauthAccountJpaAdapter memberOauthAccountPersistencePort;

    @Mock
    private AuthLoginUsecase authLoginUsecase;

    @Mock
    private MemberOauthEventPort memberEventPort;

    @InjectMocks
    private KakaoOAuthService kakaoOAuthService;

    @Test
    void linkCurrentMember_success_savesOauthAccountAndPublishesEvent() {
        UUID memberId = UUID.randomUUID();
        KakaoOAuthPendingLink pendingLink = new KakaoOAuthPendingLink(
                "link-token",
                "provider-user-1",
                "member@test.com",
                "tester",
                null,
                Instant.now()
        );
        Member member = createActiveMember(memberId);

        when(kakaoOAuthAuthorizeStateStore.consumePendingLink("link-token")).thenReturn(Optional.of(pendingLink));
        when(memberOauthAccountPersistencePort.existsByProviderAndProviderUserId(OAuthProvider.KAKAO, "provider-user-1")).thenReturn(false);
        when(memberOauthAccountPersistencePort.existsByMemberIdAndProvider(memberId, OAuthProvider.KAKAO)).thenReturn(false);
        when(memberPersistencePort.findById(memberId)).thenReturn(Optional.of(member));
        when(memberOauthAccountPersistencePort.save(any(MemberOauthAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        KakaoOAuthLinkResult response = kakaoOAuthService.linkCurrentMember(memberId, "link-token");

        ArgumentCaptor<MemberOauthAccount> accountCaptor = ArgumentCaptor.forClass(MemberOauthAccount.class);
        verify(memberOauthAccountPersistencePort).save(accountCaptor.capture());
        MemberOauthAccount savedAccount = accountCaptor.getValue();
        assertEquals(memberId, savedAccount.getMemberId());
        assertEquals(OAuthProvider.KAKAO, savedAccount.getProvider());
        assertEquals("provider-user-1", savedAccount.getProviderUserId());
        assertEquals("member@test.com", savedAccount.getProviderEmail());
        assertEquals("tester", savedAccount.getProviderNickname());
        verify(memberEventPort).publishMemberOauthLinked(
                memberId,
                "KAKAO",
                "provider-user-1",
                "member@test.com",
                "tester",
                savedAccount.getCreatedAt()
        );
        assertEquals(true, response.linked());
        assertEquals("KAKAO", response.provider());
        assertEquals("provider-user-1", response.providerUserId());
    }

    @Test
    void linkByCode_success_savesOauthAccountAndPublishesEvent() {
        UUID memberId = UUID.randomUUID();
        Member member = createActiveMember(memberId);
        KakaoUserProfileResponse profile = new KakaoUserProfileResponse(
                12345L,
                new KakaoUserProfileResponse.KakaoAccount(
                        "member@test.com",
                        new KakaoUserProfileResponse.KakaoProfile("tester", null)
                )
        );

        when(kakaoOAuthClient.exchangeCode("valid-code")).thenReturn(new KakaoTokenResponse("access-token", "Bearer", "refresh-token", 3600));
        when(kakaoOAuthClient.fetchUserProfile("access-token")).thenReturn(profile);
        when(memberPersistencePort.findById(memberId)).thenReturn(Optional.of(member));
        when(memberOauthAccountPersistencePort.findByProviderAndProviderUserId(OAuthProvider.KAKAO, "12345")).thenReturn(Optional.empty());
        when(memberOauthAccountPersistencePort.existsByMemberIdAndProvider(memberId, OAuthProvider.KAKAO)).thenReturn(false);
        when(memberOauthAccountPersistencePort.save(any(MemberOauthAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        KakaoOAuthResult response = kakaoOAuthService.linkByCode(memberId, "valid-code");

        ArgumentCaptor<MemberOauthAccount> accountCaptor = ArgumentCaptor.forClass(MemberOauthAccount.class);
        verify(memberOauthAccountPersistencePort).save(accountCaptor.capture());
        MemberOauthAccount savedAccount = accountCaptor.getValue();
        verify(memberEventPort).publishMemberOauthLinked(
                memberId,
                "KAKAO",
                "12345",
                "member@test.com",
                "tester",
                savedAccount.getCreatedAt()
        );
        assertEquals("KAKAO", response.provider());
        assertEquals("12345", response.providerUserId());
        assertFalse(response.linkRequired());
    }

    @Test
    void linkByCode_whenAlreadyLinkedToSameMember_doesNotPublishEvent() {
        UUID memberId = UUID.randomUUID();
        Member member = createActiveMember(memberId);
        KakaoUserProfileResponse profile = new KakaoUserProfileResponse(
                12345L,
                new KakaoUserProfileResponse.KakaoAccount(
                        "member@test.com",
                        new KakaoUserProfileResponse.KakaoProfile("tester", null)
                )
        );
        MemberOauthAccount existingAccount = MemberOauthAccount.create(
                UUID.randomUUID(),
                memberId,
                OAuthProvider.KAKAO,
                "12345",
                "member@test.com",
                "tester",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(1)
        );

        when(kakaoOAuthClient.exchangeCode("valid-code")).thenReturn(new KakaoTokenResponse("access-token", "Bearer", "refresh-token", 3600));
        when(kakaoOAuthClient.fetchUserProfile("access-token")).thenReturn(profile);
        when(memberPersistencePort.findById(memberId)).thenReturn(Optional.of(member));
        when(memberOauthAccountPersistencePort.findByProviderAndProviderUserId(OAuthProvider.KAKAO, "12345"))
                .thenReturn(Optional.of(existingAccount));

        KakaoOAuthResult response = kakaoOAuthService.linkByCode(memberId, "valid-code");

        verify(memberOauthAccountPersistencePort, never()).save(any(MemberOauthAccount.class));
        verify(memberEventPort, never()).publishMemberOauthLinked(
                any(UUID.class),
                any(String.class),
                any(String.class),
                any(),
                any(),
                any(LocalDateTime.class)
        );
        assertEquals("KAKAO", response.provider());
        assertEquals("12345", response.providerUserId());
    }

    private Member createActiveMember(UUID memberId) {
        LocalDateTime now = LocalDateTime.now();
        return Member.create(
                memberId,
                "member@test.com",
                "encoded-password",
                "tester",
                null,
                null,
                null,
                MemberRole.USER,
                MemberStatus.ACTIVE,
                now,
                now
        );
    }
}
