package com.example.order.application.service;

import com.example.order.application.port.PaymentPort;
import com.example.order.application.port.ProductPort;
import com.example.order.application.port.ProductPort.ProductInfo;
import com.example.order.common.exception.CustomException;
import com.example.order.common.exception.ErrorCode;
import com.example.order.domain.entity.Order;
import com.example.order.domain.enumtype.PaymentStatus;
import com.example.order.domain.enumtype.ProductOrderStatus;
import com.example.order.domain.repository.OrderRepository;
import com.example.order.infrastructure.client.dto.request.ProductRequest;
import com.example.order.presentation.dto.request.OrderCreateRequest;
import com.example.order.presentation.dto.request.OrderItemCreateRequest;
import com.example.order.presentation.dto.response.OrderCreateResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderCreateServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductPort productPort;

    @Mock
    private PaymentPort paymentPort;

    @Mock
    private DeliveryCreateService deliveryCreateService;

    @InjectMocks
    private OrderCreateService orderCreateService;

    private UUID memberId;
    private UUID productId1;
    private UUID productId2;
    private UUID sellerId1;
    private UUID sellerId2;

    @BeforeEach
    void setUp() {
        memberId = UUID.randomUUID();
        productId1 = UUID.randomUUID();
        productId2 = UUID.randomUUID();
        sellerId1 = UUID.randomUUID();
        sellerId2 = UUID.randomUUID();
    }

    @Nested
    @DisplayName("주문 생성 실패")
    class CreateFail {

        @Test
        @DisplayName("주문 항목이 비어있으면 예외가 발생한다")
        void create_fail_when_order_items_empty() {
            OrderCreateRequest request = new OrderCreateRequest(
                    "서울시 강남구",
                    "101동 101호",
                    "12345",
                    "홍길동",
                    "01012345678",
                    List.of()
            );

            assertThatThrownBy(() -> orderCreateService.create(memberId, request))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.INVALID_INPUT_VALUE);

            verifyNoInteractions(productPort, paymentPort, deliveryCreateService, orderRepository);
        }

        @Test
        @DisplayName("중복 상품 요청이면 예외가 발생한다")
        void create_fail_when_duplicate_product_request() {
            OrderCreateRequest request = new OrderCreateRequest(
                    "서울시 강남구",
                    "101동 101호",
                    "12345",
                    "홍길동",
                    "01012345678",
                    List.of(
                            new OrderItemCreateRequest(productId1, 1),
                            new OrderItemCreateRequest(productId1, 2)
                    )
            );

            assertThatThrownBy(() -> orderCreateService.create(memberId, request))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.DUPLICATE_PRODUCT_REQUEST);

            verifyNoInteractions(productPort, paymentPort, deliveryCreateService, orderRepository);
        }

        @Test
        @DisplayName("조회된 상품 수가 요청 상품 수와 다르면 PRODUCT_NOT_FOUND 예외가 발생한다")
        void create_fail_when_product_not_found() {
            OrderCreateRequest request = new OrderCreateRequest(
                    "서울시 강남구",
                    "101동 101호",
                    "12345",
                    "홍길동",
                    "01012345678",
                    List.of(
                            new OrderItemCreateRequest(productId1, 1),
                            new OrderItemCreateRequest(productId2, 2)
                    )
            );

            when(productPort.checkAvailability(anyList()))
                    .thenReturn(List.of(
                            productInfo(productId1, sellerId1, ProductOrderStatus.ORDERABLE, BigDecimal.valueOf(1000))
                    ));

            assertThatThrownBy(() -> orderCreateService.create(memberId, request))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.PRODUCT_NOT_FOUND);

            verify(productPort).checkAvailability(anyList());
            verifyNoInteractions(paymentPort, deliveryCreateService);
            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("재고 부족 상품이 포함되면 INSUFFICIENT_STOCK 예외가 발생한다")
        void create_fail_when_insufficient_stock() {
            OrderCreateRequest request = new OrderCreateRequest(
                    "서울시 강남구",
                    "101동 101호",
                    "12345",
                    "홍길동",
                    "01012345678",
                    List.of(
                            new OrderItemCreateRequest(productId1, 1)
                    )
            );

            when(productPort.checkAvailability(anyList()))
                    .thenReturn(List.of(
                            productInfo(productId1, sellerId1, ProductOrderStatus.INSUFFICIENT_STOCK, BigDecimal.valueOf(1000))
                    ));

            assertThatThrownBy(() -> orderCreateService.create(memberId, request))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.INSUFFICIENT_STOCK);

            verify(productPort).checkAvailability(anyList());
            verify(orderRepository, never()).save(any(Order.class));
            verifyNoInteractions(paymentPort, deliveryCreateService);
        }

        @Test
        @DisplayName("판매 불가 상품이 포함되면 PRODUCT_NOT_ORDERABLE 예외가 발생한다")
        void create_fail_when_product_not_orderable() {
            OrderCreateRequest request = new OrderCreateRequest(
                    "서울시 강남구",
                    "101동 101호",
                    "12345",
                    "홍길동",
                    "01012345678",
                    List.of(
                            new OrderItemCreateRequest(productId1, 1)
                    )
            );

            when(productPort.checkAvailability(anyList()))
                    .thenReturn(List.of(
                            productInfo(productId1, sellerId1, ProductOrderStatus.NOT_FOR_SALE, BigDecimal.valueOf(1000))
                    ));

            assertThatThrownBy(() -> orderCreateService.create(memberId, request))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.PRODUCT_NOT_ORDERABLE);

            verify(productPort).checkAvailability(anyList());
            verify(orderRepository, never()).save(any(Order.class));
            verifyNoInteractions(paymentPort, deliveryCreateService);
        }

        @Test
        @DisplayName("결제 응답의 orderId가 다르면 PAYMENT_FAILED 예외가 발생한다")
        void create_fail_when_payment_order_id_mismatch() {
            OrderCreateRequest request = validRequest();

            when(productPort.checkAvailability(anyList()))
                    .thenReturn(validProducts());

            when(orderRepository.save(any(Order.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            when(paymentPort.requestPayment(any()))
                    .thenReturn(new PaymentPort.PaymentResult(
                            UUID.randomUUID(),   // 다른 orderId
                            BigDecimal.valueOf(4000),
                            PaymentStatus.SUCCESS,
                            null
                    ));

            assertThatThrownBy(() -> orderCreateService.create(memberId, request))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.PAYMENT_FAILED);

            verify(orderRepository).save(any(Order.class));
            verify(paymentPort).requestPayment(any());
            verifyNoInteractions(deliveryCreateService);
        }

        @Test
        @DisplayName("결제 상태가 SUCCESS가 아니면 PAYMENT_FAILED 예외가 발생한다")
        void create_fail_when_payment_status_is_not_success() {
            OrderCreateRequest request = validRequest();

            when(productPort.checkAvailability(anyList()))
                    .thenReturn(validProducts());

            when(orderRepository.save(any(Order.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            when(paymentPort.requestPayment(any()))
                    .thenAnswer(invocation -> {
                        PaymentPort.PaymentRequest paymentRequest = invocation.getArgument(0);
                        return new PaymentPort.PaymentResult(
                                paymentRequest.orderId(),
                                BigDecimal.ZERO,
                                PaymentStatus.FAILED,
                                "PAYMENT_DECLINED"
                        );
                    });

            assertThatThrownBy(() -> orderCreateService.create(memberId, request))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode")
                    .isEqualTo(ErrorCode.PAYMENT_FAILED);

            verify(productPort).checkAvailability(anyList());
            verify(orderRepository).save(any(Order.class));
            verify(paymentPort).requestPayment(any());
            verifyNoInteractions(deliveryCreateService);
        }

        @Test
        @DisplayName("정상 주문이면 주문 저장, 결제 요청, 주문 확정, 배송 생성이 수행된다")
        void create_success() {
            OrderCreateRequest request = validRequest();

            when(productPort.checkAvailability(anyList()))
                    .thenReturn(validProducts());

            when(orderRepository.save(any(Order.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            when(paymentPort.requestPayment(any()))
                    .thenAnswer(invocation -> {
                        PaymentPort.PaymentRequest paymentRequest = invocation.getArgument(0);
                        return new PaymentPort.PaymentResult(
                                paymentRequest.orderId(),
                                BigDecimal.valueOf(4000),
                                PaymentStatus.SUCCESS,
                                null
                        );
                    });

            OrderCreateResponse response = orderCreateService.create(memberId, request);

            assertThat(response).isNotNull();

            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(orderCaptor.capture());

            Order savedOrder = orderCaptor.getValue();
            assertThat(savedOrder).isNotNull();

            verify(productPort).checkAvailability(anyList());
            verify(paymentPort).requestPayment(any());
            verify(deliveryCreateService).create(savedOrder);
        }

        @Test
        @DisplayName("상품 조회 요청이 주문 요청 수량과 상품 ID 기준으로 만들어진다")
        void create_calls_product_port_with_expected_requests() {
            OrderCreateRequest request = validRequest();

            when(productPort.checkAvailability(anyList()))
                    .thenReturn(validProducts());

            when(orderRepository.save(any(Order.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            when(paymentPort.requestPayment(any()))
                    .thenAnswer(invocation -> {
                        PaymentPort.PaymentRequest paymentRequest = invocation.getArgument(0);
                        return new PaymentPort.PaymentResult(
                                paymentRequest.orderId(),
                                BigDecimal.valueOf(4000),
                                PaymentStatus.SUCCESS,
                                null
                        );
                    });

            orderCreateService.create(memberId, request);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<ProductRequest>> captor = ArgumentCaptor.forClass((Class) List.class);
            verify(productPort).checkAvailability(captor.capture());

            List<ProductRequest> actual = captor.getValue();
            assertThat(actual).hasSize(2);
            assertThat(actual)
                    .extracting(ProductRequest::productId)
                    .containsExactly(productId1, productId2);
            assertThat(actual)
                    .extracting(ProductRequest::quantity)
                    .containsExactly(1, 2);
        }

        private OrderCreateRequest validRequest() {
            return new OrderCreateRequest(
                    "서울시 강남구",
                    "101동 101호",
                    "12345",
                    "홍길동",
                    "01012345678",
                    List.of(
                            new OrderItemCreateRequest(productId1, 1),
                            new OrderItemCreateRequest(productId2, 2)
                    )
            );
        }

        private List<ProductInfo> validProducts() {
            return List.of(
                    productInfo(productId1, sellerId1, ProductOrderStatus.ORDERABLE, BigDecimal.valueOf(1000)),
                    productInfo(productId2, sellerId2, ProductOrderStatus.ORDERABLE, BigDecimal.valueOf(1500))
            );
        }

        private ProductInfo productInfo(
                UUID productId,
                UUID sellerId,
                ProductOrderStatus status,
                BigDecimal price
        ) {
            return new ProductInfo(
                    productId,
                    sellerId,
                    "상품-" + productId,
                    price,
                    "thumbnail-key",
                    status
            );
        }
    }
}