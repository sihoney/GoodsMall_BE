package com.example.member.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.member.application.dto.command.PasswordResetConfirmCommand;
import com.example.member.application.dto.command.PasswordResetSendCommand;
import com.example.member.common.exception.InvalidPasswordResetTokenException;
import com.example.member.config.PasswordResetProperties;
import com.example.member.domain.entity.Member;
import com.example.member.domain.enumtype.MemberStatus;
import com.example.member.infrastructure.email.EmailSender;
import com.example.member.infrastructure.persistence.jpa.MemberJpaAdapter;
import com.example.member.infrastructure.redis.passwordreset.PasswordResetToken;
import com.example.member.infrastructure.redis.passwordreset.PasswordResetTokenStore;
import com.todaylunch.common.security.auth.enumtype.MemberRole;
import java.time.Duration;
import java.time.Instant;
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
class PasswordResetServiceTest {

    @Mock
    private MemberJpaAdapter memberPersistencePort;

    @Mock
    private PasswordResetTokenStore passwordResetTokenStore;

    @Mock
    private EmailSender emailSender;

    @Mock
    private PasswordEncoder passwordEncoder;

    private final PasswordResetProperties passwordResetProperties = new PasswordResetProperties(
            Duration.ofMinutes(30),
            "http://localhost:5173/password-reset"
    );

    private PasswordResetService passwordResetService;

    @BeforeEach
    void setUp() {
        passwordResetService = new PasswordResetService(
                memberPersistencePort,
                passwordResetTokenStore,
                emailSender,
                passwordResetProperties,
                passwordEncoder
        );
    }

    @Test
    void sendPasswordReset_existingMember_storesTokenAndSendsEmail() {
        Member member = activeMember();
        when(memberPersistencePort.findByEmail("member@test.com")).thenReturn(Optional.of(member));
        when(passwordResetTokenStore.create(any(PasswordResetToken.class), eq(Duration.ofMinutes(30))))
                .thenReturn(Optional.of("token"));

        passwordResetService.sendPasswordReset(new PasswordResetSendCommand("member@test.com"));

        ArgumentCaptor<PasswordResetToken> tokenCaptor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenStore).create(tokenCaptor.capture(), eq(Duration.ofMinutes(30)));
        verify(emailSender).send(eq("member@test.com"), any(), any(), eq(true));

        PasswordResetToken storedToken = tokenCaptor.getValue();
        assertEquals(member.getMemberId(), storedToken.memberId());
        assertEquals("member@test.com", storedToken.email());
    }

    @Test
    void sendPasswordReset_unknownMember_returnsSuccessWithoutSendingEmail() {
        when(memberPersistencePort.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        passwordResetService.sendPasswordReset(new PasswordResetSendCommand("missing@test.com"));

        verify(passwordResetTokenStore, never()).create(any(), any());
        verify(emailSender, never()).send(any(), any(), any(), eq(true));
    }

    @Test
    void confirmPasswordReset_success_changesPasswordAndDeletesToken() {
        Member member = activeMember();
        PasswordResetToken token = new PasswordResetToken(
                "reset-token",
                member.getMemberId(),
                member.getEmail(),
                Instant.now()
        );

        when(passwordResetTokenStore.find("reset-token")).thenReturn(Optional.of(token));
        when(memberPersistencePort.findById(member.getMemberId())).thenReturn(Optional.of(member));
        when(passwordEncoder.encode("new-password")).thenReturn("encoded-new-password");

        passwordResetService.confirmPasswordReset(new PasswordResetConfirmCommand("reset-token", "new-password"));

        assertEquals("encoded-new-password", member.getPassword());
        verify(passwordResetTokenStore).delete("reset-token");
    }

    @Test
    void confirmPasswordReset_invalidToken_throwsException() {
        when(passwordResetTokenStore.find("missing-token")).thenReturn(Optional.empty());

        assertThrows(
                InvalidPasswordResetTokenException.class,
                () -> passwordResetService.confirmPasswordReset(
                        new PasswordResetConfirmCommand("missing-token", "new-password")
                )
        );
    }

    @Test
    void confirmPasswordReset_shortPassword_throwsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> passwordResetService.confirmPasswordReset(
                        new PasswordResetConfirmCommand("reset-token", "short")
                )
        );
    }

    private Member activeMember() {
        return Member.create(
                UUID.randomUUID(),
                "member@test.com",
                "encoded-password",
                "tester",
                null,
                null,
                null,
                MemberRole.USER,
                MemberStatus.ACTIVE,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().minusDays(1)
        );
    }
}
