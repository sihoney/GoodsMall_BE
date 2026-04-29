package com.example.order.application.service;

import com.example.order.common.exception.CustomException;
import com.example.order.common.exception.ErrorCode;
import com.example.order.domain.entity.Order;
import com.example.order.domain.enumtype.OrderType;
import com.example.order.domain.repository.DeliveryRepository;
import com.example.order.domain.repository.OrderRepository;
import com.example.order.presentation.dto.response.OrderDetailResponse;
import com.example.order.presentation.dto.response.OrderSummaryResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderSearchServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private DeliveryRepository deliveryRepository;

    @InjectMocks
    private OrderSearchService orderSearchService;

    @Test
    @DisplayName("회원 ID로 주문 목록을 조회하면 OrderSummaryResponse 페이지로 반환한다")
    void findByMemberId_success() {
        UUID memberId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        Order order1 = mock(Order.class);
        Order order2 = mock(Order.class);

        Page<Order> orderPage = new PageImpl<>(List.of(order1, order2), pageable, 2);

        when(orderRepository.findByBuyerIdAndOrderType(memberId, null, null, null, null, pageable)).thenReturn(orderPage);

        Page<OrderSummaryResponse> result = orderSearchService.findByMemberId(memberId, null, null, null, null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);

        verify(orderRepository).findByBuyerIdAndOrderType(memberId, null, null, null, null, pageable);
    }

    @Test
    @DisplayName("회원 주문 목록이 없으면 빈 페이지를 반환한다")
    void findByMemberId_empty() {
        UUID memberId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        when(orderRepository.findByBuyerIdAndOrderType(memberId, null, null, null, null, pageable))
                .thenReturn(Page.empty(pageable));

        Page<OrderSummaryResponse> result = orderSearchService.findByMemberId(memberId, null, null, null, null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();

        verify(orderRepository).findByBuyerIdAndOrderType(memberId, null, null, null, null, pageable);
    }

    @Test
    @DisplayName("orderType 필터로 조회하면 해당 타입의 주문만 반환한다")
    void findByMemberId_withOrderTypeFilter() {
        UUID memberId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        Order order = mock(Order.class);
        Page<Order> orderPage = new PageImpl<>(List.of(order), pageable, 1);

        when(orderRepository.findByBuyerIdAndOrderType(memberId, OrderType.AUCTION, null, null, null, pageable)).thenReturn(orderPage);

        Page<OrderSummaryResponse> result = orderSearchService.findByMemberId(memberId, OrderType.AUCTION, null, null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(orderRepository).findByBuyerIdAndOrderType(memberId, OrderType.AUCTION, null, null, null, pageable);
    }

    @Test
    @DisplayName("keyword로 검색하면 해당 상품명을 포함한 주문만 반환한다")
    void findByMemberId_withKeyword() {
        UUID memberId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        Order order = mock(Order.class);
        Page<Order> orderPage = new PageImpl<>(List.of(order), pageable, 1);

        when(orderRepository.findByBuyerIdAndOrderType(memberId, null, "닭갈비", null, null, pageable)).thenReturn(orderPage);

        Page<OrderSummaryResponse> result = orderSearchService.findByMemberId(memberId, null, "닭갈비", null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(orderRepository).findByBuyerIdAndOrderType(memberId, null, "닭갈비", null, null, pageable);
    }

    @Nested
    @DisplayName("주문 상세 조회")
    class GetOrderDetail {

        @Test
        @DisplayName("주문이 존재하면 상세 정보를 반환한다")
        void getOrderDetail_success() {
            UUID orderId = UUID.randomUUID();
            UUID memberId = UUID.randomUUID();

            Order order = mock(Order.class);

            when(orderRepository.findByOrderIdAndBuyerId(orderId, memberId))
                    .thenReturn(Optional.of(order));

            // order.getItems()가 내부적으로 사용되므로 null만 아니면 됨
            when(order.getItems()).thenReturn(List.of());

            OrderDetailResponse result = orderSearchService.getOrderDetail(orderId, memberId);

            assertThat(result).isNotNull();
            verify(orderRepository).findByOrderIdAndBuyerId(orderId, memberId);
            verify(order).getItems();
        }

        @Test
        @DisplayName("주문이 없으면 ORDER_NOT_FOUND 예외가 발생한다")
        void getOrderDetail_fail_when_order_not_found() {
            UUID orderId = UUID.randomUUID();
            UUID memberId = UUID.randomUUID();

            when(orderRepository.findByOrderIdAndBuyerId(orderId, memberId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> orderSearchService.getOrderDetail(orderId, memberId))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.ORDER_NOT_FOUND);

            verify(orderRepository).findByOrderIdAndBuyerId(orderId, memberId);
        }
    }
}