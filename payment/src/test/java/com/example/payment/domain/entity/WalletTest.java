package com.example.payment.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Wallet 도메인 테스트")
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
    @DisplayName("Wallet.create() 생성 테스트")
    class Create {

        @Test
        @DisplayName("Wallet 생성 시 초기 잔액이 올바르게 설정된다")
        void create_setsInitialBalance() {
            Wallet wallet = createWallet(5_000L);

            assertThat(wallet.getBalance()).isEqualTo(amount(5_000L));
        }

        @Test
        @DisplayName("Wallet 생성 시 memberId가 올바르게 저장된다")
        void create_setsMemberId() {
            Wallet wallet = createWallet(0L);

            assertThat(wallet.getMemberId()).isEqualTo(memberId);
            assertThat(wallet.getWalletId()).isEqualTo(walletId);
        }
    }

    @Nested
    @DisplayName("Wallet.increaseBalance() 잔액 증가 테스트")
    class IncreaseBalance {

        @Test
        @DisplayName("충전 금액만큼 잔액이 증가한다")
        void increaseBalance_addsAmountToBalance() {
            Wallet wallet = createWallet(10_000L);
            LocalDateTime updatedAt = now.plusMinutes(5);

            BigDecimal newBalance = wallet.increaseBalance(amount(5_000L), updatedAt);

            assertThat(newBalance).isEqualTo(amount(15_000L));
            assertThat(wallet.getBalance()).isEqualTo(amount(15_000L));
        }

        @Test
        @DisplayName("잔액 증가 후 updatedAt이 갱신된다")
        void increaseBalance_updatesUpdatedAt() {
            Wallet wallet = createWallet(0L);
            LocalDateTime updatedAt = now.plusMinutes(10);

            wallet.increaseBalance(amount(1_000L), updatedAt);

            assertThat(wallet.getUpdatedAt()).isEqualTo(updatedAt);
        }

        @Test
        @DisplayName("0 이하 금액으로 증가 요청 시 예외가 발생한다 - 0원")
        void increaseBalance_zeroAmount_throwsException() {
            Wallet wallet = createWallet(10_000L);

            assertThatThrownBy(() -> wallet.increaseBalance(amount(0L), now))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Amount must be positive.");
        }

        @Test
        @DisplayName("0 이하 금액으로 증가 요청 시 예외가 발생한다 - 음수")
        void increaseBalance_negativeAmount_throwsException() {
            Wallet wallet = createWallet(10_000L);

            assertThatThrownBy(() -> wallet.increaseBalance(amount(-1_000L), now))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Amount must be positive.");
        }

        @Test
        @DisplayName("여러 번 충전 시 잔액이 누적된다")
        void increaseBalance_multipleTimes_accumulates() {
            Wallet wallet = createWallet(0L);

            wallet.increaseBalance(amount(10_000L), now.plusMinutes(1));
            wallet.increaseBalance(amount(5_000L), now.plusMinutes(2));

            assertThat(wallet.getBalance()).isEqualTo(amount(15_000L));
        }
    }

    @Nested
    @DisplayName("Wallet.applyTransaction() 테스트")
    class ApplyTransaction {

        @Test
        @DisplayName("applyTransaction() 호출 시 잔액과 updatedAt이 업데이트된다")
        void applyTransaction_updatesBalanceAndTimestamp() {
            Wallet wallet = createWallet(10_000L);
            LocalDateTime updatedAt = now.plusMinutes(5);

            wallet.applyTransaction(amount(20_000L), updatedAt);

            assertThat(wallet.getBalance()).isEqualTo(amount(20_000L));
            assertThat(wallet.getUpdatedAt()).isEqualTo(updatedAt);
        }
    }

    @Nested
    @DisplayName("Wallet.decreaseBalance() 환불 차감 테스트")
    class DecreaseBalance {

        @Test
        @DisplayName("환불 금액만큼 잔액이 차감된다")
        void decreaseBalance_subtractsAmountFromBalance() {
            Wallet wallet = createWallet(10_000L);
            LocalDateTime updatedAt = now.plusMinutes(5);

            BigDecimal newBalance = wallet.decreaseBalance(amount(4_000L), updatedAt);

            assertThat(newBalance).isEqualTo(amount(6_000L));
            assertThat(wallet.getBalance()).isEqualTo(amount(6_000L));
            assertThat(wallet.getUpdatedAt()).isEqualTo(updatedAt);
        }

        @Test
        @DisplayName("잔액보다 큰 금액 차감 시 예외가 발생한다")
        void decreaseBalance_insufficientBalance_throwsException() {
            Wallet wallet = createWallet(3_000L);

            assertThatThrownBy(() -> wallet.decreaseBalance(amount(4_000L), now))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Balance is insufficient.");
        }
    }
}

