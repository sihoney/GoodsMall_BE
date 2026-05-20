package com.example.settlement.presentation.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.settlement.application.dto.PagedResult;
import com.example.settlement.application.dto.SellerSettlementDetailResult;
import com.example.settlement.application.dto.SellerSettlementListItemResult;
import com.example.settlement.application.usecase.SellerSettlementSearchUseCase;
import com.example.settlement.domain.enumtype.SettlementStatus;
import com.example.settlement.domain.enumtype.SettlementType;
import com.example.settlement.presentation.dto.response.ApiResponse;
import com.example.settlement.presentation.dto.response.PagedResponse;
import com.example.settlement.presentation.dto.response.SellerSettlementDetailResponse;
import com.example.settlement.presentation.dto.response.SellerSettlementListItemResponse;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import com.todaylunch.common.security.auth.enumtype.MemberRole;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
@DisplayName("SellerSettlementController 테스트")
class SellerSettlementControllerTest {

    private static final AuthenticatedMember SELLER =
            new AuthenticatedMember(UUID.randomUUID(), MemberRole.SELLER, UUID.randomUUID());
    private static final AuthenticatedMember USER =
            new AuthenticatedMember(UUID.randomUUID(), MemberRole.USER, UUID.randomUUID());

    @Mock
    private SellerSettlementSearchUseCase sellerSettlementSearchUseCase;

    @InjectMocks
    private SellerSettlementController sellerSettlementController;

    @Test
    @DisplayName("판매자 정산 목록 조회 요청이 유효하면 200 응답을 반환한다")
    void findSettlementsReturnsOk() {
        when(sellerSettlementSearchUseCase.findSettlements(
                SELLER.memberId(),
                SettlementType.MONTHLY,
                SettlementStatus.COMPLETED,
                2026,
                3,
                0,
                20
        )).thenReturn(new PagedResult<>(
                List.of(new SellerSettlementListItemResult(
                        UUID.randomUUID(),
                        SELLER.memberId(),
                        SettlementType.MONTHLY,
                        2026,
                        3,
                        BigDecimal.valueOf(120000L),
                        BigDecimal.valueOf(12000L),
                        BigDecimal.valueOf(108000L),
                        BigDecimal.valueOf(108000L),
                        SettlementStatus.COMPLETED,
                        LocalDateTime.now(),
                        null,
                        LocalDateTime.now(),
                        LocalDateTime.now()
                )),
                0,
                20,
                1,
                1,
                false
        ));

        ResponseEntity<ApiResponse<PagedResponse<SellerSettlementListItemResponse>>> response =
                sellerSettlementController.findSettlements(
                        SELLER,
                        SettlementType.MONTHLY,
                        SettlementStatus.COMPLETED,
                        2026,
                        3,
                        0,
                        20
                );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        assertThat(response.getBody().data()).isNotNull();
        assertThat(response.getBody().data().items()).hasSize(1);
    }

    @Test
    @DisplayName("판매자 정산 상세 조회 요청이 유효하면 200 응답을 반환한다")
    void findSettlementDetailReturnsOk() {
        UUID settlementId = UUID.randomUUID();
        when(sellerSettlementSearchUseCase.findSettlementDetail(SELLER.memberId(), settlementId))
                .thenReturn(new SellerSettlementDetailResult(
                        settlementId,
                        SELLER.memberId(),
                        SettlementType.MONTHLY,
                        2026,
                        3,
                        BigDecimal.valueOf(120000L),
                        BigDecimal.valueOf(12000L),
                        BigDecimal.valueOf(108000L),
                        BigDecimal.valueOf(108000L),
                        SettlementStatus.COMPLETED,
                        LocalDateTime.now(),
                        null,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        List.of()
                ));

        ResponseEntity<ApiResponse<SellerSettlementDetailResponse>> response =
                sellerSettlementController.findSettlementDetail(SELLER, settlementId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().success()).isTrue();
        verify(sellerSettlementSearchUseCase).findSettlementDetail(SELLER.memberId(), settlementId);
    }

    @Test
    @DisplayName("판매자 권한이 아니면 403 예외를 던진다")
    void findSettlementsThrowsWhenNotSeller() {
        assertThatThrownBy(() -> sellerSettlementController.findSettlements(
                USER,
                null,
                null,
                null,
                null,
                0,
                20
        )).isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403 FORBIDDEN");
    }
}
