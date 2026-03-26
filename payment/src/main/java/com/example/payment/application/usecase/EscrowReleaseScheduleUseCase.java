package com.example.payment.application.usecase;

import com.example.payment.application.dto.EscrowReleaseScheduleCommand;
import com.example.payment.application.dto.EscrowReleaseScheduleResult;

public interface EscrowReleaseScheduleUseCase {

    EscrowReleaseScheduleResult scheduleRelease(EscrowReleaseScheduleCommand command);
}
