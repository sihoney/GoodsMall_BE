package com.example.payment.application.usecase;

import com.example.payment.application.dto.EscrowReleaseScheduleCommand;
import com.example.payment.application.dto.EscrowReleaseScheduleResult;

/**
 * escrow 자동 구매확정 시점 예약 유스케이스의 진입점이다.
 */
public interface EscrowReleaseScheduleUseCase {

    EscrowReleaseScheduleResult scheduleRelease(EscrowReleaseScheduleCommand command);
}
