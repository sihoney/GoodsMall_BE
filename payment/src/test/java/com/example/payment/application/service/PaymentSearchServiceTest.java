package com.example.payment.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.example.payment.application.dto.ChargeDetailResult;
import com.example.payment.application.dto.ChargeListItemResult;
import com.example.payment.application.dto.ChargeRefundSummaryResult;
import com.example.payment.application.dto.PagedResult;
import com.example.payment.application.dto.PendingSellerIncomeItemResult;
import com.example.payment.application.dto.WalletSummaryResult;
import com.example.payment.application.dto.WalletTransactionItemResult;
import com.example.payment.common.exception.ChargeNotFoundException;
import com.example.payment.common.exception.WalletNotFoundException;
import com.example.payment.domain.entity.Charge;
import com.example.payment.domain.entity.ChargeRefund;
import com.example.payment.domain.entity.Escrow;
import com.example.payment.domain.entity.Wallet;
import com.example.payment.domain.entity.WalletTransaction;
import com.example.payment.domain.enumtype.ChargeRefundStatus;
import com.example.payment.domain.enumtype.ChargeStatus;
import com.example.payment.domain.enumtype.EscrowStatus;
import com.example.payment.domain.enumtype.PgProvider;
import com.example.payment.domain.enumtype.WalletTransactionType;
import com.example.payment.domain.repository.ChargeRefundRepository;
import com.example.payment.domain.repository.ChargeRepository;
import com.example.payment.domain.repository.EscrowRepository;
import com.example.payment.domain.repository.WalletRepository;
import com.example.payment.domain.repository.WalletTransactionRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentSearchService 테스트")
class PaymentSearchServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private ChargeRepository chargeRepository;

    @Mock
    private ChargeRefundRepository chargeRefundRepository;

    @Mock
    private WalletTransactionRepository walletTransactionRepository;

    @Mock
    private EscrowRepository escrowRepository;

    @InjectMocks
    private PaymentSearchService paymentSearchService;

    private UUID memberId;
    private UUID walletId;
    private UUID chargeId;
    private UUID refundId;
    private UUID transactionId;
    private UUID escrowId;
    private UUID orderId;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        memberId = UUID.randomUUID();
        walletId = UUID.randomUUID();
        chargeId = UUID.randomUUID();
        refundId = UUID.randomUUID();
        transactionId = UUID.randomUUID();
        escrowId = UUID.randomUUID();
        orderId = UUID.randomUUID();
        now = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
    }

    @Nested
    @DisplayName("findWalletSummary() 테스트")
    class FindWalletSummary {

        @Test
        @DisplayName("회원 지갑을 조회해 요약 정보를 반환한다")
        void findWalletSummary_success_returnsWalletSummary() {
            Wallet wallet = Wallet.create(walletId, memberId, 30_000L, now, now);
            given(walletRepository.findByMemberId(memberId)).willReturn(Optional.of(wallet));

            WalletSummaryResult result = paymentSearchService.findWalletSummary(memberId);

            assertThat(result.walletId()).isEqualTo(walletId);
            assertThat(result.memberId()).isEqualTo(memberId);
            assertThat(result.balance()).isEqualTo(30_000L);
        }

        @Test
        @DisplayName("지갑이 없으면 WalletNotFoundException이 발생한다")
        void findWalletSummary_walletNotFound_throwsException() {
            given(walletRepository.findByMemberId(memberId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> paymentSearchService.findWalletSummary(memberId))
                    .isInstanceOf(WalletNotFoundException.class);
        }
    }

    @Test
    @DisplayName("충전 목록은 최신순 페이지 결과를 반환한다")
    void findAllCharges_success_returnsPagedCharges() {
        Charge charge = Charge.create(chargeId, memberId, walletId, 10_000L, PgProvider.TOSS, "CHARGE-1", now);
        charge.approve(10_000L, "payment-key", "92", now.plusMinutes(1));
        given(chargeRepository.findByMemberId(any(), any())).willReturn(
                new PageImpl<>(List.of(charge), PageRequest.of(0, 20), 1)
        );

        PagedResult<ChargeListItemResult> result = paymentSearchService.findAllCharges(memberId, 0, 20);

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().chargeId()).isEqualTo(chargeId);
        assertThat(result.items().getFirst().chargeStatus()).isEqualTo(ChargeStatus.CONFIRM_SUCCESS);
        assertThat(result.items().getFirst().tossBankCode()).isEqualTo("92");
    }

    @Test
    @DisplayName("충전 상세는 본인 charge만 조회하고 최신 환불 이력을 포함한다")
    void findChargeDetail_success_returnsChargeDetail() {
        Charge charge = Charge.create(chargeId, memberId, walletId, 10_000L, PgProvider.TOSS, "CHARGE-1", now);
        charge.approve(10_000L, "payment-key", "92", now.plusMinutes(1));
        ChargeRefund refund = ChargeRefund.refunded(
                refundId,
                chargeId,
                10_000L,
                "단순 변심",
                now.plusDays(1),
                now.plusDays(1).plusMinutes(1)
        );
        given(chargeRepository.findByChargeIdAndMemberId(chargeId, memberId)).willReturn(Optional.of(charge));
        given(chargeRefundRepository.findTopByChargeIdOrderByRequestedAtDesc(chargeId)).willReturn(Optional.of(refund));

        ChargeDetailResult result = paymentSearchService.findChargeDetail(memberId, chargeId);

        assertThat(result.chargeId()).isEqualTo(chargeId);
        assertThat(result.tossBankCode()).isEqualTo("92");
        assertThat(result.hasRefundHistory()).isTrue();
        assertThat(result.latestRefund()).isNotNull();
        assertThat(result.latestRefund().refundStatus()).isEqualTo(ChargeRefundStatus.REFUNDED);
    }

    @Test
    @DisplayName("본인 charge가 아니면 ChargeNotFoundException이 발생한다")
    void findChargeDetail_notOwned_throwsException() {
        given(chargeRepository.findByChargeIdAndMemberId(chargeId, memberId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> paymentSearchService.findChargeDetail(memberId, chargeId))
                .isInstanceOf(ChargeNotFoundException.class);
    }

    @Test
    @DisplayName("환불 목록은 charge 소유 기준으로 조회된다")
    void findAllRefunds_success_returnsPagedRefunds() {
        ChargeRefund refund = ChargeRefund.failed(
                refundId,
                chargeId,
                10_000L,
                "중복 결제",
                now,
                now.plusMinutes(1),
                "실패"
        );
        given(chargeRefundRepository.findByMemberId(any(), any())).willReturn(
                new PageImpl<>(List.of(refund), PageRequest.of(0, 20), 1)
        );

        PagedResult<ChargeRefundSummaryResult> result = paymentSearchService.findAllRefunds(memberId, 0, 20);

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().chargeRefundId()).isEqualTo(refundId);
    }

    @Test
    @DisplayName("거래 내역은 회원 지갑 기준으로 조회된다")
    void findAllTransactions_success_returnsWalletTransactions() {
        Wallet wallet = Wallet.create(walletId, memberId, 20_000L, now, now);
        WalletTransaction transaction = WalletTransaction.create(
                transactionId,
                walletId,
                10_000L,
                20_000L,
                WalletTransactionType.CHARGE,
                chargeId,
                "CHARGE",
                "wallet charge",
                now
        );
        given(walletRepository.findByMemberId(memberId)).willReturn(Optional.of(wallet));
        given(walletTransactionRepository.findByWalletId(any(), any())).willReturn(
                new PageImpl<>(List.of(transaction), PageRequest.of(0, 20), 1)
        );

        PagedResult<WalletTransactionItemResult> result = paymentSearchService.findAllTransactions(memberId, 0, 20);

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().transactionId()).isEqualTo(transactionId);
        assertThat(result.items().getFirst().transactionType()).isEqualTo(WalletTransactionType.CHARGE);
    }

    @Test
    @DisplayName("판매자 미입금 건은 HELD escrow 목록을 반환한다")
    void findAllPendingSellerIncomes_success_returnsHeldEscrows() {
        Escrow escrow = Escrow.create(
                escrowId,
                orderId,
                UUID.randomUUID(),
                memberId,
                8_000L,
                EscrowStatus.HELD,
                null,
                null,
                now.plusDays(7),
                now,
                now
        );
        given(escrowRepository.findPendingBySellerMemberId(any(), any())).willReturn(
                new PageImpl<>(List.of(escrow), PageRequest.of(0, 20), 1)
        );

        PagedResult<PendingSellerIncomeItemResult> result = paymentSearchService.findAllPendingSellerIncomes(
                memberId,
                0,
                20
        );

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().getFirst().escrowStatus()).isEqualTo(EscrowStatus.HELD);
        assertThat(result.items().getFirst().orderId()).isEqualTo(orderId);
    }

    @Test
    @DisplayName("페이지 크기가 100을 넘으면 예외가 발생한다")
    void findAllCharges_tooLargePageSize_throwsException() {
        assertThatThrownBy(() -> paymentSearchService.findAllCharges(memberId, 0, 101))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must not exceed 100");
    }
}
