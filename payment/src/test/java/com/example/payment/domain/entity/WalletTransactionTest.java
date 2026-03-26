package com.example.payment.domain.entity;

import com.example.payment.domain.enumtype.WalletTransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("WalletTransaction 도메인 테스트")
class WalletTransactionTest {

    private UUID transactionId;
    private UUID walletId;
    private UUID chargeId;
    private UUID orderId;
    private LocalDateTime createdAt;

    @BeforeEach
    void setUp() {
        transactionId = UUID.randomUUID();
        walletId = UUID.randomUUID();
        chargeId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        createdAt = LocalDateTime.of(2024, 1, 1, 12, 5, 0);
    }

    @Nested
    @DisplayName("WalletTransaction.charge() 충전 트랜잭션 생성 테스트")
    class ChargeFactory {

        @Test
        @DisplayName("charge() 생성 시 transactionType이 CHARGE로 설정된다")
        void charge_transactionTypeIsCharge() {
            WalletTransaction tx = WalletTransaction.charge(
                    transactionId, walletId, 10_000L, 20_000L, chargeId, createdAt
            );

            assertThat(tx.getTransactionType()).isEqualTo(WalletTransactionType.CHARGE);
        }

        @Test
        @DisplayName("charge() 생성 시 모든 필드가 올바르게 저장된다")
        void charge_allFieldsAreSetCorrectly() {
            WalletTransaction tx = WalletTransaction.charge(
                    transactionId, walletId, 10_000L, 20_000L, chargeId, createdAt
            );

            assertThat(tx.getTransactionId()).isEqualTo(transactionId);
            assertThat(tx.getWalletId()).isEqualTo(walletId);
            assertThat(tx.getAmount()).isEqualTo(10_000L);
            assertThat(tx.getBalanceAfter()).isEqualTo(20_000L);
            assertThat(tx.getReferenceId()).isEqualTo(chargeId);
            assertThat(tx.getReferenceType()).isEqualTo("CHARGE");
            assertThat(tx.getCreatedAt()).isEqualTo(createdAt);
        }

        @Test
        @DisplayName("charge() 생성 시 description이 'wallet charge'로 설정된다")
        void charge_descriptionIsWalletCharge() {
            WalletTransaction tx = WalletTransaction.charge(
                    transactionId, walletId, 10_000L, 20_000L, chargeId, createdAt
            );

            assertThat(tx.getDescription()).isEqualTo("wallet charge");
        }

        @Test
        @DisplayName("0원으로 charge() 생성 시 예외가 발생한다")
        void charge_zeroAmount_throwsException() {
            assertThatThrownBy(() ->
                    WalletTransaction.charge(transactionId, walletId, 0L, 10_000L, chargeId, createdAt)
            )
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Charge amount must be positive.");
        }

        @Test
        @DisplayName("음수 금액으로 charge() 생성 시 예외가 발생한다")
        void charge_negativeAmount_throwsException() {
            assertThatThrownBy(() ->
                    WalletTransaction.charge(transactionId, walletId, -5_000L, 10_000L, chargeId, createdAt)
            )
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Charge amount must be positive.");
        }
    }

    @Nested
    @DisplayName("WalletTransaction.create() 일반 생성 테스트")
    class CreateFactory {

        @Test
        @DisplayName("create() 로 직접 생성 시 지정한 필드가 저장된다")
        void create_allFieldsAreStoredCorrectly() {
            WalletTransaction tx = WalletTransaction.create(
                    transactionId,
                    walletId,
                    3_000L,
                    13_000L,
                    WalletTransactionType.PURCHASE,
                    chargeId,
                    "PURCHASE",
                    "purchase item",
                    createdAt
            );

            assertThat(tx.getTransactionId()).isEqualTo(transactionId);
            assertThat(tx.getAmount()).isEqualTo(3_000L);
            assertThat(tx.getBalanceAfter()).isEqualTo(13_000L);
            assertThat(tx.getTransactionType()).isEqualTo(WalletTransactionType.PURCHASE);
            assertThat(tx.getDescription()).isEqualTo("purchase item");
        }
    }

    @Nested
    @DisplayName("WalletTransaction.refund() 환불 트랜잭션 생성 테스트")
    class RefundFactory {

        @Test
        @DisplayName("refund() 생성 시 transactionType이 REFUND로 설정된다")
        void refund_transactionTypeIsRefund() {
            WalletTransaction tx = WalletTransaction.refund(
                    transactionId, walletId, 10_000L, 5_000L, chargeId, createdAt
            );

            assertThat(tx.getTransactionType()).isEqualTo(WalletTransactionType.REFUND);
            assertThat(tx.getAmount()).isEqualTo(-10_000L);
            assertThat(tx.getReferenceId()).isEqualTo(chargeId);
            assertThat(tx.getDescription()).isEqualTo("charge refund");
        }
    }

    @Nested
    @DisplayName("WalletTransaction.purchase() 주문 결제 트랜잭션 생성 테스트")
    class PurchaseFactory {

        @Test
        @DisplayName("purchase() 생성 시 transactionType이 PURCHASE로 설정된다")
        void purchase_transactionTypeIsPurchase() {
            WalletTransaction tx = WalletTransaction.purchase(
                    transactionId, walletId, 12_000L, 8_000L, orderId, createdAt
            );

            assertThat(tx.getTransactionType()).isEqualTo(WalletTransactionType.PURCHASE);
            assertThat(tx.getAmount()).isEqualTo(-12_000L);
            assertThat(tx.getReferenceId()).isEqualTo(orderId);
            assertThat(tx.getReferenceType()).isEqualTo("ORDER");
            assertThat(tx.getDescription()).isEqualTo("order purchase");
        }

        @Test
        @DisplayName("0원으로 purchase() 생성 시 예외가 발생한다")
        void purchase_zeroAmount_throwsException() {
            assertThatThrownBy(() ->
                    WalletTransaction.purchase(transactionId, walletId, 0L, 10_000L, orderId, createdAt)
            )
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Purchase amount must be positive.");
        }
    }

    @Nested
    @DisplayName("WalletTransaction.saleIncome() 판매자 정산 트랜잭션 생성 테스트")
    class SaleIncomeFactory {

        @Test
        @DisplayName("saleIncome() 생성 시 transactionType이 SALE_INCOME으로 설정된다")
        void saleIncome_transactionTypeIsSaleIncome() {
            WalletTransaction tx = WalletTransaction.saleIncome(
                    transactionId, walletId, 9_000L, 29_000L, orderId, createdAt
            );

            assertThat(tx.getTransactionType()).isEqualTo(WalletTransactionType.SALE_INCOME);
            assertThat(tx.getAmount()).isEqualTo(9_000L);
            assertThat(tx.getReferenceId()).isEqualTo(orderId);
            assertThat(tx.getReferenceType()).isEqualTo("ORDER");
            assertThat(tx.getDescription()).isEqualTo("sale income release");
        }

        @Test
        @DisplayName("음수 금액으로 saleIncome() 생성 시 예외가 발생한다")
        void saleIncome_negativeAmount_throwsException() {
            assertThatThrownBy(() ->
                    WalletTransaction.saleIncome(transactionId, walletId, -1L, 10_000L, orderId, createdAt)
            )
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Sale income amount must be positive.");
        }
    }
}

