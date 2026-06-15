package com.example.payment.wallet.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("payment test")
class WalletTest {

    private UUID walletId;
    private UUID memberId;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        walletId = UUID.randomUUID();
        memberId = UUID.randomUUID();
        now = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
    }

    private BigDecimal amount(long value) {
        return BigDecimal.valueOf(value);
    }

    private Wallet createWallet(long initialBalance) {
        return Wallet.create(walletId, memberId, amount(initialBalance), now, now);
    }

    @Nested
    @DisplayName("payment test")
    class Create {

        @Test
        @DisplayName("payment test")
        void create_setsInitialBalance() {
            Wallet wallet = createWallet(5_000L);

            assertThat(wallet.getBalance()).isEqualTo(amount(5_000L));
        }

        @Test
        @DisplayName("payment test")
        void create_setsMemberId() {
            Wallet wallet = createWallet(0L);

            assertThat(wallet.getMemberId()).isEqualTo(memberId);
            assertThat(wallet.getWalletId()).isEqualTo(walletId);
        }
    }

    @Nested
    @DisplayName("payment test")
    class IncreaseBalance {

        @Test
        @DisplayName("payment test")
        void increaseBalance_addsAmountToBalance() {
            Wallet wallet = createWallet(10_000L);
            LocalDateTime updatedAt = now.plusMinutes(5);

            BigDecimal newBalance = wallet.increaseBalance(amount(5_000L), updatedAt);

            assertThat(newBalance).isEqualTo(amount(15_000L));
            assertThat(wallet.getBalance()).isEqualTo(amount(15_000L));
        }

        @Test
        @DisplayName("payment test")
        void increaseBalance_updatesUpdatedAt() {
            Wallet wallet = createWallet(0L);
            LocalDateTime updatedAt = now.plusMinutes(10);

            wallet.increaseBalance(amount(1_000L), updatedAt);

            assertThat(wallet.getUpdatedAt()).isEqualTo(updatedAt);
        }

        @Test
        @DisplayName("payment test")
        void increaseBalance_zeroAmount_throwsException() {
            Wallet wallet = createWallet(10_000L);

            assertThatThrownBy(() -> wallet.increaseBalance(amount(0L), now))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("payment test")
        void increaseBalance_negativeAmount_throwsException() {
            Wallet wallet = createWallet(10_000L);

            assertThatThrownBy(() -> wallet.increaseBalance(amount(-1_000L), now))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("payment test")
        void increaseBalance_multipleTimes_accumulates() {
            Wallet wallet = createWallet(0L);

            wallet.increaseBalance(amount(10_000L), now.plusMinutes(1));
            wallet.increaseBalance(amount(5_000L), now.plusMinutes(2));

            assertThat(wallet.getBalance()).isEqualTo(amount(15_000L));
        }
    }

    @Nested
    @DisplayName("payment test")
    class ApplyTransaction {

        @Test
        @DisplayName("payment test")
        void applyTransaction_updatesBalanceAndTimestamp() {
            Wallet wallet = createWallet(10_000L);
            LocalDateTime updatedAt = now.plusMinutes(5);

            wallet.applyTransaction(amount(20_000L), updatedAt);

            assertThat(wallet.getBalance()).isEqualTo(amount(20_000L));
            assertThat(wallet.getUpdatedAt()).isEqualTo(updatedAt);
        }
    }

    @Nested
    @DisplayName("payment test")
    class DecreaseBalance {

        @Test
        @DisplayName("payment test")
        void decreaseBalance_subtractsAmountFromBalance() {
            Wallet wallet = createWallet(10_000L);
            LocalDateTime updatedAt = now.plusMinutes(5);

            BigDecimal newBalance = wallet.decreaseBalance(amount(4_000L), updatedAt);

            assertThat(newBalance).isEqualTo(amount(6_000L));
            assertThat(wallet.getBalance()).isEqualTo(amount(6_000L));
            assertThat(wallet.getUpdatedAt()).isEqualTo(updatedAt);
        }

        @Test
        @DisplayName("payment test")
        void decreaseBalance_insufficientBalance_throwsException() {
            Wallet wallet = createWallet(3_000L);

            assertThatThrownBy(() -> wallet.decreaseBalance(amount(4_000L), now))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}

