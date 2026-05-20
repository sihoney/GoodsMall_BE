package com.example.payment.application.service;

import com.example.payment.application.dto.OrderPaymentCommand;
import com.example.payment.application.dto.OrderPaymentLineCommand;
import com.example.payment.application.dto.OrderPaymentResult;
import com.example.payment.application.usecase.OrderPaymentUseCase;
import com.example.payment.common.exception.InvalidOrderPaymentRequestException;
import com.example.payment.common.exception.WalletNotFoundException;
import com.example.payment.domain.entity.Escrow;
import com.example.payment.domain.entity.EscrowTransaction;
import com.example.payment.domain.entity.OrderPayment;
import com.example.payment.domain.entity.OrderPaymentAllocation;
import com.example.payment.domain.entity.Wallet;
import com.example.payment.domain.entity.WalletTransaction;
import com.example.payment.domain.enumtype.EscrowReferenceType;
import com.example.payment.domain.enumtype.OrderPaymentMethod;
import com.example.payment.domain.repository.EscrowTransactionRepository;
import com.example.payment.domain.repository.OrderPaymentAllocationRepository;
import com.example.payment.domain.repository.OrderPaymentRepository;
import com.example.payment.domain.repository.EscrowRepository;
import com.example.payment.domain.repository.WalletRepository;
import com.example.payment.domain.repository.WalletTransactionRepository;
import com.example.payment.domain.service.IdentifierGenerator;
import com.example.payment.domain.service.TimeProvider;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
/**
 * 주문 결제 유스케이스를 담당한다.
 * 구매자 wallet 차감과 orderItem 단위 escrow 생성을 하나의 흐름으로 처리하고, 중복 주문 결제 요청은 기존 결과를 재사용한다.
 */
public class OrderPaymentService implements OrderPaymentUseCase {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final EscrowRepository escrowRepository;
    private final EscrowTransactionRepository escrowTransactionRepository;
    private final OrderPaymentRepository orderPaymentRepository;
    private final OrderPaymentAllocationRepository orderPaymentAllocationRepository;
    private final IdentifierGenerator identifierGenerator;
    private final TimeProvider timeProvider;

    public OrderPaymentService(
            WalletRepository walletRepository,
            WalletTransactionRepository walletTransactionRepository,
            EscrowRepository escrowRepository,
            EscrowTransactionRepository escrowTransactionRepository,
            OrderPaymentRepository orderPaymentRepository,
            OrderPaymentAllocationRepository orderPaymentAllocationRepository,
            IdentifierGenerator identifierGenerator,
            TimeProvider timeProvider
    ) {
        this.walletRepository = walletRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.escrowRepository = escrowRepository;
        this.escrowTransactionRepository = escrowTransactionRepository;
        this.orderPaymentRepository = orderPaymentRepository;
        this.orderPaymentAllocationRepository = orderPaymentAllocationRepository;
        this.identifierGenerator = identifierGenerator;
        this.timeProvider = timeProvider;
    }

    @Override
    /**
     * 주문 결제 요청을 검증한 뒤 구매자 wallet을 한 번 차감하고 orderItem 단위 escrow를 생성한다.
     * 이미 같은 orderId의 escrow가 있으면 추가 차감 없이 기존 결과를 반환해 멱등하게 처리한다.
     */
    public OrderPaymentResult payOrder(OrderPaymentCommand command) {
        validateCommand(command);

        // 다중 seller 주문에서는 orderId 아래 escrow가 여러 건 존재할 수 있다.
        List<Escrow> existingEscrows = escrowRepository.findAllByOrderId(command.orderId());
        // 주문번호기준 escrow가 이미 존재한다면 같은 주문에 대한 중복 요청이므로 기존 결과를 재구성해서 반환한다.
        if (!existingEscrows.isEmpty()) {
            return existingResult(command, existingEscrows);
        }

        // 구매자 지갑 조회
        Wallet buyerWallet = walletRepository.findByMemberId(command.buyerMemberId())
                .orElseThrow(WalletNotFoundException::new);

        LocalDateTime now = timeProvider.now();
        // 지갑 금액과 결제 금액 차이는 도메인에 있음
        java.math.BigDecimal balanceAfter = buyerWallet.decreaseBalance(command.orderAmount(), now);

        // wallet 변동 사항 기록
        WalletTransaction purchaseTransaction = WalletTransaction.purchase(
                identifierGenerator.generateUuid(),
                buyerWallet.getWalletId(),
                command.orderAmount(),
                balanceAfter,
                command.orderId(),
                now
        );

        // buyer 결제는 한 번만 수행하고, orderItem 단위 정산 원천을 escrow로 저장한다.
        List<Escrow> escrows = command.paymentLines().stream()
                .map(paymentLine -> Escrow.createHeld(
                        identifierGenerator.generateUuid(),
                        command.orderId(),
                        paymentLine.orderItemId(),
                        EscrowReferenceType.ORDER_ITEM,
                        command.buyerMemberId(),
                        paymentLine.sellerMemberId(),
                        paymentLine.lineAmount(),
                        now
                ))
                .toList();

        walletRepository.save(buyerWallet);
        walletTransactionRepository.save(purchaseTransaction);
        escrowRepository.saveAll(escrows);
        recordHoldEscrowTransactions(escrows, now);
        saveOrderPaymentRecords(
                command,
                purchaseTransaction.getTransactionId(),
                now
        );

        return new OrderPaymentResult(
                command.orderId(),
                buyerWallet.getWalletId(),
                escrows.stream().map(Escrow::getEscrowId).toList(),
                command.orderAmount(),
                buyerWallet.getBalance()
        );
    }

