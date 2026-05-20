package com.example.payment.withdraw.infrastructure.crypto;

import org.springframework.stereotype.Service;

@Service
public class WithdrawAccountMaskingService {

    public String mask(String normalizedBankAccount) {
        int length = normalizedBankAccount.length();
        if (length <= 4) {
            return "****";
        }
        if (length <= 8) {
            return normalizedBankAccount.substring(0, 2) + "****"
                    + normalizedBankAccount.substring(length - 2);
        }
        return normalizedBankAccount.substring(0, 3) + "-****-"
                + normalizedBankAccount.substring(length - 4);
    }
}
