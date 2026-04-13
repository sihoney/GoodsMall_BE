package com.example.member.application.service;

import com.example.member.config.EmailVerificationProperties;
import com.example.member.common.exception.EmailVerificationNotAllowedException;
import com.example.member.common.exception.ExpiredEmailVerificationException;
import com.example.member.common.exception.InvalidEmailVerificationTokenException;
import com.example.member.domain.entity.EmailVerification;
import com.example.member.domain.entity.Member;
import com.example.member.domain.enumtype.EmailVerificationPurpose;
import com.example.member.domain.enumtype.MemberStatus;
import com.example.member.infrastructure.email.EmailSender;
import com.example.member.infrastructure.repository.EmailVerificationRepository;
import com.example.member.infrastructure.repository.MemberRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmailVerificationService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final MemberRepository memberRepository;
    private final EmailSender emailSender;
    private final EmailVerificationProperties emailVerificationProperties;

    /**
     * 회원 가입 이메일 인증을 위한 EmailVerification 엔티티를 생성하고 저장한 후, 인증 이메일을 발송한다.
     * @param member
     * @return
     */
    @Transactional
    public EmailVerification createSignupVerification(Member member) {
        Member targetMember = validateSignupTarget(member);
        LocalDateTime now = LocalDateTime.now();

        cancelPendingSignupVerifications(targetMember.getMemberId(), now);

        EmailVerification emailVerification = EmailVerification.create(
                UUID.randomUUID(),
                targetMember.getMemberId(),
                targetMember.getEmail(),
                UUID.randomUUID().toString(),
                EmailVerificationPurpose.SIGNUP,
                now,
                now.plus(emailVerificationProperties.expiration())
        );

        EmailVerification saved = emailVerificationRepository.save(emailVerification);
        emailSender.send(
                targetMember.getEmail(),
                "[TodayLunch] Email verification",
                buildSignupVerificationBody(targetMember.getEmail(), saved.getToken())
        );
        return saved;
    }

    /**
     * 회원 가입 이메일 인증을 검증한다. 토큰이 유효한지, 만료되지 않았는지 확인한 후, 회원의 상태를 ACTIVE로 변경한다.
     * @param token
     * @return
     */
    @Transactional
    public Member confirmSignupVerification(String token) {
        String normalizedToken = normalizeRequired(token, "token");
        EmailVerification emailVerification = emailVerificationRepository.findByToken(normalizedToken)
                .orElseThrow(InvalidEmailVerificationTokenException::new);

        LocalDateTime now = LocalDateTime.now();
        if (emailVerification.isExpiredAt(now) && emailVerification.isPending()) {
            emailVerification.expire(now);
            throw new ExpiredEmailVerificationException();
        }

        Member member = memberRepository.findById(emailVerification.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("Member for email verification was not found."));

        if (member.getStatus() == MemberStatus.PENDING_VERIFICATION) {
            member.changeStatus(MemberStatus.ACTIVE, now);
        } else if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new EmailVerificationNotAllowedException(
                    "Current member status cannot be activated by email verification."
            );
        }

        emailVerification.verify(now);
        return member;
    }

    /**
     * 회원 가입 이메일 인증 토큰을 재발급한다. 기존에 PENDING_VERIFICATION 상태인 회원에 대해서만 재발급이 가능하다.
     * @param email
     * @return
     */
    @Transactional
    public EmailVerification resendSignupVerification(String email) {
        String normalizedEmail = normalizeRequired(email, "email");
        Member member = memberRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("Member not found for the email."));

        if (member.isActive()) {
            throw new EmailVerificationNotAllowedException(
                    "ACTIVE member does not need email verification."
            );
        }
        if (member.getStatus() != MemberStatus.PENDING_VERIFICATION) {
            throw new EmailVerificationNotAllowedException(
                    "Current member status cannot request signup email verification."
            );
        }

        return createSignupVerification(member);
    }

    private void cancelPendingSignupVerifications(UUID memberId, LocalDateTime now) {
        List<EmailVerification> pendingVerifications = emailVerificationRepository.findPendingByMemberIdAndPurpose(
                memberId,
                EmailVerificationPurpose.SIGNUP
        );
        for (EmailVerification pendingVerification : pendingVerifications) {
            pendingVerification.cancel(now);
        }
    }

    private Member validateSignupTarget(Member member) {
        if (member == null) {
            throw new IllegalArgumentException("member is required.");
        }
        if (member.getStatus() != MemberStatus.PENDING_VERIFICATION) {
            throw new EmailVerificationNotAllowedException(
                    "Signup email verification target must be PENDING_VERIFICATION."
            );
        }
        return member;
    }

    private String buildSignupVerificationBody(String email, String token) {
        return """
                Hello,

                Please verify your email address for TodayLunch.
                email: %s
                verificationUrl: %s?token=%s

                This link expires in %d minutes.
                """.formatted(
                email,
                emailVerificationProperties.frontendConfirmUrl(),
                token,
                emailVerificationProperties.expiration().toMinutes()
        );
    }

    private String normalizeRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return value.trim();
    }
}
