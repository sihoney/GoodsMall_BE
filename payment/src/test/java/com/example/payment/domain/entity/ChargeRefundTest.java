package com.example.payment.domain.entity;

import com.example.payment.domain.enumtype.ChargeRefundStatus;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ChargeRefund 도메인 테스트")
class ChargeRefundTest {

    @Test
    @DisplayName("환불 성공 레코드를 생성할 수 있다")
    void refunded_createsRefundRecord() {
        UUID refundId = UUID.randomUUID();
        UUID chargeId = UUID.randomUUID();
        LocalDateTime requestedAt = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
        LocalDateTime refundedAt = requestedAt.plusMinutes(5);

        ChargeRefund chargeRefund = ChargeRefund.refunded(
                refundId,
                chargeId,
                10_000L,
                "user request",
                requestedAt,
                refundedAt
        );

        assertThat(chargeRefund.getChargeRefundId()).isEqualTo(refundId);
        assertThat(chargeRefund.getChargeId()).isEqualTo(chargeId);
        assertThat(chargeRefund.getRefundStatus()).isEqualTo(ChargeRefundStatus.REFUNDED);
        assertThat(chargeRefund.getRefundedAt()).isEqualTo(refundedAt);
    }

    @Test
    @DisplayName("환불 실패 레코드를 생성할 수 있다")
    void failed_createsFailedRefundRecord() {
        UUID refundId = UUID.randomUUID();
        UUID chargeId = UUID.randomUUID();
        LocalDateTime requestedAt = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
        LocalDateTime failedAt = requestedAt.plusMinutes(1);

        ChargeRefund chargeRefund = ChargeRefund.failed(
                refundId,
                chargeId,
                10_000L,
                "user request",
                requestedAt,
                failedAt,
                "cancel rejected"
        );

        assertThat(chargeRefund.getRefundStatus()).isEqualTo(ChargeRefundStatus.FAILED);
        assertThat(chargeRefund.getFailedAt()).isEqualTo(failedAt);
        assertThat(chargeRefund.getFailureReason()).isEqualTo("cancel rejected");
    }
}
