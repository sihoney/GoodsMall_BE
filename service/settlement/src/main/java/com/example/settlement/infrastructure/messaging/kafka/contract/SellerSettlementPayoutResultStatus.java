package com.example.settlement.infrastructure.messaging.kafka.contract;

/**
 * payment -> settlement 지급 결과 상태(status) 계약(contract)이다.
 */
public enum SellerSettlementPayoutResultStatus {
    SUCCESS,
    FAILED
}

