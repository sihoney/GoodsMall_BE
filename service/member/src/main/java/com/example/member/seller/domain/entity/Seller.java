package com.example.member.seller.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "seller")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Seller {

    @Id
    @Column(name = "seller_id", nullable = false, updatable = false)
    private UUID sellerId;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "account")
    private String account;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    private Seller(UUID sellerId, UUID memberId, String bankName, String account, LocalDateTime approvedAt) {
        this.sellerId = Objects.requireNonNull(sellerId);
        this.memberId = Objects.requireNonNull(memberId);
        this.bankName = bankName;
        this.account = account;
        this.approvedAt = approvedAt;
    }

    public static Seller create(UUID sellerId, UUID memberId, String bankName, String account, LocalDateTime approvedAt) {
        return new Seller(sellerId, memberId, bankName, account, approvedAt);
    }

    public void updateSettlementAccount(String bankName, String account) {
        this.bankName = bankName;
        this.account = account;
    }
}
