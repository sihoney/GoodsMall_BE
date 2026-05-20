package com.example.member.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.member.application.dto.command.ChangePasswordCommand;
import com.example.member.application.dto.command.CreateMemberCommand;
import com.example.member.application.dto.command.UpdateMemberCommand;
import com.example.member.application.dto.command.WithdrawMemberCommand;
import com.example.member.application.dto.result.CreateMemberResult;
import com.example.member.application.dto.result.MemberResult;
import com.example.member.application.dto.result.WithdrawMemberResult;
import com.example.member.application.port.in.AuthUsecase;
import com.example.member.application.port.out.MemberEventPort;
import com.example.member.application.port.out.MemberOauthAccountPersistencePort;
import com.example.member.application.port.out.MemberPersistencePort;
import com.example.member.application.port.out.ProfileImageUrlPort;
import com.example.member.application.port.out.MemberWithdrawalCheckPort;
import com.example.member.common.exception.DuplicateMemberEmailException;
import com.example.member.common.exception.InvalidCurrentPasswordException;
import com.example.member.common.exception.MemberWithdrawalException;
import com.example.member.config.MemberSignupProperties;
import com.example.member.domain.entity.Member;
import com.example.member.domain.entity.MemberOauthAccount;
import com.example.member.domain.enumtype.OAuthProvider;
import com.example.member.domain.enumtype.MemberStatus;
import com.todaylunch.common.security.auth.enumtype.MemberRole;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberPersistencePort memberPersistencePort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MemberEventPort memberEventPort;

    @Mock
    private ProfileImageUrlPort profileImageUrlPort;

    @Mock
    private EmailVerificationService emailVerificationService;

    @Mock
    private KakaoOAuthService kakaoOAuthService;

    @Mock
    private MemberSignupProperties memberSignupProperties;

    @Mock
    private AuthUsecase authUsecase;

    @Mock
    private MemberWithdrawalCheckPort memberWithdrawalCheckPort;

    @Mock
    private MemberOauthAccountPersistencePort memberOauthAccountPersistencePort;

    @InjectMocks
    private MemberService memberService;

    @Test
    void createMember_success_savesEncodedMemberAndPublishesEvent() {
        CreateMemberCommand command = new CreateMemberCommand(
                "member@test.com",
                "plain-password",
                "tester",
                "010-1111-2222",
                "Seoul",
                "members/profile/profile.png",
                MemberRole.USER,
                null
        );

        when(memberPersistencePort.existsByEmail("member@test.com")).thenReturn(false);
        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");
        when(memberPersistencePort.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(profileImageUrlPort.isSupportedKey("members/profile/profile.png")).thenReturn(true);
        when(profileImageUrlPort.resolve("members/profile/profile.png"))
                .thenReturn("https://cdn.test/members/profile/profile.png");
        when(memberSignupProperties.requireEmailVerification()).thenReturn(true);
        when(emailVerificationService.createSignupVerification(any(Member.class))).thenReturn(null);

        CreateMemberResult response = memberService.createMember(command);

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberPersistencePort).save(memberCaptor.capture());
        verify(memberEventPort).publishMemberSignedUp(memberCaptor.getValue());
        verify(emailVerificationService).createSignupVerification(memberCaptor.getValue());

        Member savedMember = memberCaptor.getValue();
        assertEquals("member@test.com", savedMember.getEmail());
        assertEquals("encoded-password", savedMember.getPassword());
        assertEquals("tester", savedMember.getNickname());
        assertEquals("members/profile/profile.png", savedMember.getProfileImageKey());
        assertEquals(MemberRole.USER, savedMember.getRole());
        assertEquals(MemberStatus.PENDING_VERIFICATION, savedMember.getStatus());
        assertEquals(savedMember.getMemberId(), response.memberId());
        assertEquals(savedMember.getNickname(), response.nickname());
        assertEquals("https://cdn.test/members/profile/profile.png", response.profileImageUrl());
    }

    @Test
    void createMember_whenEmailVerificationDisabled_savesActiveMemberWithoutSendingEmail() {
        CreateMemberCommand command = new CreateMemberCommand(
                "local@test.com",
                "plain-password",
                "local-user",
                null,
                null,
                null,
                MemberRole.USER,
                null
        );

        when(memberPersistencePort.existsByEmail("local@test.com")).thenReturn(false);
        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");
        when(memberPersistencePort.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(profileImageUrlPort.resolve(null)).thenReturn(null);
        when(memberSignupProperties.requireEmailVerification()).thenReturn(false);

        CreateMemberResult response = memberService.createMember(command);

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberPersistencePort).save(memberCaptor.capture());
        verify(memberEventPort).publishMemberSignedUp(memberCaptor.getValue());
        verify(emailVerificationService, never()).createSignupVerification(any(Member.class));

        Member savedMember = memberCaptor.getValue();
        assertEquals(MemberStatus.ACTIVE, savedMember.getStatus());
        assertEquals(savedMember.getMemberId(), response.memberId());
    }

    @Test
    void createMember_withKakaoLinkToken_linksOauthAccountAfterSavingMember() {
        CreateMemberCommand command = new CreateMemberCommand(
                "kakao@test.com",
                "plain-password",
                "kakao-user",
                null,
                null,
                null,
                MemberRole.USER,
                "pending-kakao-link-token"
        );

        when(memberPersistencePort.existsByEmail("kakao@test.com")).thenReturn(false);
        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");
        when(memberPersistencePort.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(profileImageUrlPort.resolve(null)).thenReturn(null);
        when(memberSignupProperties.requireEmailVerification()).thenReturn(false);

        CreateMemberResult response = memberService.createMember(command);

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberPersistencePort).save(memberCaptor.capture());
        verify(kakaoOAuthService).linkPendingSignupMember(
                memberCaptor.getValue().getMemberId(),
                "pending-kakao-link-token"
        );
        assertEquals(memberCaptor.getValue().getMemberId(), response.memberId());
    }

    @Test
    void createMember_duplicateEmail_throwsException() {
        CreateMemberCommand command = new CreateMemberCommand(
                "member@test.com",
                "plain-password",
                "tester",
                null,
                null,
                null,
                MemberRole.USER,
                null
        );

        when(memberPersistencePort.existsByEmail("member@test.com")).thenReturn(true);

        assertThrows(DuplicateMemberEmailException.class, () -> memberService.createMember(command));

        verify(memberPersistencePort, never()).save(any(Member.class));
        verify(memberEventPort, never()).publishMemberSignedUp(any(Member.class));
    }

    @Test
    void createMember_invalidProfileImageKey_throwsException() {
        CreateMemberCommand command = new CreateMemberCommand(
                "member@test.com",
                "plain-password",
                "tester",
                null,
                null,
                "invalid/profile.png",
                MemberRole.USER,
                null
        );

        when(memberPersistencePort.existsByEmail("member@test.com")).thenReturn(false);
        when(profileImageUrlPort.isSupportedKey("invalid/profile.png")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> memberService.createMember(command));

        verify(memberPersistencePort, never()).save(any(Member.class));
        verify(memberEventPort, never()).publishMemberSignedUp(any(Member.class));
    }

    @Test
    void updateMember_withoutProfileImageKey_keepsExistingProfileImageKey() {
        UUID memberId = UUID.randomUUID();
        Member member = Member.create(
                memberId,
                "member@test.com",
                "encoded-password",
                "tester",
                null,
                null,
                "members/profile/existing.png",
                MemberRole.USER,
                MemberStatus.ACTIVE,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(1)
        );
        UpdateMemberCommand command = new UpdateMemberCommand(
                memberId,
                "updated-tester",
                "010-1111-2222",
                "Seoul",
                null
        );

        when(memberPersistencePort.findById(memberId)).thenReturn(Optional.of(member));
        when(profileImageUrlPort.resolve("members/profile/existing.png"))
                .thenReturn("https://cdn.test/members/profile/existing.png");

        MemberResult result = memberService.updateMember(command);

        assertEquals("members/profile/existing.png", member.getProfileImageKey());
        assertEquals("updated-tester", result.nickname());
        assertEquals("010-1111-2222", result.phone());
        assertEquals("Seoul", result.address());
        assertEquals("member@test.com", result.email());
    }

    @Test
    void changeCurrentMemberPassword_success_updatesEncodedPassword() {
        UUID memberId = UUID.randomUUID();
        Member member = Member.create(
                memberId,
                "member@test.com",
                "encoded-current-password",
                "tester",
                null,
                null,
                null,
                MemberRole.USER,
                MemberStatus.ACTIVE,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(1)
        );
        ChangePasswordCommand command = new ChangePasswordCommand(memberId, "current-password", "new-password");

        when(memberPersistencePort.findById(memberId)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("current-password", "encoded-current-password")).thenReturn(true);
        when(passwordEncoder.encode("new-password")).thenReturn("encoded-new-password");

        memberService.changeCurrentMemberPassword(command);

        assertEquals("encoded-new-password", member.getPassword());
    }

    @Test
    void changeCurrentMemberPassword_invalidCurrentPassword_throwsException() {
        UUID memberId = UUID.randomUUID();
        Member member = Member.create(
                memberId,
                "member@test.com",
                "encoded-current-password",
                "tester",
                null,
                null,
                null,
                MemberRole.USER,
                MemberStatus.ACTIVE,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(1)
        );
        ChangePasswordCommand command = new ChangePasswordCommand(memberId, "wrong-password", "new-password");

        when(memberPersistencePort.findById(memberId)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("wrong-password", "encoded-current-password")).thenReturn(false);

        assertThrows(InvalidCurrentPasswordException.class,
                () -> memberService.changeCurrentMemberPassword(command));
    }

    @Test
    void changeCurrentMemberPassword_shortNewPassword_throwsException() {
        UUID memberId = UUID.randomUUID();
        Member member = Member.create(
                memberId,
                "member@test.com",
                "encoded-current-password",
                "tester",
                null,
                null,
                null,
                MemberRole.USER,
                MemberStatus.ACTIVE,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(1)
        );
        ChangePasswordCommand command = new ChangePasswordCommand(memberId, "current-password", "short");

        when(memberPersistencePort.findById(memberId)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("current-password", "encoded-current-password")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> memberService.changeCurrentMemberPassword(command));
    }

    @Test
    void withdrawCurrentMember_success_changesStatusAndLogsOutAllSessions() {
        UUID memberId = UUID.randomUUID();
        Member member = Member.create(
                memberId,
                "member@test.com",
                "encoded-current-password",
                "tester",
                null,
                null,
                null,
                MemberRole.USER,
                MemberStatus.ACTIVE,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(1)
        );
        WithdrawMemberCommand command = new WithdrawMemberCommand(
                memberId,
                "current-password",
                "Bearer access-token"
        );

        when(memberPersistencePort.findById(memberId)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("current-password", "encoded-current-password")).thenReturn(true);
        MemberOauthAccount oauthAccount = MemberOauthAccount.create(
                UUID.randomUUID(),
                memberId,
                OAuthProvider.KAKAO,
                "provider-user-id",
                "member@test.com",
                "tester",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(1)
        );
        when(memberOauthAccountPersistencePort.findAllByMemberId(memberId)).thenReturn(List.of(oauthAccount));

        WithdrawMemberResult result = memberService.withdrawCurrentMember(command);

        assertEquals(MemberStatus.WITHDRAWN, member.getStatus());
        assertEquals("withdrawn+" + memberId + "@deleted.local", member.getEmail());
        assertEquals(MemberStatus.WITHDRAWN, result.status());
        verify(memberWithdrawalCheckPort).validateWithdrawable(member, "Bearer access-token");
        verify(memberOauthAccountPersistencePort).delete(oauthAccount);
        verify(authUsecase).logoutAllSessions("Bearer access-token");
    }

    @Test
    void withdrawCurrentMember_admin_throwsMemberWithdrawalException() {
        UUID memberId = UUID.randomUUID();
        Member member = Member.create(
                memberId,
                "admin@test.com",
                "encoded-current-password",
                "admin",
                null,
                null,
                null,
                MemberRole.ADMIN,
                MemberStatus.ACTIVE,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(1)
        );
        WithdrawMemberCommand command = new WithdrawMemberCommand(
                memberId,
                "current-password",
                "Bearer access-token"
        );

        when(memberPersistencePort.findById(memberId)).thenReturn(Optional.of(member));

        MemberWithdrawalException exception = assertThrows(
                MemberWithdrawalException.class,
                () -> memberService.withdrawCurrentMember(command)
        );
        assertEquals("MEMBER_WITHDRAWAL_ADMIN_FORBIDDEN", exception.getCode());
        verify(memberWithdrawalCheckPort, never()).validateWithdrawable(any(), any());
        verify(authUsecase, never()).logoutAllSessions(any());
    }

    @Test
    void withdrawCurrentMember_invalidCurrentPassword_throwsException() {
        UUID memberId = UUID.randomUUID();
        Member member = Member.create(
                memberId,
                "member@test.com",
                "encoded-current-password",
                "tester",
                null,
                null,
                null,
                MemberRole.USER,
                MemberStatus.ACTIVE,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(1)
        );
        WithdrawMemberCommand command = new WithdrawMemberCommand(
                memberId,
                "wrong-password",
                "Bearer access-token"
        );

        when(memberPersistencePort.findById(memberId)).thenReturn(Optional.of(member));
        when(passwordEncoder.matches("wrong-password", "encoded-current-password")).thenReturn(false);

        MemberWithdrawalException exception = assertThrows(
                MemberWithdrawalException.class,
                () -> memberService.withdrawCurrentMember(command)
        );
        assertEquals("MEMBER_WITHDRAWAL_PASSWORD_INVALID", exception.getCode());
        verify(memberWithdrawalCheckPort).validateWithdrawable(member, "Bearer access-token");
        verify(authUsecase, never()).logoutAllSessions(any());
    }

    @Test
    void withdrawCurrentMember_whenMemberNotActive_throwsMemberWithdrawalException() {
        UUID memberId = UUID.randomUUID();
        Member member = Member.create(
                memberId,
                "member@test.com",
                "encoded-current-password",
                "tester",
                null,
                null,
                null,
                MemberRole.USER,
                MemberStatus.SUSPENDED,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(1)
        );
        WithdrawMemberCommand command = new WithdrawMemberCommand(
                memberId,
                "current-password",
                "Bearer access-token"
        );

        when(memberPersistencePort.findById(memberId)).thenReturn(Optional.of(member));

        MemberWithdrawalException exception = assertThrows(
                MemberWithdrawalException.class,
                () -> memberService.withdrawCurrentMember(command)
        );
        assertEquals("MEMBER_WITHDRAWAL_NOT_ACTIVE", exception.getCode());
        verify(memberWithdrawalCheckPort, never()).validateWithdrawable(any(), any());
        verify(authUsecase, never()).logoutAllSessions(any());
    }
}
