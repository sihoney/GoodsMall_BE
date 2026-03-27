package com.example.settlement.application.service;

import com.example.settlement.application.dto.MonthlySettlementAggregateCommand;
import com.example.settlement.application.dto.MonthlySettlementAggregateResult;
import com.example.settlement.application.dto.SettlementItemCreateCommand;
import com.example.settlement.domain.entity.Settlement;
import com.example.settlement.domain.entity.SettlementItem;
import com.example.settlement.domain.repository.SettlementItemRepository;
import com.example.settlement.domain.repository.SettlementRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MonthlySettlementService {

    private static final long FEE_RATE_PERCENT = 10L;

    private final SettlementRepository settlementRepository;
    private final SettlementItemRepository settlementItemRepository;

    public MonthlySettlementService(
            SettlementRepository settlementRepository,
            SettlementItemRepository settlementItemRepository
    ) {
        this.settlementRepository = settlementRepository;
        this.settlementItemRepository = settlementItemRepository;
    }

    public SettlementItem registerSettlementItem(SettlementItemCreateCommand command) {
        validateSettlementItemCommand(command);

        SettlementItem existingItem = settlementItemRepository.findByEscrowId(command.escrowId()).orElse(null);
        if (existingItem != null) {
            return existingItem;
        }

        long feeAmount = calculateFeeAmount(command.grossAmount());
        long netAmount = command.grossAmount() - feeAmount;

        return settlementItemRepository.save(SettlementItem.create(
                UUID.randomUUID(),
                null,
                command.orderId(),
                command.escrowId(),
                command.sellerId(),
                command.grossAmount(),
                feeAmount,
                netAmount,
                command.releasedAt(),
                LocalDateTime.now()
        ));
    }

    public MonthlySettlementAggregateResult aggregateMonthlySettlements(MonthlySettlementAggregateCommand command) {
        validateAggregateCommand(command);

        List<SettlementItem> settlementItems = settlementItemRepository.findByReleasedAtBetween(
                command.releasedAtFrom(),
                command.releasedAtTo()
        );

        int createdSettlementCount = 0;
        int updatedSettlementCount = 0;

        for (SettlementItem settlementItem : settlementItems) {
            Settlement settlement = settlementRepository.findBySellerIdAndSettlementYearAndSettlementMonth(
                    settlementItem.getSellerId(),
                    command.settlementYear(),
                    command.settlementMonth()
            ).orElse(null);

            if (settlement == null) {
                settlement = Settlement.createPending(
                        UUID.randomUUID(),
                        settlementItem.getSellerId(),
                        command.settlementYear(),
                        command.settlementMonth(),
                        settlementItem.getGrossAmount(),
                        settlementItem.getFeeAmount(),
                        settlementItem.getNetAmount(),
                        LocalDateTime.now()
                );
                settlement = settlementRepository.save(settlement);
                settlementItem.assignSettlement(settlement.getSettlementId());
                settlementItemRepository.save(settlementItem);
                createdSettlementCount++;
                continue;
            }

            settlement.accumulate(
                    settlementItem.getGrossAmount(),
                    settlementItem.getFeeAmount(),
                    settlementItem.getNetAmount(),
                    LocalDateTime.now()
            );
            settlementRepository.save(settlement);
            settlementItem.assignSettlement(settlement.getSettlementId());
            settlementItemRepository.save(settlementItem);
            updatedSettlementCount++;
        }

        return new MonthlySettlementAggregateResult(
                command.settlementYear(),
                command.settlementMonth(),
                createdSettlementCount,
                updatedSettlementCount,
                settlementItems.size()
        );
    }

    private void validateSettlementItemCommand(SettlementItemCreateCommand command) {
        Objects.requireNonNull(command, "command must not be null.");
        requireUuid(command.orderId(), "orderId");
        requireUuid(command.escrowId(), "escrowId");
        requireUuid(command.sellerId(), "sellerId");
        requirePositive(command.grossAmount(), "grossAmount");
        Objects.requireNonNull(command.releasedAt(), "releasedAt must not be null.");
    }

    private void validateAggregateCommand(MonthlySettlementAggregateCommand command) {
        Objects.requireNonNull(command, "command must not be null.");
        if (command.settlementYear() <= 0) {
            throw new IllegalArgumentException("settlementYear must be positive.");
        }
        if (command.settlementMonth() < 1 || command.settlementMonth() > 12) {
            throw new IllegalArgumentException("settlementMonth must be between 1 and 12.");
        }
        Objects.requireNonNull(command.releasedAtFrom(), "releasedAtFrom must not be null.");
        Objects.requireNonNull(command.releasedAtTo(), "releasedAtTo must not be null.");
        if (!command.releasedAtFrom().isBefore(command.releasedAtTo())) {
            throw new IllegalArgumentException("releasedAtFrom must be before releasedAtTo.");
        }
    }

    private long calculateFeeAmount(long grossAmount) {
        return grossAmount * FEE_RATE_PERCENT / 100;
    }

    private void requireUuid(UUID value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null.");
        }
    }

    private void requirePositive(Long value, String fieldName) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive.");
        }
    }
}
