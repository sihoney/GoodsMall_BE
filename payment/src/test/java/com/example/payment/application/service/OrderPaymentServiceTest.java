package com.example.payment.application.service;

import com.example.payment.application.dto.OrderPaymentCommand;
import com.example.payment.application.dto.OrderPaymentResult;
import com.example.payment.domain.entity.Escrow;
import com.example.payment.domain.entity.Wallet;
import com.example.payment.domain.exception.InvalidOrderPaymentRequestException;
import com.example.payment.domain.exception.WalletNotFoundException;
import com.example.payment.domain.repository.EscrowRepository;
import com.example.payment.domain.repository.WalletRepository;
import com.example.payment.domain.repository.WalletTransactionRepository;
import com.example.payment.domain.service.IdentifierGenerator;
import com.example.payment.domain.service.TimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderPaymentService 애플리케이션 서비스 테스트")
class OrderPaymentServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletTransactionRepository walletTransactionRepository;

    @Mock
    private EscrowRepository escrowRepository;

    @Mock
    private IdentifierGenerator identifierGenerator;

    @Mock
    private TimeProvider timeProvider;

    @InjectMocks
    private OrderPaymentService orderPaymentService;

    private UUID orderId;
    private UUID buyerMemberId;
    private UUID sellerMemberId;
    private UUID buyerWalletId;
    private UUID escrowId;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        buyerMemberId = UUID.randomUUID();
        sellerMemberId = UUID.randomUUID();
        buyerWalletId = UUID.randomUUID();
        escrowId = UUID.randomUUID();
        now = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
    }

    @Nested
    @DisplayName("payOrder() 주문 결제 테스트")
    class PayOrder {

        @Test
        @DisplayName("정상 결제 시 구매자 잔액이 차감되고 escrow가 생성된다")
        void payOrder_success_decreasesBuyerBalanceAndCreatesEscrow() {
            OrderPaymentCommand command = new OrderPaymentCommand(
                    orderId,
                    buyerMemberId,
                    sellerMemberId,
                    12_000L,
                    10_000L,
                    null
            );
            Wallet buyerWallet = Wallet.create(buyerWalletId, buyerMemberId, 20_000L, now, now);

            given(escrowRepository.findByOrderId(orderId)).willReturn(Optional.empty());
            given(walletRepository.findByMemberId(buyerMemberId)).willReturn(Optional.of(buyerWallet));
            given(timeProvider.now()).willReturn(now);
            given(identifierGenerator.generateUuid()).willReturn(UUID.randomUUID(), escrowId);
            given(walletRepository.save(any(Wallet.class))).willAnswer(inv -> inv.getArgument(0));
            given(walletTransactionRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
            given(escrowRepository.save(any(Escrow.class))).willAnswer(inv -> inv.getArgument(0));

            OrderPaymentResult result = orderPaymentService.payOrder(command);

            assertThat(result.orderId()).isEqualTo(orderId);
            assertThat(result.buyerWalletId()).isEqualTo(buyerWalletId);
            assertThat(result.escrowId()).isEqualTo(escrowId);
            assertThat(result.paidAmount()).isEqualTo(12_000L);
            assertThat(result.buyerWalletBalance()).isEqualTo(8_000L);
            verify(walletRepository).save(any(Wallet.class));
            verify(walletTransactionRepository).save(any());
            verify(escrowRepository).save(any(Escrow.class));
        }

        @Test
        @DisplayName("같은 orderId로 이미 escrow가 있으면 중복 결제를 막는다")
        void payOrder_duplicateOrder_throwsException() {
            OrderPaymentCommand command = new OrderPaymentCommand(
                    orderId,
                    buyerMemberId,
                    sellerMemberId,
                    12_000L,
                    10_000L,
                    null
            );
            Escrow existingEscrow = Escrow.createHeld(escrowId, orderId, sellerMemberId, 10_000L, null, now);

            given(escrowRepository.findByOrderId(orderId)).willReturn(Optional.of(existingEscrow));

            assertThatThrownBy(() -> orderPaymentService.payOrder(command))
                    .isInstanceOf(InvalidOrderPaymentRequestException.class)
                    .hasMessageContaining("already been completed");

            verify(walletRepository, never()).save(any());
            verify(walletTransactionRepository, never()).save(any());
        }

        @Test
        @DisplayName("구매자 지갑이 없으면 WalletNotFoundException이 발생한다")
        void payOrder_walletNotFound_throwsException() {
            OrderPaymentCommand command = new OrderPaymentCommand(
                    orderId,
                    buyerMemberId,
                    sellerMemberId,
                    12_000L,
                    10_000L,
                    null
            );

            given(escrowRepository.findByOrderId(orderId)).willReturn(Optional.empty());
            given(walletRepository.findByMemberId(buyerMemberId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> orderPaymentService.payOrder(command))
                    .isInstanceOf(WalletNotFoundException.class);
        }

        @Test
        @DisplayName("판매자 정산 대상 금액이 주문 금액보다 크면 예외가 발생한다")
        void payOrder_invalidSellerReceivable_throwsException() {
            OrderPaymentCommand command = new OrderPaymentCommand(
                    orderId,
                    buyerMemberId,
                    sellerMemberId,
                    10_000L,
                    12_000L,
                    null
            );

            assertThatThrownBy(() -> orderPaymentService.payOrder(command))
                    .isInstanceOf(InvalidOrderPaymentRequestException.class)
                    .hasMessageContaining("cannot exceed orderAmount");
        }

        @Test
        @DisplayName("지갑 잔액이 부족하면 구매자 차감 없이 실패한다")
        void payOrder_insufficientBalance_throwsException() {
            OrderPaymentCommand command = new OrderPaymentCommand(
                    orderId,
                    buyerMemberId,
                    sellerMemberId,
                    12_000L,
                    10_000L,
                    null
            );
            Wallet buyerWallet = Wallet.create(buyerWalletId, buyerMemberId, 5_000L, now, now);

            given(escrowRepository.findByOrderId(orderId)).willReturn(Optional.empty());
            given(walletRepository.findByMemberId(buyerMemberId)).willReturn(Optional.of(buyerWallet));
            given(timeProvider.now()).willReturn(now);

            assertThatThrownBy(() -> orderPaymentService.payOrder(command))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Balance is insufficient.");

            verify(walletRepository, never()).save(any());
            verify(walletTransactionRepository, never()).save(any());
            verify(escrowRepository, never()).save(any());
        }
    }
}
