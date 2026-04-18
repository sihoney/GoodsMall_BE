package com.example.payment.application.dto;

import java.util.UUID;

public record WithdrawCommand(
        UUID memberId,
        Long amount,
        String bankAccount,
        String accountHolder
) {
}
