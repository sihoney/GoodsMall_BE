package com.example.payment.application.usecase;

import com.example.payment.application.dto.EscrowReleaseCommand;
import com.example.payment.application.dto.EscrowReleaseResult;

/**
 * escrow release 유스케이스의 진입점이다.
 */
public interface EscrowReleaseUseCase {

    EscrowReleaseResult releaseEscrow(EscrowReleaseCommand command);
}
