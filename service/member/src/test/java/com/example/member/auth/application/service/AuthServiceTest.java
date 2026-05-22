package com.example.member.auth.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.member.common.application.dto.AuthSessionMetadata;
import com.example.member.auth.application.dto.command.LoginCommand;
import com.example.member.auth.application.dto.command.TokenRefreshCommand;
import com.example.member.auth.application.dto.result.AuthSessionListResult;
import com.example.member.auth.application.dto.result.AuthTokenResult;
import com.example.member.auth.exception.InvalidLoginException;
import com.example.member.restriction.exception.MemberRestrictedException;
import com.example.member.member.exception.MemberSuspendedException;
import com.example.member.member.exception.MemberWithdrawnException;
import com.example.member.member.domain.entity.Member;
import com.example.member.restriction.domain.entity.MemberRestriction;
import com.example.member.restriction.application.service.MemberRestrictionService;
import com.example.member.member.domain.enumtype.MemberStatus;
import com.example.member.restriction.domain.enumtype.RestrictionType;
import com.example.member.member.infrastructure.persistence.jpa.MemberJpaAdapter;
import com.example.member.auth.infrastructure.redis.auth.AuthSession;
import com.example.member.auth.infrastructure.redis.auth.ParsedRefreshToken;
import com.example.member.auth.infrastructure.redis.auth.RefreshTokenStore;
import com.example.member.auth.infrastructure.redis.auth.TokenBlacklistStore;
import com.example.member.auth.infrastructure.security.jwt.JwtTokenProvider;
import com.todaylunch.common.security.auth.enumtype.MemberRole;
import com.todaylunch.common.security.exception.InvalidTokenException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private MemberJpaAdapter memberPersistencePort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenStore refreshTokenStore;

    @Mock
    private TokenBlacklistStore tokenBlacklistStore;

    @Mock
    private MemberRestrictionService memberRestrictionService;

    private AuthLoginService authLoginService;
    private AuthTokenRefreshService authTokenRefreshService;
    private AuthSessionService authSessionService;

    @BeforeEach
    void setUp() {
        LoginEligibilityValidator loginEligibilityValidator = new LoginEligibilityValidator(memberRestrictionService);
        AuthTokenIssuer authTokenIssuer = new AuthTokenIssuer(jwtTokenProvider, refreshTokenStore);
        authLoginService = new AuthLoginService(
                memberPersistencePort,
                passwordEncoder,
                loginEligibilityValidator,
                authTokenIssuer
        );
        authTokenRefreshService = new AuthTokenRefreshService(
                memberPersistencePort,
                jwtTokenProvider,
                refreshTokenStore,
                loginEligibilityValidator
        );
        authSessionService = new AuthSessionService(
                jwtTokenProvider,
                refreshTokenStore,
                tokenBlacklistStore
        );
    }

    @Test
    void login_success_returnsTokensWhenNoActiveLoginBan() {
        Member member = createMember();
        LoginCommand command = new LoginCommand("member@test.com", "plain-password", AuthSessionMetadata.empty());

        when(memberPersistencePort.findByEmail("member@test.com")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("plain-password", "encoded-password")).thenReturn(true);
        when(memberRestrictionService.getActiveLoginRestriction(eq(member.getMemberId()), any()))
                .thenReturn(null);
        when(jwtTokenProvider.createAccessToken(eq(member), any(UUID.class))).thenReturn("access-token");
        when(jwtTokenProvider.createRefreshToken(eq(member), any(UUID.class))).thenReturn("refresh-token");
        when(jwtTokenProvider.parseRefreshToken("refresh-token"))
                .thenReturn(new ParsedRefreshToken(member.getMemberId(), UUID.randomUUID(), "refresh-token-id"));
        when(jwtTokenProvider.getAccessExpiration()).thenReturn(3600L);
        when(jwtTokenProvider.getRefreshExpiration()).thenReturn(7200L);

        AuthTokenResult response = authLoginService.login(command);

        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        verify(refreshTokenStore).createSession(
                eq(member.getMemberId()),
                any(UUID.class),
                eq("refresh-token-id"),
                eq(Duration.ofMillis(7200L)),
                any(AuthSessionMetadata.class)
        );
    }

    @Test
    void login_activeLoginBan_throwsMemberRestrictedException() {
        Member member = createMember();
        LoginCommand command = new LoginCommand("member@test.com", "plain-password", AuthSessionMetadata.empty());
        MemberRestriction restriction = MemberRestriction.create(
                UUID.randomUUID(),
                member.getMemberId(),
                UUID.randomUUID(),
                "abuse",
                RestrictionType.LOGIN_BAN,
                24,
                LocalDateTime.now()
        );

        when(memberPersistencePort.findByEmail("member@test.com")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("plain-password", "encoded-password")).thenReturn(true);
        when(memberRestrictionService.getActiveLoginRestriction(eq(member.getMemberId()), any()))
                .thenReturn(restriction);

        assertThrows(MemberRestrictedException.class, () -> authLoginService.login(command));

        verify(jwtTokenProvider, never()).createAccessToken(eq(member), any(UUID.class));
        verify(refreshTokenStore, never()).createSession(any(UUID.class), any(UUID.class), any(String.class), any(Duration.class), any(AuthSessionMetadata.class));
    }

    @Test
    void login_invalidPassword_throwsInvalidLoginException() {
        Member member = createMember();
        LoginCommand command = new LoginCommand("member@test.com", "plain-password", AuthSessionMetadata.empty());

        when(memberPersistencePort.findByEmail("member@test.com")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("plain-password", "encoded-password")).thenReturn(false);

        assertThrows(InvalidLoginException.class, () -> authLoginService.login(command));
    }

    @Test
    void login_pendingVerification_throwsIllegalStateException() {
        Member member = createMember(MemberStatus.PENDING_VERIFICATION);
        LoginCommand command = new LoginCommand("member@test.com", "plain-password", AuthSessionMetadata.empty());

        when(memberPersistencePort.findByEmail("member@test.com")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("plain-password", "encoded-password")).thenReturn(true);

        assertThrows(com.example.member.verification.exception.EmailVerificationRequiredException.class, () -> authLoginService.login(command));

        verify(jwtTokenProvider, never()).createAccessToken(eq(member), any(UUID.class));
        verify(refreshTokenStore, never()).createSession(any(UUID.class), any(UUID.class), any(String.class), any(Duration.class), any(AuthSessionMetadata.class));
    }

    @Test
    void login_suspendedMember_throwsMemberSuspendedException() {
        Member member = createMember(MemberStatus.SUSPENDED);
        LoginCommand command = new LoginCommand("member@test.com", "plain-password", AuthSessionMetadata.empty());

        when(memberPersistencePort.findByEmail("member@test.com")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("plain-password", "encoded-password")).thenReturn(true);

        assertThrows(MemberSuspendedException.class, () -> authLoginService.login(command));

        verify(jwtTokenProvider, never()).createAccessToken(eq(member), any(UUID.class));
        verify(refreshTokenStore, never()).createSession(any(UUID.class), any(UUID.class), any(String.class), any(Duration.class), any(AuthSessionMetadata.class));
    }

    @Test
    void refresh_activeMember_returnsNewAccessTokenAndRotatesRefreshToken() {
        Member member = createMember(MemberStatus.ACTIVE);
        UUID memberId = member.getMemberId();
        UUID sessionId = UUID.randomUUID();
        TokenRefreshCommand command = new TokenRefreshCommand("refresh-token", AuthSessionMetadata.empty());

        when(jwtTokenProvider.parseRefreshToken("refresh-token"))
                .thenReturn(new ParsedRefreshToken(memberId, sessionId, "refresh-token-id"));
        when(memberRestrictionService.getActiveLoginRestriction(eq(memberId), any()))
                .thenReturn(null);
        when(refreshTokenStore.findBySessionId(sessionId))
                .thenReturn(Optional.of(new AuthSession(memberId, sessionId, "refresh-token-id", java.time.Instant.now(), java.time.Instant.now(), java.time.Instant.now(), null, null)));
        when(memberPersistencePort.findById(memberId)).thenReturn(Optional.of(member));
        when(jwtTokenProvider.createAccessToken(member, sessionId)).thenReturn("new-access-token");
        when(jwtTokenProvider.createRefreshToken(member, sessionId)).thenReturn("new-refresh-token");
        when(jwtTokenProvider.parseRefreshToken("new-refresh-token"))
                .thenReturn(new ParsedRefreshToken(memberId, sessionId, "new-refresh-token-id"));
        when(jwtTokenProvider.getAccessExpiration()).thenReturn(3600L);
        when(jwtTokenProvider.getRefreshExpiration()).thenReturn(7200L);

        AuthTokenResult response = authTokenRefreshService.refresh(command);

        assertEquals("new-access-token", response.accessToken());
        assertEquals("new-refresh-token", response.refreshToken());
        assertEquals(sessionId, response.sessionId());
        verify(refreshTokenStore).updateRefreshTokenId(
                eq(sessionId),
                eq("new-refresh-token-id"),
                eq(Duration.ofMillis(7200L)),
                any(AuthSessionMetadata.class)
        );
    }

    @Test
    void refresh_withdrawnMember_throwsMemberWithdrawnException() {
        Member member = createMember(MemberStatus.WITHDRAWN);
        UUID memberId = member.getMemberId();
        UUID sessionId = UUID.randomUUID();
        TokenRefreshCommand command = new TokenRefreshCommand("refresh-token", AuthSessionMetadata.empty());

        when(jwtTokenProvider.parseRefreshToken("refresh-token"))
                .thenReturn(new ParsedRefreshToken(memberId, sessionId, "refresh-token-id"));
        when(memberRestrictionService.getActiveLoginRestriction(org.mockito.ArgumentMatchers.eq(memberId), any()))
                .thenReturn(null);
        when(refreshTokenStore.findBySessionId(sessionId))
                .thenReturn(Optional.of(new AuthSession(memberId, sessionId, "refresh-token-id", java.time.Instant.now(), java.time.Instant.now(), java.time.Instant.now(), null, null)));
        when(memberPersistencePort.findById(memberId)).thenReturn(Optional.of(member));

        assertThrows(MemberWithdrawnException.class, () ->
                authTokenRefreshService.refresh(command));

        verify(jwtTokenProvider, never()).createAccessToken(eq(member), eq(sessionId));
    }

    @Test
    void refresh_withRotatedTokenDeletesSessionAndThrowsInvalidTokenException() {
        Member member = createMember(MemberStatus.ACTIVE);
        UUID memberId = member.getMemberId();
        UUID sessionId = UUID.randomUUID();
        TokenRefreshCommand command = new TokenRefreshCommand("refresh-token", AuthSessionMetadata.empty());

        when(jwtTokenProvider.parseRefreshToken("refresh-token"))
                .thenReturn(new ParsedRefreshToken(memberId, sessionId, "refresh-token-id"));
        when(memberRestrictionService.getActiveLoginRestriction(eq(memberId), any()))
                .thenReturn(null);
        when(refreshTokenStore.findBySessionId(sessionId))
                .thenReturn(Optional.of(new AuthSession(memberId, sessionId, "different-token-id", java.time.Instant.now(), java.time.Instant.now(), java.time.Instant.now(), null, null)));

        assertThrows(InvalidTokenException.class, () -> authTokenRefreshService.refresh(command));

        verify(refreshTokenStore).deleteSession(memberId, sessionId);
        verify(memberPersistencePort, never()).findById(any());
    }

    @Test
    void getSessions_marksCurrentSessionAndSortsByLastAccessedAtDesc() {
        UUID memberId = UUID.randomUUID();
        UUID currentSessionId = UUID.randomUUID();
        UUID otherSessionId = UUID.randomUUID();
        Instant now = Instant.now();

        when(refreshTokenStore.findSessionsByMemberId(memberId)).thenReturn(List.of(
                new AuthSession(
                        memberId,
                        otherSessionId,
                        "refresh-token-id-2",
                        now.minusSeconds(300),
                        now.minusSeconds(120),
                        now.minusSeconds(120),
                        "other-agent",
                        "127.0.0.2"
                ),
                new AuthSession(
                        memberId,
                        currentSessionId,
                        "refresh-token-id-1",
                        now.minusSeconds(600),
                        now.minusSeconds(30),
                        now.minusSeconds(30),
                        "current-agent",
                        "127.0.0.1"
                )
        ));

        AuthSessionListResult result = authSessionService.getSessions(memberId, currentSessionId);

        assertEquals(2, result.sessions().size());
        assertEquals(currentSessionId, result.sessions().get(0).sessionId());
        assertEquals(otherSessionId, result.sessions().get(1).sessionId());
        assertEquals(true, result.sessions().get(0).current());
        assertEquals(false, result.sessions().get(1).current());
    }

    @Test
    void logoutSession_otherSessionDeletesAndBlacklistsTargetSession() {
        UUID memberId = UUID.randomUUID();
        UUID currentSessionId = UUID.randomUUID();
        UUID targetSessionId = UUID.randomUUID();

        when(refreshTokenStore.findBySessionId(targetSessionId))
                .thenReturn(Optional.of(new AuthSession(
                        memberId,
                        targetSessionId,
                        "refresh-token-id",
                        Instant.now(),
                        Instant.now(),
                        Instant.now(),
                        "target-agent",
                        "127.0.0.2"
                )));
        when(jwtTokenProvider.getRefreshExpiration()).thenReturn(7200L);

        authSessionService.logoutSession("Bearer current-access-token", memberId, currentSessionId, targetSessionId);

        verify(refreshTokenStore).deleteSession(memberId, targetSessionId);
        verify(tokenBlacklistStore).blacklistSession(targetSessionId, Duration.ofMillis(7200L));
    }

    @Test
    void logoutSession_currentSessionDelegatesToCurrentLogoutFlow() {
        UUID memberId = UUID.randomUUID();
        UUID currentSessionId = UUID.randomUUID();
        UUID accessTokenId = UUID.randomUUID();
        String token = "access-token";

        when(jwtTokenProvider.parseAccessToken(token))
                .thenReturn(new com.example.member.auth.infrastructure.redis.auth.ParsedAccessToken(
                        memberId,
                        currentSessionId,
                        accessTokenId.toString(),
                        LocalDateTime.now().plusMinutes(10).toInstant(java.time.ZoneOffset.UTC)
                ));
        when(jwtTokenProvider.getRefreshExpiration()).thenReturn(7200L);

        authSessionService.logoutSession(token, memberId, currentSessionId, currentSessionId);

        verify(refreshTokenStore).deleteSession(memberId, currentSessionId);
        verify(tokenBlacklistStore).blacklistAccessToken(any(), any(Duration.class));
        verify(tokenBlacklistStore).blacklistSession(currentSessionId, Duration.ofMillis(7200L));
    }

    @Test
    void logoutCurrentSession_blacklistsAccessTokenAndSession() {
        UUID memberId = UUID.randomUUID();
        UUID sessionId = UUID.randomUUID();
        UUID accessTokenId = UUID.randomUUID();
        String token = "access-token";

        when(jwtTokenProvider.parseAccessToken(token))
                .thenReturn(new com.example.member.auth.infrastructure.redis.auth.ParsedAccessToken(
                        memberId,
                        sessionId,
                        accessTokenId.toString(),
                        LocalDateTime.now().plusMinutes(10).toInstant(java.time.ZoneOffset.UTC)
                ));
        when(jwtTokenProvider.getRefreshExpiration()).thenReturn(7200L);

        authSessionService.logoutCurrentSession(token);

        verify(refreshTokenStore).deleteSession(memberId, sessionId);
        verify(tokenBlacklistStore).blacklistAccessToken(any(), any(Duration.class));
        verify(tokenBlacklistStore).blacklistSession(any(), any(Duration.class));
    }

    @Test
    void logoutAllSessions_blacklistsMemberSessions() {
        UUID memberId = UUID.randomUUID();
        UUID sessionId1 = UUID.randomUUID();
        UUID sessionId2 = UUID.randomUUID();
        UUID accessTokenId = UUID.randomUUID();
        String token = "access-token";

        when(jwtTokenProvider.parseAccessToken(token))
                .thenReturn(new com.example.member.auth.infrastructure.redis.auth.ParsedAccessToken(
                        memberId,
                        sessionId1,
                        accessTokenId.toString(),
                        LocalDateTime.now().plusMinutes(10).toInstant(java.time.ZoneOffset.UTC)
                ));
        when(refreshTokenStore.findSessionIdsByMemberId(memberId)).thenReturn(java.util.Set.of(sessionId1, sessionId2));
        when(jwtTokenProvider.getRefreshExpiration()).thenReturn(7200L);

        authSessionService.logoutAllSessions(token);

        verify(tokenBlacklistStore).blacklistSession(sessionId1, Duration.ofMillis(7200L));
        verify(tokenBlacklistStore).blacklistSession(sessionId2, Duration.ofMillis(7200L));
        verify(refreshTokenStore).deleteAllSessions(memberId);
        verify(tokenBlacklistStore).blacklistAccessToken(any(), any(Duration.class));
    }

    private Member createMember() {
        return createMember(MemberStatus.ACTIVE);
    }

    private Member createMember(MemberStatus status) {
        LocalDateTime now = LocalDateTime.now();
        return Member.create(
                UUID.randomUUID(),
                "member@test.com",
                "encoded-password",
                "tester",
                null,
                null,
                null,
                MemberRole.USER,
                status,
                now,
                now
        );
    }
}
