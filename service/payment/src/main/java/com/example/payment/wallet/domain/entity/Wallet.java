package com.example.payment.wallet.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "wallet", schema = "payment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
/**
 * ?뚯썝??payment ?붿븸??愿由ы븯??wallet aggregate??
 * ?붿븸 利앷컧怨?理쒖쥌 balance 諛섏쁺 洹쒖튃???뷀떚???대??먯꽌 蹂댁옣?쒕떎.
 */
public class Wallet {

    @Id
    @Column(name = "wallet_id", nullable = false, updatable = false)
    private UUID walletId;

    @Column(name = "member_id", nullable = false, unique = true)
    private UUID memberId;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private Wallet(UUID walletId, UUID memberId, BigDecimal balance, LocalDateTime updatedAt, LocalDateTime createdAt) {
        this.walletId = Objects.requireNonNull(walletId);
        this.memberId = Objects.requireNonNull(memberId);
        this.balance = Objects.requireNonNull(balance);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    public static Wallet create(
            UUID walletId,
            UUID memberId,
            BigDecimal balance,
            LocalDateTime updatedAt,
            LocalDateTime createdAt
    ) {
        return new Wallet(walletId, memberId, balance, updatedAt, createdAt);
    }

    /**
     * ?몃??먯꽌 怨꾩궛??理쒖쥌 balance瑜?洹몃?濡?諛섏쁺?쒕떎.
     */
    public void applyTransaction(BigDecimal balanceAfter, LocalDateTime updatedAt) {
        this.balance = Objects.requireNonNull(balanceAfter);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    /**
     * wallet ?붿븸??利앷??쒗궎怨?諛섏쁺 ??balance瑜?諛섑솚?쒕떎.
     */
    public BigDecimal increaseBalance(BigDecimal amount, LocalDateTime updatedAt) {
        Objects.requireNonNull(amount);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("湲덉븸? 0蹂대떎 而ㅼ빞 ?⑸땲??");
        }

        this.balance = balance.add(amount);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        return this.balance;
    }

    /**
     * wallet ?붿븸??李④컧?섍퀬 諛섏쁺 ??balance瑜?諛섑솚?쒕떎.
     * 遺議깊븳 ?붿븸?쇰줈??李④컧?????녿떎.
     */
    public BigDecimal decreaseBalance(BigDecimal amount, LocalDateTime updatedAt) {
        Objects.requireNonNull(amount);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("湲덉븸? 0蹂대떎 而ㅼ빞 ?⑸땲??");
        }
        if (balance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("?붿븸??遺議깊빀?덈떎.");
        }

        this.balance = balance.subtract(amount);
        this.updatedAt = Objects.requireNonNull(updatedAt);
        return this.balance;
    }
}
