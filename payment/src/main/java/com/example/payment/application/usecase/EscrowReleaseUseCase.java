package com.example.payment.application.usecase;

import com.example.payment.application.dto.EscrowReleaseCommand;
import com.example.payment.application.dto.EscrowReleaseResult;

public interface EscrowReleaseUseCase {

    EscrowReleaseResult releaseEscrow(EscrowReleaseCommand command);
}
