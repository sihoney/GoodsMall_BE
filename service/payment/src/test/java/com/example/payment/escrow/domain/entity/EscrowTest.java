package com.example.payment.escrow.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.payment.escrow.domain.enumtype.EscrowStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("payment test")
class EscrowTest {

    private final UUID escrowId = UUID.randomUUID();
    private final UUID orderId = UUID.randomUUID();
    private final UUID buyerMemberId = UUID.randomUUID();
    private final UUID sellerMemberId = UUID.randomUUID();
    private final LocalDateTime createdAt = LocalDateTime.of(2026, 4, 1, 10, 0);

    private BigDecimal amount(long value) {
        return BigDecimal.valueOf(value);
    }

    @Nested
    @DisplayName("payment test")
    class CreateHeld {

        @Test
        @DisplayName("payment test")
        void createHeld_success() {
            Escrow escrow = Escrow.createHeld(
                    escrowId,
                    orderId,
                    buyerMemberId,
                    sellerMemberId,
                    amount(12_000L),
                    createdAt
            );

            assertThat(escrow.getEscrowStatus()).isEqualTo(EscrowStatus.HELD);
            assertThat(escrow.getAmount()).isEqualTo(amount(12_000L));
            assertThat(escrow.getOriginalAmount()).isEqualTo(amount(12_000L));
            assertThat(escrow.getRefundedAmount()).isZero();
        }

        @Test
        @DisplayName("payment test")
        void createHeld_nonPositiveAmount_throwsException() {
            assertThatThrownBy(() -> Escrow.createHeld(
                    escrowId,
                    orderId,
                    buyerMemberId,
                    sellerMemberId,
                    amount(0L),
                    createdAt
            )).isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("payment test")
    class Release {

        @Test
        @DisplayName("payment test")
        void release_success() {
            Escrow escrow = Escrow.createHeld(
                    escrowId,
                    orderId,
                    buyerMemberId,
                    sellerMemberId,
                    amount(12_000L),
                    createdAt
            );
            LocalDateTime now = createdAt.plusDays(1);

            escrow.release(now, now);

            assertThat(escrow.getEscrowStatus()).isEqualTo(EscrowStatus.RELEASED);
            assertThat(escrow.getReleasedAt()).isEqualTo(now);
        }
    }

    @Nested
    @DisplayName("payment test")
    class ApplyRefundAmount {

        @Test
        @DisplayName("payment test")
        void applyRefundAmount_partial() {
            Escrow escrow = Escrow.createHeld(
                    escrowId,
                    orderId,
                    buyerMemberId,
                    sellerMemberId,
                    amount(12_000L),
                    createdAt
            );
            LocalDateTime now = createdAt.plusDays(1);

            escrow.applyRefundAmount(amount(5_000L), now, now);

            assertThat(escrow.getEscrowStatus()).isEqualTo(EscrowStatus.HELD);
            assertThat(escrow.getAmount()).isEqualTo(amount(7_000L));
            assertThat(escrow.getRefundedAmount()).isEqualTo(amount(5_000L));
        }

        @Test
        @DisplayName("payment test")
        void applyRefundAmount_full() {
            Escrow escrow = Escrow.createHeld(
                    escrowId,
                    orderId,
                    buyerMemberId,
                    sellerMemberId,
                    amount(12_000L),
                    createdAt
            );
            LocalDateTime now = createdAt.plusDays(1);

            escrow.applyRefundAmount(amount(12_000L), now, now);

            assertThat(escrow.getEscrowStatus()).isEqualTo(EscrowStatus.REFUNDED);
            assertThat(escrow.getAmount()).isZero();
            assertThat(escrow.getRefundedAmount()).isEqualTo(amount(12_000L));
            assertThat(escrow.getRefundedAt()).isEqualTo(now);
        }
    }
}
