package com.example.settlement.application.service;

import com.example.settlement.application.dto.PartialSettlementCreateCommand;
import com.example.settlement.application.dto.PartialSettlementCreateResult;
import com.example.settlement.application.usecase.PartialSettlementCreationUseCase;
import com.example.settlement.domain.entity.Settlement;
import com.example.settlement.domain.entity.SettlementItem;
import com.example.settlement.domain.repository.SettlementItemRepository;
import com.example.settlement.domain.repository.SettlementRepository;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 판매자 부분 정산 생성을 담당하는 서비스다.
 */
@Service
@Transactional
public class PartialSettlementCreationService implements PartialSettlementCreationUseCase {

    private final SettlementRepository settlementRepository;
    private final SettlementItemRepository settlementItemRepository;

    public PartialSettlementCreationService(
            SettlementRepository settlementRepository,
            SettlementItemRepository settlementItemRepository
    ) {
        this.settlementRepository = settlementRepository;
        this.settlementItemRepository = settlementItemRepository;
    }

    @Override
    public PartialSettlementCreateResult createPartialSettlement(PartialSettlementCreateCommand command) {
        validateCreatePartialSettlementCommand(command);

        List<UUID> uniqueSettlementItemIds = toUniqueSettlementItemIds(command.settlementItemIds());
        List<SettlementItem> settlementItems = settlementItemRepository.findAllBySettlementItemIdIn(uniqueSettlementItemIds);

        validateSettlementItemCount(uniqueSettlementItemIds, settlementItems);
        validateSettlementItemsForPartialSettlement(command.sellerId(), settlementItems);

        long totalSalesAmount = settlementItems.stream()
                .mapToLong(SettlementItem::getGrossAmount)
                .sum();
        long feeAmount = settlementItems.stream()
                .mapToLong(SettlementItem::getFeeAmount)
                .sum();
        long finalSettlementAmount = settlementItems.stream()
                .mapToLong(SettlementItem::getNetAmount)
                .sum();

        LocalDateTime requestedAt = LocalDateTime.now();
        Settlement partialSettlement = settlementRepository.save(Settlement.createPartialPending(
                UUID.randomUUID(),
                command.sellerId(),
                requestedAt.getYear(),
                requestedAt.getMonthValue(),
                totalSalesAmount,
                feeAmount,
                finalSettlementAmount,
                requestedAt
        ));

        for (SettlementItem settlementItem : settlementItems) {
            settlementItem.assignSettlement(partialSettlement.getSettlementId());
            settlementItemRepository.save(settlementItem);
        }

        return new PartialSettlementCreateResult(
                partialSettlement.getSettlementId(),
                partialSettlement.getSellerId(),
                partialSettlement.getSettlementType(),
                partialSettlement.getSettlementStatus(),
                settlementItems.size(),
                partialSettlement.getTotalSalesAmount(),
                partialSettlement.getFeeAmount(),
                partialSettlement.getFinalSettlementAmount()
        );
    }

    private void validateCreatePartialSettlementCommand(PartialSettlementCreateCommand command) {
        Objects.requireNonNull(command, "command must not be null.");
        if (command.sellerId() == null) {
            throw new IllegalArgumentException("sellerId must not be null.");
        }
        if (command.settlementItemIds() == null || command.settlementItemIds().isEmpty()) {
            throw new IllegalArgumentException("settlementItemIds must not be empty.");
        }
    }

    private List<UUID> toUniqueSettlementItemIds(List<UUID> settlementItemIds) {
        Set<UUID> uniqueSettlementItemIds = new LinkedHashSet<>();
        for (UUID settlementItemId : settlementItemIds) {
            if (settlementItemId == null) {
                throw new IllegalArgumentException("settlementItemId must not be null.");
            }
            uniqueSettlementItemIds.add(settlementItemId);
        }
        return List.copyOf(uniqueSettlementItemIds);
    }

    private void validateSettlementItemCount(List<UUID> requestedSettlementItemIds, List<SettlementItem> settlementItems) {
        if (requestedSettlementItemIds.size() != settlementItems.size()) {
            throw new IllegalArgumentException("Some settlement items do not exist.");
        }
    }

    private void validateSettlementItemsForPartialSettlement(UUID sellerId, List<SettlementItem> settlementItems) {
        for (SettlementItem settlementItem : settlementItems) {
            if (!Objects.equals(settlementItem.getSellerId(), sellerId)) {
                throw new IllegalArgumentException("settlement item sellerId does not match request sellerId.");
            }
            if (settlementItem.isAlreadyAggregated()) {
                throw new IllegalArgumentException("Already assigned settlement item is not allowed.");
            }
            if (settlementItem.getGrossAmount() <= 0L) {
                throw new IllegalArgumentException("Only positive grossAmount settlement items are allowed.");
            }
        }
    }
}
