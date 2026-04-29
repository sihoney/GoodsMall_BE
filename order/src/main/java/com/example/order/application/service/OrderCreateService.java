package com.example.order.application.service;

import com.example.order.application.port.dto.request.ProductStockDeductRequest;
import com.example.order.application.port.dto.response.ProductInfo;
import com.example.order.application.processor.PaymentProcessor;
import com.example.order.application.processor.ProductProcessor;
import com.example.order.application.usecase.OrderCreateUseCase;
import com.example.order.common.exception.CustomException;
import com.example.order.common.exception.ErrorCode;
import com.example.order.domain.entity.Order;
import com.example.order.domain.entity.OutboxEvent;
import com.example.order.domain.repository.OrderRepository;
import com.example.order.domain.repository.OutboxRepository;
import com.example.order.infrastructure.kafka.KafkaTopics;
import com.example.order.infrastructure.kafka.OutboxEventSaver;
import com.example.order.infrastructure.kafka.event.OrderConfirmedEvent;
import com.example.order.infrastructure.kafka.event.PaymentFailedEvent;
import com.example.order.presentation.dto.request.OrderCreateRequest;
import com.example.order.presentation.dto.request.OrderItemCreateRequest;
import com.example.order.presentation.dto.response.OrderCreateResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderCreateService implements OrderCreateUseCase {

    private final OrderRepository orderRepository;
    private final ProductProcessor productProcessor;
    private final PaymentProcessor paymentProcessor;
    private final DeliveryCreateService deliveryCreateService;
    private final OutboxRepository outboxRepository;
    private final OutboxEventSaver outboxEventSaver;
    private final ObjectMapper objectMapper;
    private final OrderNumberGenerator orderNumberGenerator;

    @Transactional
    @Override
    public OrderCreateResponse createByDeposit(UUID memberId, OrderCreateRequest request) {
        Order order = createOrder(memberId, request);

        try {
            paymentProcessor.process(order);
        } catch (CustomException e) {
            log.warn("결제 실패. orderId={}", order.getOrderId());
            savePaymentFailedOutbox(order);
            throw e;
        }

        order.confirm();
        deliveryCreateService.create(order);
        saveOrderConfirmedOutbox(order);

        return OrderCreateResponse.from(order);
    }

    private void saveOrderConfirmedOutbox(Order order) {
        try {
            String payload = objectMapper.writeValueAsString(OrderConfirmedEvent.envelopeOf(order));
            outboxRepository.save(OutboxEvent.create(KafkaTopics.ORDER_CONFIRMED, order.getOrderId().toString(), payload));
        } catch (Exception e) {
            log.error("OrderConfirmedEvent Outbox 저장 실패. orderId={}", order.getOrderId(), e);
        }
    }

    private void savePaymentFailedOutbox(Order order) {
        try {
            String payload = objectMapper.writeValueAsString(PaymentFailedEvent.envelopeOf(order));
            outboxEventSaver.save(OutboxEvent.create(KafkaTopics.PAYMENT_FAILED, order.getOrderId().toString(), payload));
        } catch (Exception e) {
            log.error("PaymentFailedEvent Outbox 저장 실패. orderId={}", order.getOrderId(), e);
        }
    }

    @Transactional
    @Override
    public OrderCreateResponse createByPg(UUID memberId, OrderCreateRequest request) {
        Order order = createOrder(memberId, request);
        return OrderCreateResponse.from(order);
    }

    private Order createOrder(UUID memberId, OrderCreateRequest request) {
        validateRequest(request);

        List<ProductStockDeductRequest> productRequests = toProductStockDeductRequests(request);
        Map<UUID, ProductInfo> productMap = loadProducts(productRequests, request);

        Order order = buildOrder(memberId, request, productRequests, productMap);
        addOrderItems(order, request.orderItemRequest(), productMap);

        orderRepository.save(order);

        return order;
    }

    private void validateRequest(OrderCreateRequest request) {
        if (request.orderItemRequest().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private List<ProductStockDeductRequest> toProductStockDeductRequests(OrderCreateRequest request) {
        return request.orderItemRequest().stream()
                .map(item -> new ProductStockDeductRequest(item.productId(), item.quantity()))
                .toList();
    }

    private Map<UUID, ProductInfo> loadProducts(
            List<ProductStockDeductRequest> productRequests,
            OrderCreateRequest request
    ) {
        productProcessor.validateDuplicate(productRequests);

        Map<UUID, ProductInfo> productMap = productProcessor.deductStock(productRequests);
        productProcessor.validateStatus(productMap, request);

        return productMap;
    }

    private Order buildOrder(
            UUID memberId,
            OrderCreateRequest request,
            List<ProductStockDeductRequest> productRequests,
            Map<UUID, ProductInfo> productMap
    ) {
        ProductInfo representativeProduct = getRepresentativeProduct(productRequests, productMap);
        String orderNumber = orderNumberGenerator.generateUnique();

        return Order.create(
                orderNumber,
                memberId,
                request.address(),
                request.addressDetail(),
                request.zipCode(),
                request.receiver(),
                request.receiverPhone(),
                representativeProduct.name(),
                representativeProduct.thumbnailKeySnapshot(),
                productMap.size()
        );
    }

    private ProductInfo getRepresentativeProduct(
            List<ProductStockDeductRequest> productRequests,
            Map<UUID, ProductInfo> productMap
    ) {
        UUID firstProductId = productRequests.get(0).productId();
        ProductInfo representativeProduct = productMap.get(firstProductId);

        if (representativeProduct == null) {
            throw new CustomException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        return representativeProduct;
    }

    private void addOrderItems(
            Order order,
            List<OrderItemCreateRequest> orderItemRequests,
            Map<UUID, ProductInfo> productMap
    ) {
        for (OrderItemCreateRequest item : orderItemRequests) {
            ProductInfo product = productMap.get(item.productId());

            order.addItem(
                    product.productId(),
                    product.sellerId(),
                    product.name(),
                    product.price(),
                    item.quantity(),
                    product.thumbnailKeySnapshot()
            );
        }
    }
}
