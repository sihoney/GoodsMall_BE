package com.example.member.auth.application.service;

import com.example.member.auth.application.dto.command.PasswordResetConfirmCommand;
import com.example.member.auth.application.dto.command.PasswordResetSendCommand;
import com.example.member.auth.application.dto.result.PasswordResetConfirmResult;
import com.example.member.auth.application.dto.result.PasswordResetSendResult;
import com.example.member.verification.application.port.out.EmailSenderPort;
import com.example.member.member.application.port.out.MemberPersistencePort;
import com.example.member.common.exception.InvalidPasswordResetTokenException;
import com.example.member.common.config.PasswordResetProperties;
import com.example.member.member.domain.entity.Member;
import com.example.member.auth.infrastructure.redis.passwordreset.PasswordResetToken;
import com.example.member.auth.infrastructure.redis.passwordreset.PasswordResetTokenStore;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PasswordResetService {

    private final MemberPersistencePort memberPersistencePort;
    private final PasswordResetTokenStore passwordResetTokenStore;
    private final EmailSenderPort emailSender;
    private final PasswordResetProperties passwordResetProperties;
    private final PasswordEncoder passwordEncoder;

    public PasswordResetSendResult sendPasswordReset(PasswordResetSendCommand command) {
        String email = normalizeRequired(command == null ? null : command.email(), "email");

        memberPersistencePort.findByEmail(email).ifPresent(this::createAndSendToken);
        return new PasswordResetSendResult("이메일이 존재하면 비밀번호 재설정 안내를 발송했습니다.");
    }

    @Transactional
    public PasswordResetConfirmResult confirmPasswordReset(PasswordResetConfirmCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("비밀번호 재설정 확인 요청은 필수입니다.");
        }

        String token = normalizeRequired(command.token(), "token");
        String newPassword = normalizeRequired(command.newPassword(), "newPassword");
        validateNewPassword(newPassword);

        PasswordResetToken passwordResetToken = passwordResetTokenStore.find(token)
                .orElseThrow(InvalidPasswordResetTokenException::new);

        Member member = memberPersistencePort.findById(passwordResetToken.memberId())
                .orElseThrow(InvalidPasswordResetTokenException::new);

        member.changePassword(passwordEncoder.encode(newPassword), LocalDateTime.now());
        passwordResetTokenStore.delete(token);

        return new PasswordResetConfirmResult("비밀번호가 재설정되었습니다.");
    }

    private void createAndSendToken(Member member) {
        String token = UUID.randomUUID().toString();
        PasswordResetToken passwordResetToken = new PasswordResetToken(
                token,
                member.getMemberId(),
                member.getEmail(),
                LocalDateTime.now().toInstant(ZoneOffset.UTC)
        );
        passwordResetTokenStore.create(passwordResetToken, passwordResetProperties.expiration());

        emailSender.send(
                member.getEmail(),
                buildSubject(),
                buildBody(member.getEmail(), token),
                true
        );
    }

    private void validateNewPassword(String newPassword) {
        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("새 비밀번호는 8자 이상이어야 합니다.");
        }
    }

    private String buildSubject() {
        return "[Goods Mall] 비밀번호 재설정 안내";
    }

    private String buildBody(String email, String token) {
        String resetUrl = "%s?token=%s".formatted(passwordResetProperties.frontendResetUrl(), token);
        long expirationMinutes = passwordResetProperties.expiration().toMinutes();

        return """
                <!DOCTYPE html>
                <html lang="ko">
                <head>
                    <meta charset="UTF-8" />
                    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                    <title>Goods Mall 비밀번호 재설정</title>
                </head>
                <body style="margin:0; padding:0; background-color:#f6f1ff; font-family:'Apple SD Gothic Neo','Malgun Gothic',sans-serif; color:#2f2340;">
                    <div style="max-width:640px; margin:0 auto; padding:32px 20px;">
                        <div style="background-color:#ffffff; border-radius:24px; padding:40px 32px; box-shadow:0 16px 40px rgba(106,79,155,0.12);">
                            <div style="display:inline-block; padding:10px 14px; border-radius:16px; background-color:#efe4ff; color:#6d28d9; font-weight:700; font-size:14px;">
                                Goods Mall
                            </div>
                            <h1 style="margin:24px 0 12px; font-size:28px; line-height:1.35; color:#241533;">
                                비밀번호를 다시 설정해 주세요
                            </h1>
                            <p style="margin:0 0 16px; font-size:16px; line-height:1.7; color:#5a4a6a;">
                                아래 버튼을 눌러 새 비밀번호를 설정해 주세요.
                            </p>

                            <div style="margin:24px 0; padding:18px 20px; border-radius:18px; background-color:#f7f3ff;">
                                <div style="font-size:12px; font-weight:700; letter-spacing:0.08em; text-transform:uppercase; color:#7c5ab8;">
                                    재설정 대상 이메일
                                </div>
                                <div style="margin-top:8px; font-size:16px; font-weight:600; color:#2f2340; word-break:break-all;">
                                    %s
                                </div>
                            </div>

                            <div style="margin:32px 0;">
                                <a href="%s" style="display:inline-block; padding:16px 24px; border-radius:16px; background-color:#7c3aed; color:#ffffff; text-decoration:none; font-size:16px; font-weight:700;">
                                    비밀번호 재설정하기
                                </a>
                            </div>

                            <p style="margin:0 0 12px; font-size:14px; line-height:1.7; color:#5a4a6a;">
                                버튼이 동작하지 않으면 아래 링크를 복사해 브라우저 주소창에 붙여 넣어 주세요.
                            </p>
                            <p style="margin:0 0 24px; font-size:14px; line-height:1.7; color:#6d28d9; word-break:break-all;">
                                %s
                            </p>

                            <p style="margin:0; font-size:14px; line-height:1.7; color:#5a4a6a;">
                                이 링크는 %d분 동안만 유효합니다.<br />
                                본인이 요청하지 않았다면 이 메일을 무시해 주세요.
                            </p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(email, resetUrl, resetUrl, expirationMinutes);
    }

    private String normalizeRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + "은(는) 필수입니다.");
        }
        return value.trim();
    }
}
