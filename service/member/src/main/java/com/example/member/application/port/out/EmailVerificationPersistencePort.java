package com.example.member.application.port.out;

import com.example.member.domain.entity.EmailVerification;
import com.example.member.domain.enumtype.EmailVerificationPurpose;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationPersistencePort {

    EmailVerification save(EmailVerification emailVerification);

    Optional<EmailVerification> findById(UUID verificationId);

    Optional<EmailVerification> findByToken(String token);

    List<EmailVerification> findPendingByMemberIdAndPurpose(UUID memberId, EmailVerificationPurpose purpose);

    List<EmailVerification> findPendingByEmailAndPurpose(String email, EmailVerificationPurpose purpose);
}
