package com.example.settlement.application.usecase;

import com.example.settlement.application.dto.PagedResult;
import com.example.settlement.application.dto.SellerSettlementDetailResult;
import com.example.settlement.application.dto.SellerSettlementListItemResult;
import com.example.settlement.domain.enumtype.SettlementStatus;
import com.example.settlement.domain.enumtype.SettlementType;
import java.util.UUID;

public interface SellerSettlementSearchUseCase {

    PagedResult<SellerSettlementListItemResult> findSettlements(
            UUID sellerId,
            SettlementType settlementType,
            SettlementStatus settlementStatus,
            Integer settlementYear,
            Integer settlementMonth,
            int page,
            int size
    );

    SellerSettlementDetailResult findSettlementDetail(UUID sellerId, UUID settlementId);
}
