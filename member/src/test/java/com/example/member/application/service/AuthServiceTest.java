package com.example.member.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.member.common.exception.InvalidLoginException;
import com.example.member.common.exception.MemberRestrictedException;
import com.example.member.domain.entity.Member;
import com.example.member.domain.entity.MemberRestriction;
import com.example.member.domain.enumtype.MemberStatus;
import com.example.member.domain.enumtype.RestrictionType;
import com.example.member.infrastructure.redis.RefreshTokenStore;
import com.example.member.infrastructure.repository.MemberRepository;
import com.example.member.presentation.dto.LoginRequest;
import com.example.member.presentation.dto.LoginResponse;
import com.example.member.presentation.dto.TokenRefreshRequest;
import com.example.member.presentation.dto.TokenRefreshResponse;
import com.example.member.security.JwtTokenProvider;
import com.todaylunch.common.security.auth.enumtype.MemberRole;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenStore refreshTokenStore;

    @Mock
    private MemberRestrictionService memberRestrictionService;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_success_returnsTokensWhenNoActiveLoginBan() {
        Member member = createMember();
        LoginRequest request = new LoginRequest("member@test.com", "plain-password");

        when(memberRepository.findByEmail("member@test.com")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("plain-password", "encoded-password")).thenReturn(true);
        when(memberRestrictionService.getActiveLoginRestriction(org.mockito.ArgumentMatchers.eq(member.getMemberId()), any()))
                .thenReturn(null);
        when(jwtTokenProvider.createAccessToken(member)).thenReturn("access-token");
        when(jwtTokenProvider.createRefreshToken(member)).thenReturn("refresh-token");
        when(jwtTokenProvider.getAccessExpiration()).thenReturn(3600L);
        when(jwtTokenProvider.getRefreshExpiration()).thenReturn(7200L);

        LoginResponse response = authService.login(request);

        assertEquals("access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
        verify(refreshTokenStore).save(member.getMemberId(), "refresh-token", Duration.ofMillis(7200L));
    }

    @Test
    void login_activeLoginBan_throwsMemberRestrictedException() {
        Member member = createMember();
        LoginRequest request = new LoginRequest("member@test.com", "plain-password");
        MemberRestriction restriction = MemberRestriction.create(
                UUID.randomUUID(),
                member.getMemberId(),
                UUID.randomUUID(),
                "abuse",
                RestrictionType.LOGIN_BAN,
                24,
                LocalDateTime.now()
        );

        when(memberRepository.findByEmail("member@test.com")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("plain-password", "encoded-password")).thenReturn(true);
        when(memberRestrictionService.getActiveLoginRestriction(org.mockito.ArgumentMatchers.eq(member.getMemberId()), any()))
                .thenReturn(restriction);

        assertThrows(MemberRestrictedException.class, () -> authService.login(request));

        verify(jwtTokenProvider, never()).createAccessToken(member);
        verify(refreshTokenStore, never()).save(any(), any(), any());
    }

    @Test
    void login_invalidPassword_throwsInvalidLoginException() {
        Member member = createMember();
        LoginRequest request = new LoginRequest("member@test.com", "plain-password");

        when(memberRepository.findByEmail("member@test.com")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("plain-password", "encoded-password")).thenReturn(false);

        assertThrows(InvalidLoginException.class, () -> authService.login(request));
    }

    @Test
    void login_pendingVerification_throwsIllegalStateException() {
        Member member = createMember(MemberStatus.PENDING_VERIFICATION);
        LoginRequest request = new LoginRequest("member@test.com", "plain-password");

        when(memberRepository.findByEmail("member@test.com")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("plain-password", "encoded-password")).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> authService.login(request));

        verify(jwtTokenProvider, never()).createAccessToken(member);
        verify(refreshTokenStore, never()).save(any(), any(), any());
    }

    @Test
    void refresh_activeMember_returnsNewAccessToken() {
        Member member = createMember(MemberStatus.ACTIVE);
        UUID memberId = member.getMemberId();
        TokenRefreshRequest request = new TokenRefreshRequest("refresh-token");

        when(jwtTokenProvider.extractMemberId("refresh-token")).thenReturn(memberId);
        when(memberRestrictionService.getActiveLoginRestriction(org.mockito.ArgumentMatchers.eq(memberId), any()))
                .thenReturn(null);
        when(refreshTokenStore.findByMemberId(memberId)).thenReturn(Optional.of("refresh-token"));
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(jwtTokenProvider.createAccessToken(member)).thenReturn("new-access-token");
        when(jwtTokenProvider.getAccessExpiration()).thenReturn(3600L);
        when(jwtTokenProvider.getRefreshExpiration()).thenReturn(7200L);

        TokenRefreshResponse response = authService.refresh(request);

        assertEquals("new-access-token", response.accessToken());
        assertEquals("refresh-token", response.refreshToken());
    }

    @Test
    void refresh_withdrawnMember_throwsIllegalStateException() {
        Member member = createMember(MemberStatus.WITHDRAWN);
        UUID memberId = member.getMemberId();
        TokenRefreshRequest request = new TokenRefreshRequest("refresh-token");

        when(jwtTokenProvider.extractMemberId("refresh-token")).thenReturn(memberId);
        when(memberRestrictionService.getActiveLoginRestriction(org.mockito.ArgumentMatchers.eq(memberId), any()))
                .thenReturn(null);
        when(refreshTokenStore.findByMemberId(memberId)).thenReturn(Optional.of("refresh-token"));
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        assertThrows(IllegalStateException.class, () -> authService.refresh(request));

        verify(jwtTokenProvider, never()).createAccessToken(member);
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
