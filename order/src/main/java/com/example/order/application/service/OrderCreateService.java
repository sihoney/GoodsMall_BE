package com.example.order.application.service;

import com.example.order.application.port.PaymentResult;
import com.example.order.application.port.ProductPort.ProductInfo;
import com.example.order.application.processor.PaymentProcessor;
import com.example.order.application.processor.ProductProcessor;
import com.example.order.application.usecase.OrderCreateUseCase;
import com.example.order.common.exception.CustomException;
import com.example.order.common.exception.ErrorCode;
import com.example.order.domain.entity.Order;
import com.example.order.domain.repository.OrderRepository;
import com.example.order.infrastructure.client.dto.request.ProductRequest;
import com.example.order.presentation.dto.request.OrderCreateRequest;
import com.example.order.presentation.dto.response.OrderCreateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderCreateService implements OrderCreateUseCase {

    private final OrderRepository orderRepository;
    private final ProductProcessor productProcessor;
    private final PaymentProcessor paymentProcessor;
    private final DeliveryCreateService deliveryCreateService;

    /**
     * 주문 생성 처리
     * <p>
     * - 상품 ID 중복 요청 검증
     * - 상품 정보 조회 및 존재 여부 검증
     * - 상품 상태(재고, 판매 여부) 검증
     * - Order 및 OrderItem 생성 후 저장
     * - OrderCreatedEvent 발행
     */
    @Transactional
    @Override
    public OrderCreateResponse create(
            UUID memberId,
            OrderCreateRequest request) {

        if (request.orderItemRequest().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        List<ProductRequest> productRequests = request.orderItemRequest().stream()
                .map(item -> new ProductRequest(item.productId(), item.quantity()))
                .toList();

        productProcessor.validateDuplicate(productRequests);
        Map<UUID, ProductInfo> productMap = productProcessor.deductStock(productRequests);
        productProcessor.validateStatus(productMap, request);

        ProductInfo firstProduct = productMap.get(productRequests.getFirst().productId());

        Order order = Order.create(
                memberId,
                request.address(),
                request.addressDetail(),
                request.zipCode(),
                request.receiver(),
                request.receiverPhone(),
                firstProduct.name(),
                firstProduct.thumbnailKeySnapshot(),
                productMap.size());

        request.orderItemRequest().forEach(item -> {
            ProductInfo product = productMap.get(item.productId());
            order.addItem(
                    product.productId(),
                    product.sellerId(),
                    product.name(),
                    product.price(),
                    item.quantity(),
                    product.thumbnailKeySnapshot());
        });
        orderRepository.save(order);

        PaymentResult paymentResult = paymentProcessor.process(order);
        order.confirm(paymentResult.paidAmount());

        deliveryCreateService.create(order);

        return OrderCreateResponse.from(order);
    }
}
