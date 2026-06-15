package com.example.payment.payment.application.service;

import com.example.payment.payment.application.dto.OrderPaymentCommand;
import com.example.payment.payment.application.dto.OrderPaymentLineCommand;
import com.example.payment.payment.application.dto.OrderPaymentResult;
import com.example.payment.payment.application.usecase.OrderPaymentUseCase;
import com.example.payment.common.exception.InvalidOrderPaymentRequestException;
import com.example.payment.common.exception.WalletNotFoundException;
import com.example.payment.escrow.domain.entity.Escrow;
import com.example.payment.escrow.domain.entity.EscrowTransaction;
import com.example.payment.payment.domain.entity.OrderPayment;
import com.example.payment.payment.domain.entity.OrderPaymentAllocation;
import com.example.payment.wallet.domain.entity.Wallet;
import com.example.payment.wallet.domain.entity.WalletTransaction;
import com.example.payment.escrow.domain.enumtype.EscrowReferenceType;
import com.example.payment.payment.domain.enumtype.OrderPaymentMethod;
import com.example.payment.escrow.domain.repository.EscrowTransactionRepository;
import com.example.payment.payment.domain.repository.OrderPaymentAllocationRepository;
import com.example.payment.payment.domain.repository.OrderPaymentRepository;
import com.example.payment.escrow.domain.repository.EscrowRepository;
import com.example.payment.wallet.domain.repository.WalletRepository;
import com.example.payment.wallet.domain.repository.WalletTransactionRepository;
import com.example.payment.common.domain.service.IdentifierGenerator;
import com.example.payment.common.domain.service.TimeProvider;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
/**
 * 二쇰Ц 寃곗젣 ?좎뒪耳?댁뒪瑜??대떦?쒕떎.
 * 援щℓ??wallet 李④컧怨?orderItem ?⑥쐞 escrow ?앹꽦???섎굹???먮쫫?쇰줈 泥섎━?섍퀬, 以묐났 二쇰Ц 寃곗젣 ?붿껌? 湲곗〈 寃곌낵瑜??ъ궗?⑺븳??
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
     * 二쇰Ц 寃곗젣 ?붿껌??寃利앺븳 ??援щℓ??wallet????踰?李④컧?섍퀬 orderItem ?⑥쐞 escrow瑜??앹꽦?쒕떎.
     * ?대? 媛숈? orderId??escrow媛 ?덉쑝硫?異붽? 李④컧 ?놁씠 湲곗〈 寃곌낵瑜?諛섑솚??硫깅벑?섍쾶 泥섎━?쒕떎.
     */
    public OrderPaymentResult payOrder(OrderPaymentCommand command) {
        validateCommand(command);

        // ?ㅼ쨷 seller 二쇰Ц?먯꽌??orderId ?꾨옒 escrow媛 ?щ윭 嫄?議댁옱?????덈떎.
        List<Escrow> existingEscrows = escrowRepository.findAllByOrderId(command.orderId());
        // 二쇰Ц踰덊샇湲곗? escrow媛 ?대? 議댁옱?쒕떎硫?媛숈? 二쇰Ц?????以묐났 ?붿껌?대?濡?湲곗〈 寃곌낵瑜??ш뎄?깊빐??諛섑솚?쒕떎.
        if (!existingEscrows.isEmpty()) {
            return existingResult(command, existingEscrows);
        }

        // 援щℓ??吏媛?議고쉶
        Wallet buyerWallet = walletRepository.findByMemberId(command.buyerMemberId())
                .orElseThrow(WalletNotFoundException::new);

        LocalDateTime now = timeProvider.now();
        // 吏媛?湲덉븸怨?寃곗젣 湲덉븸 李⑥씠???꾨찓?몄뿉 ?덉쓬
        java.math.BigDecimal balanceAfter = buyerWallet.decreaseBalance(command.orderAmount(), now);

        // wallet 蹂???ы빆 湲곕줉
        WalletTransaction purchaseTransaction = WalletTransaction.purchase(
                identifierGenerator.generateUuid(),
                buyerWallet.getWalletId(),
                command.orderAmount(),
                balanceAfter,
                command.orderId(),
                now
        );

        // buyer 寃곗젣????踰덈쭔 ?섑뻾?섍퀬, orderItem ?⑥쐞 ?뺤궛 ?먯쿇??escrow濡???ν븳??
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
     * 媛숈? 二쇰Ц???대? 泥섎━??寃쎌슦 湲곗〈 escrow 紐⑸줉??湲곕컲?쇰줈 ?묐떟???ш뎄?깊븳??
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
     * 二쇰Ц 寃곗젣 怨꾩빟???꾩닔 ?낅젰怨?orderItem蹂?湲덉븸 ?⑷퀎瑜?寃利앺븳??
     */
    private void validateCommand(OrderPaymentCommand command) {
        if (command.orderId() == null) {
            throw new InvalidOrderPaymentRequestException("二쇰Ц ID???꾩닔?낅땲??");
        }
        if (command.buyerMemberId() == null) {
            throw new InvalidOrderPaymentRequestException("援щℓ???뚯썝 ID???꾩닔?낅땲??");
        }
        if (command.orderAmount() == null || command.orderAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new InvalidOrderPaymentRequestException("二쇰Ц 湲덉븸? 0蹂대떎 而ㅼ빞 ?⑸땲??");
        }
        if (command.paymentLines() == null || command.paymentLines().isEmpty()) {
            throw new InvalidOrderPaymentRequestException("寃곗젣 ?쇱씤? 鍮꾩뼱 ?덉쓣 ???놁뒿?덈떎.");
        }

        java.math.BigDecimal totalLineAmount = java.math.BigDecimal.ZERO;
        for (OrderPaymentLineCommand paymentLine : command.paymentLines()) {
            if (paymentLine == null) {
                throw new InvalidOrderPaymentRequestException("寃곗젣 ?쇱씤??鍮꾩뼱 ?덈뒗 ??ぉ???ы븿?????놁뒿?덈떎.");
            }
            if (paymentLine.orderItemId() == null) {
                throw new InvalidOrderPaymentRequestException("二쇰Ц ??ぉ ID???꾩닔?낅땲??");
            }
            if (paymentLine.sellerMemberId() == null) {
                throw new InvalidOrderPaymentRequestException("?먮ℓ???뚯썝 ID???꾩닔?낅땲??");
            }
            if (paymentLine.lineAmount() == null || paymentLine.lineAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
                throw new InvalidOrderPaymentRequestException("二쇰Ц ??ぉ 湲덉븸? 0蹂대떎 而ㅼ빞 ?⑸땲??");
            }
            totalLineAmount = totalLineAmount.add(paymentLine.lineAmount());
        }

        // order ?대깽??珥앹븸怨?orderItem蹂?吏묎퀎 珥앹븸???ㅻⅤ硫?escrow 遺꾪빐 湲곗???源⑥쭊??
        if (totalLineAmount.compareTo(command.orderAmount()) != 0) {
            throw new InvalidOrderPaymentRequestException("二쇰Ц ??ぉ 湲덉븸 ?⑷퀎? 二쇰Ц 湲덉븸???쇱튂?댁빞 ?⑸땲??");
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
                        "?먯뒪?щ줈 蹂닿?",
                        occurredAt,
                        occurredAt
                ))
                .toList();
        escrowTransactionRepository.saveAll(transactions);
    }
}
