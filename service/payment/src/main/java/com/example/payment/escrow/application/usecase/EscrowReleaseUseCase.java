package com.example.payment.escrow.application.usecase;

import com.example.payment.escrow.application.dto.EscrowReleaseCommand;
import com.example.payment.escrow.application.dto.EscrowReleaseResult;

/**
 * escrow release ?좎뒪耳?댁뒪??吏꾩엯?먯씠??
 */
public interface EscrowReleaseUseCase {

    EscrowReleaseResult releaseEscrow(EscrowReleaseCommand command);
}
