package com.example.member.infrastructure.persistence.jpa;

import com.example.member.application.port.out.EmailVerificationPersistencePort;
import com.example.member.domain.entity.EmailVerification;
import com.example.member.domain.enumtype.EmailVerificationPurpose;
import com.example.member.domain.enumtype.EmailVerificationStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EmailVerificationJpaAdapter implements EmailVerificationPersistencePort {

    private final EmailVerificationJpaRepository emailVerificationJpaRepository;

    @Override
    public EmailVerification save(EmailVerification emailVerification) {
        return emailVerificationJpaRepository.save(emailVerification);
    }

    @Override
    public Optional<EmailVerification> findById(UUID verificationId) {
        return emailVerificationJpaRepository.findById(verificationId);
    }

    @Override
    public Optional<EmailVerification> findByToken(String token) {
        return emailVerificationJpaRepository.findByToken(token);
    }

    @Override
    public List<EmailVerification> findPendingByMemberIdAndPurpose(
            UUID memberId,
            EmailVerificationPurpose purpose
    ) {
        return emailVerificationJpaRepository.findAllByMemberIdAndPurposeAndStatus(
                memberId,
                purpose,
                EmailVerificationStatus.PENDING
        );
    }

    @Override
    public List<EmailVerification> findPendingByEmailAndPurpose(
            String email,
            EmailVerificationPurpose purpose
    ) {
        return emailVerificationJpaRepository.findAllByEmailAndPurposeAndStatus(
                email,
                purpose,
                EmailVerificationStatus.PENDING
        );
    }
}
