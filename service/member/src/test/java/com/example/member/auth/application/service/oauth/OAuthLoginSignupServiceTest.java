package com.example.member.auth.application.service.oauth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.member.auth.application.dto.OAuthUserProfile;
import com.example.member.auth.application.dto.result.AuthTokenResult;
import com.example.member.auth.application.dto.result.OAuthResult;
import com.example.member.auth.application.port.in.AuthLoginUsecase;
import com.example.member.auth.application.port.out.MemberOauthEventPort;
import com.example.member.auth.domain.entity.MemberOauthAccount;
import com.example.member.auth.domain.enumtype.OAuthProvider;
import com.example.member.common.application.dto.AuthSessionMetadata;
import com.example.member.member.application.port.out.MemberEventPort;
import com.example.member.member.domain.entity.Member;
import com.example.member.member.domain.enumtype.MemberStatus;
import com.example.member.member.infrastructure.persistence.jpa.MemberJpaAdapter;
import com.example.member.auth.infrastructure.persistence.jpa.MemberOauthAccountJpaAdapter;
import com.todaylunch.common.security.auth.enumtype.MemberRole;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class OAuthLoginSignupServiceTest {

    @Mock
    private MemberJpaAdapter memberPersistencePort;

    @Mock
    private MemberOauthAccountJpaAdapter memberOauthAccountPersistencePort;

    @Mock
    private MemberEventPort memberEventPort;

    @Mock
    private MemberOauthEventPort memberOauthEventPort;

    @Mock
    private AuthLoginUsecase authLoginUsecase;

    @Mock
    private PasswordEncoder passwordEncoder;

    private OAuthLoginSignupService loginService;

    @BeforeEach
    void setUp() {
        loginService = new OAuthLoginSignupService(
                memberPersistencePort,
                memberOauthAccountPersistencePort,
                memberEventPort,
                memberOauthEventPort,
                authLoginUsecase,
                passwordEncoder
        );
    }

    @Test
    void loginOrSignupByProfile_whenOauthAccountExists_logsInExistingMember() {
        UUID memberId = UUID.randomUUID();
        Member member = createActiveMember(memberId);
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
        AuthTokenResult tokenResult = tokenResult();

        when(memberOauthAccountPersistencePort.findByProviderAndProviderUserId(OAuthProvider.KAKAO, "12345"))
                .thenReturn(Optional.of(existingAccount));
        when(memberPersistencePort.findById(memberId)).thenReturn(Optional.of(member));
        when(authLoginUsecase.loginAuthenticatedMember(any(Member.class), any(AuthSessionMetadata.class)))
                .thenReturn(tokenResult);

        OAuthResult response = loginService.loginOrSignupByProfile(profile("member@test.com"), metadata());

        assertEquals("KAKAO", response.provider());
        assertEquals("12345", response.providerUserId());
        assertEquals(tokenResult.accessToken(), response.accessToken());
        assertFalse(response.linkRequired());
        verify(memberPersistencePort, never()).save(any(Member.class));
        verify(memberOauthAccountPersistencePort, never()).save(any(MemberOauthAccount.class));
    }

    @Test
    void loginOrSignupByProfile_whenOauthAccountMissing_createsMemberAndOauthAccountThenLogsIn() {
        AuthTokenResult tokenResult = tokenResult();

        when(memberOauthAccountPersistencePort.findByProviderAndProviderUserId(OAuthProvider.KAKAO, "12345"))
                .thenReturn(Optional.empty());
        when(memberPersistencePort.existsByEmail("member@test.com")).thenReturn(false);
        when(passwordEncoder.encode(any(String.class))).thenReturn("encoded-oauth-password");
        when(memberPersistencePort.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(memberOauthAccountPersistencePort.save(any(MemberOauthAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(authLoginUsecase.loginAuthenticatedMember(any(Member.class), any(AuthSessionMetadata.class)))
                .thenReturn(tokenResult);

        OAuthResult response = loginService.loginOrSignupByProfile(profile("member@test.com"), metadata());

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberPersistencePort).save(memberCaptor.capture());
        Member savedMember = memberCaptor.getValue();
        assertEquals("member@test.com", savedMember.getEmail());
        assertEquals("tester", savedMember.getNickname());
        assertEquals(MemberStatus.ACTIVE, savedMember.getStatus());

        ArgumentCaptor<MemberOauthAccount> accountCaptor = ArgumentCaptor.forClass(MemberOauthAccount.class);
        verify(memberOauthAccountPersistencePort).save(accountCaptor.capture());
        MemberOauthAccount savedAccount = accountCaptor.getValue();
        assertEquals(savedMember.getMemberId(), savedAccount.getMemberId());
        assertEquals(OAuthProvider.KAKAO, savedAccount.getProvider());
        assertEquals("12345", savedAccount.getProviderUserId());
        assertEquals("member@test.com", savedAccount.getProviderEmail());

        verify(memberEventPort).publishMemberSignedUp(savedMember);
        verify(memberOauthEventPort).publishMemberOauthLinked(
                savedMember.getMemberId(),
                "KAKAO",
                "12345",
                "member@test.com",
                "tester",
                savedAccount.getCreatedAt()
        );
        assertEquals(tokenResult.accessToken(), response.accessToken());
        assertFalse(response.linkRequired());
    }

    @Test
    void loginOrSignupByProfile_whenNewOauthAccountHasNoEmail_failsSignup() {
        when(memberOauthAccountPersistencePort.findByProviderAndProviderUserId(OAuthProvider.KAKAO, "12345"))
                .thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> loginService.loginOrSignupByProfile(profile(null), metadata())
        );

        assertEquals("OAUTH_EMAIL_REQUIRED", exception.getMessage());
        verify(memberPersistencePort, never()).save(any(Member.class));
        verify(memberOauthAccountPersistencePort, never()).save(any(MemberOauthAccount.class));
    }

    @Test
    void loginOrSignupByProfile_whenNewOauthEmailAlreadyExists_failsSignup() {
        when(memberOauthAccountPersistencePort.findByProviderAndProviderUserId(OAuthProvider.KAKAO, "12345"))
                .thenReturn(Optional.empty());
        when(memberPersistencePort.existsByEmail("member@test.com")).thenReturn(true);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> loginService.loginOrSignupByProfile(profile("member@test.com"), metadata())
        );

        assertEquals("OAUTH_EMAIL_ALREADY_REGISTERED", exception.getMessage());
        verify(memberPersistencePort, never()).save(any(Member.class));
        verify(memberOauthAccountPersistencePort, never()).save(any(MemberOauthAccount.class));
    }

    private OAuthUserProfile profile(String email) {
        return new OAuthUserProfile(
                OAuthProvider.KAKAO,
                "12345",
                email,
                "tester",
                null
        );
    }

    private AuthSessionMetadata metadata() {
        return new AuthSessionMetadata("test-agent", "127.0.0.1");
    }

    private AuthTokenResult tokenResult() {
        return new AuthTokenResult(
                "access-token",
                "refresh-token",
                "Bearer",
                3600L,
                7200L,
                UUID.randomUUID()
        );
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
