package com.example.member.application.usecase;

import com.example.member.presentation.dto.AccountVerificationCancelResponse;
import com.example.member.presentation.dto.AccountVerificationConfirmRequest;
import com.example.member.presentation.dto.AccountVerificationConfirmResponse;
import com.example.member.presentation.dto.AccountVerificationCreateRequest;
import com.example.member.presentation.dto.AccountVerificationCurrentResponse;
import com.example.member.presentation.dto.AccountVerificationSendResponse;
import java.util.UUID;

public interface AccountVerificationUsecase {

    AccountVerificationSendResponse createAccountVerification(UUID memberId, AccountVerificationCreateRequest request);

    AccountVerificationConfirmResponse confirmAccountVerification(
            UUID memberId,
            String sessionId,
            AccountVerificationConfirmRequest request
    );

    AccountVerificationCurrentResponse getCurrentAccountVerification(UUID memberId);

    AccountVerificationSendResponse resendAccountVerification(UUID memberId, String sessionId);

    AccountVerificationCancelResponse cancelAccountVerification(UUID memberId, String sessionId);
}
