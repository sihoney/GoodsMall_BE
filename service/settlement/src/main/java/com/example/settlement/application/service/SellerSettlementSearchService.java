package com.example.settlement.application.service;

import com.example.settlement.application.dto.PagedResult;
import com.example.settlement.application.dto.SellerSettlementDetailItemResult;
import com.example.settlement.application.dto.SellerSettlementDetailResult;
import com.example.settlement.application.dto.SellerSettlementListItemResult;
import com.example.settlement.application.usecase.SellerSettlementSearchUseCase;
import com.example.settlement.common.exception.CustomException;
import com.example.settlement.common.exception.ErrorCode;
import com.example.settlement.domain.entity.Settlement;
import com.example.settlement.domain.entity.SettlementItem;
import com.example.settlement.domain.enumtype.SettlementStatus;
import com.example.settlement.domain.enumtype.SettlementType;
import com.example.settlement.domain.repository.SettlementItemRepository;
import com.example.settlement.domain.repository.SettlementRepository;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SellerSettlementSearchService implements SellerSettlementSearchUseCase {

    private static final int MAX_PAGE_SIZE = 100;

    private final SettlementRepository settlementRepository;
    private final SettlementItemRepository settlementItemRepository;

    public SellerSettlementSearchService(
            SettlementRepository settlementRepository,
            SettlementItemRepository settlementItemRepository
    ) {
        this.settlementRepository = settlementRepository;
        this.settlementItemRepository = settlementItemRepository;
    }

    @Override
    public PagedResult<SellerSettlementListItemResult> findSettlements(
            UUID sellerId,
            SettlementType settlementType,
            SettlementStatus settlementStatus,
            Integer settlementYear,
            Integer settlementMonth,
            int page,
            int size
    ) {
        validateSellerId(sellerId);
        validatePageRequest(page, size);
        validateYearMonth(settlementYear, settlementMonth);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "requestedAt"));
        Page<Settlement> settlementPage = settlementRepository.findBySellerIdWithFilters(
                sellerId,
                settlementType,
                settlementStatus,
                settlementYear,
                settlementMonth,
                pageable
        );

        List<SellerSettlementListItemResult> items = settlementPage.getContent().stream()
                .map(this::toSellerSettlementListItemResult)
                .toList();

        return new PagedResult<>(
                items,
                settlementPage.getNumber(),
                settlementPage.getSize(),
                settlementPage.getTotalElements(),
                settlementPage.getTotalPages(),
                settlementPage.hasNext()
        );
    }

    @Override
    public SellerSettlementDetailResult findSettlementDetail(UUID sellerId, UUID settlementId) {
        validateSellerId(sellerId);
        if (settlementId == null) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "settlementId는 필수입니다.");
        }

        Settlement settlement = settlementRepository.findBySettlementIdAndSellerId(settlementId, sellerId)
                .orElseThrow(() -> new CustomException(
                        ErrorCode.SETTLEMENT_NOT_FOUND,
                        "정산 정보를 찾을 수 없습니다. settlementId=" + settlementId
                ));

        List<SellerSettlementDetailItemResult> items = settlementItemRepository
                .findAllBySettlementIdOrderByReleasedAtDesc(settlementId).stream()
                .map(this::toSellerSettlementDetailItemResult)
                .toList();

        return new SellerSettlementDetailResult(
                settlement.getSettlementId(),
                settlement.getSellerId(),
                settlement.getSettlementType(),
                settlement.getSettlementYear(),
                settlement.getSettlementMonth(),
                settlement.getTotalSalesAmount(),
                settlement.getFeeAmount(),
                settlement.getFinalSettlementAmount(),
                settlement.getSettledAmount(),
                settlement.getSettlementStatus(),
                settlement.getSettledAt(),
                settlement.getLastFailureReason(),
                settlement.getRequestedAt(),
                settlement.getUpdatedAt(),
                items
        );
    }

    private SellerSettlementListItemResult toSellerSettlementListItemResult(Settlement settlement) {
        return new SellerSettlementListItemResult(
                settlement.getSettlementId(),
                settlement.getSellerId(),
                settlement.getSettlementType(),
                settlement.getSettlementYear(),
                settlement.getSettlementMonth(),
                settlement.getTotalSalesAmount(),
                settlement.getFeeAmount(),
                settlement.getFinalSettlementAmount(),
                settlement.getSettledAmount(),
                settlement.getSettlementStatus(),
                settlement.getSettledAt(),
                settlement.getLastFailureReason(),
                settlement.getRequestedAt(),
                settlement.getUpdatedAt()
        );
    }

    private SellerSettlementDetailItemResult toSellerSettlementDetailItemResult(SettlementItem settlementItem) {
        return new SellerSettlementDetailItemResult(
                settlementItem.getSettlementItemId(),
                settlementItem.getOrderId(),
                settlementItem.getEscrowId(),
                settlementItem.getGrossAmount(),
                settlementItem.getFeeAmount(),
                settlementItem.getNetAmount(),
                settlementItem.getReleasedAt()
        );
    }

    private void validateSellerId(UUID sellerId) {
        Objects.requireNonNull(sellerId, "sellerId must not be null.");
    }

    private void validatePageRequest(int page, int size) {
        if (page < 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "page는 0 이상이어야 합니다.");
        }
        if (size <= 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "size는 1 이상이어야 합니다.");
        }
        if (size > MAX_PAGE_SIZE) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "size는 100을 초과할 수 없습니다.");
        }
    }

    private void validateYearMonth(Integer settlementYear, Integer settlementMonth) {
        if (settlementYear != null && settlementYear <= 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "settlementYear는 1 이상이어야 합니다.");
        }
        if (settlementMonth != null && (settlementMonth < 1 || settlementMonth > 12)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "settlementMonth는 1부터 12 사이여야 합니다.");
        }
    }
}
