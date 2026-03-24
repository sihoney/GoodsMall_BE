package com.example.payment.domain.repository;

import com.example.payment.domain.entity.WalletTransaction;

public interface WalletTransactionRepository {

    WalletTransaction save(WalletTransaction walletTransaction);
}