    /**
     * 같은 주문이 이미 처리된 경우 기존 escrow 목록을 기반으로 응답을 재구성한다.
     */
    private OrderPaymentResult existingResult(OrderPaymentCommand command, List<Escrow> existingEscrows) {
        Wallet buyerWallet = walletRepository.findByMemberId(command.buyerMemberId())
                .orElseThrow(WalletNotFoundException::new);

        return new OrderPaymentResult(
                command.orderId(),
                buyerWallet.getWalletId(),
                existingEscrows.stream().map(Escrow::getEscrowId).toList(),
                command.orderAmount(),
                buyerWallet.getBalance()
        );
    }

    /**
     * 주문 결제 계약의 필수 입력과 orderItem별 금액 합계를 검증한다.
     */
    private void validateCommand(OrderPaymentCommand command) {
        if (command.orderId() == null) {
            throw new InvalidOrderPaymentRequestException("주문 ID는 필수입니다.");
        }
        if (command.buyerMemberId() == null) {
            throw new InvalidOrderPaymentRequestException("구매자 회원 ID는 필수입니다.");
        }
        if (command.orderAmount() == null || command.orderAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new InvalidOrderPaymentRequestException("주문 금액은 0보다 커야 합니다.");
        }
        if (command.paymentLines() == null || command.paymentLines().isEmpty()) {
            throw new InvalidOrderPaymentRequestException("결제 라인은 비어 있을 수 없습니다.");
        }

        java.math.BigDecimal totalLineAmount = java.math.BigDecimal.ZERO;
        for (OrderPaymentLineCommand paymentLine : command.paymentLines()) {
            if (paymentLine == null) {
                throw new InvalidOrderPaymentRequestException("결제 라인에 비어 있는 항목이 포함될 수 없습니다.");
            }
            if (paymentLine.orderItemId() == null) {
                throw new InvalidOrderPaymentRequestException("주문 항목 ID는 필수입니다.");
            }
            if (paymentLine.sellerMemberId() == null) {
                throw new InvalidOrderPaymentRequestException("판매자 회원 ID는 필수입니다.");
            }
            if (paymentLine.lineAmount() == null || paymentLine.lineAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new InvalidOrderPaymentRequestException("주문 항목 금액은 0보다 커야 합니다.");
            }
            totalLineAmount = totalLineAmount.add(paymentLine.lineAmount());
        }

        // order 이벤트 총액과 orderItem별 집계 총액이 다르면 escrow 분해 기준이 깨진다.
        if (totalLineAmount.compareTo(command.orderAmount()) != 0) {
            throw new InvalidOrderPaymentRequestException("주문 항목 금액 합계와 주문 금액이 일치해야 합니다.");
        }
    }

    private void saveOrderPaymentRecords(
            OrderPaymentCommand command,
            java.util.UUID walletTransactionId,
            LocalDateTime paidAt
    ) {
        OrderPayment orderPayment = OrderPayment.createSucceeded(
                identifierGenerator.generateUuid(),
                command.orderId(),
                command.buyerMemberId(),
                command.orderAmount(),
                OrderPaymentMethod.WALLET,
                paidAt
        );
        OrderPayment savedOrderPayment = orderPaymentRepository.save(orderPayment);

        OrderPaymentAllocation walletAllocation = OrderPaymentAllocation.walletAllocation(
                identifierGenerator.generateUuid(),
                savedOrderPayment.getOrderPaymentId(),
                command.orderAmount(),
                walletTransactionId,
                paidAt
        );
        orderPaymentAllocationRepository.saveAll(java.util.List.of(walletAllocation));
    }

    private void recordHoldEscrowTransactions(List<Escrow> escrows, LocalDateTime occurredAt) {
        List<EscrowTransaction> transactions = escrows.stream()
                .map(escrow -> EscrowTransaction.hold(
                        identifierGenerator.generateUuid(),
                        escrow.getEscrowId(),
                        escrow.getOrderId(),
                        escrow.isOrderItemReference() ? escrow.getReferenceId() : null,
                        escrow.getSellerMemberId(),
                        escrow.getBuyerMemberId(),
                        escrow.getAmount(),
                        java.math.BigDecimal.ZERO,
                        escrow.getAmount(),
                        escrow.getReferenceId(),
                        escrow.getReferenceType().name(),
                        "에스크로 보관",
                        occurredAt,
                        occurredAt
                ))
                .toList();
        escrowTransactionRepository.saveAll(transactions);
    }
}
