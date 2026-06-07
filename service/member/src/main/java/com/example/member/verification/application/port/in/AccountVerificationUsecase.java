package com.example.member.verification.application.port.in;

import com.example.member.verification.application.dto.command.AccountVerificationConfirmCommand;
import com.example.member.verification.application.dto.command.AccountVerificationCreateCommand;
import com.example.member.verification.application.dto.result.AccountVerificationCancelResult;
import com.example.member.verification.application.dto.result.AccountVerificationConfirmResult;
import com.example.member.verification.application.dto.result.AccountVerificationCurrentResult;
import com.example.member.verification.application.dto.result.AccountVerificationSendResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public interface AccountVerificationUsecase {

    AccountVerificationSendResult createAccountVerification(UUID memberId, @Valid @NotNull AccountVerificationCreateCommand command);

    AccountVerificationConfirmResult confirmAccountVerification(
            UUID memberId,
            UUID authSessionId,
            String sessionId,
            @Valid @NotNull AccountVerificationConfirmCommand command
    );

    AccountVerificationCurrentResult getCurrentAccountVerification(UUID memberId);

    AccountVerificationSendResult resendAccountVerification(UUID memberId, String sessionId);

    AccountVerificationCancelResult cancelAccountVerification(UUID memberId, String sessionId);
}
