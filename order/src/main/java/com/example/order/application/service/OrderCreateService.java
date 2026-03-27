package com.example.order.application.service;

import com.example.order.application.port.ProductPort;
import com.example.order.application.port.ProductPort.ProductInfo;
import com.example.order.application.usecase.OrderCreateUseCase;
import com.example.order.common.exception.CustomException;
import com.example.order.common.exception.ErrorCode;
import com.example.order.domain.entity.Order;
import com.example.order.domain.enumtype.ProductOrderStatus;
import com.example.order.domain.repository.OrderRepository;
import com.example.order.infrastructure.client.dto.request.ProductRequest;
import com.example.order.presentation.dto.request.OrderCreateRequest;
import com.example.order.presentation.dto.response.OrderCreateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderCreateService implements OrderCreateUseCase {

    private final OrderRepository orderRepository;
    private final ProductPort productPort;

    /**
     * 주문 생성 처리
     * <p>
     * - 상품 ID 중복 요청 검증
     * - 상품 정보 조회 및 존재 여부 검증
     * - 상품 상태(재고, 판매 여부) 검증
     * - Order 및 OrderItem 생성 후 저장
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
                .map(item -> new ProductRequest(
                        item.productId(),
                        item.quantity()
                ))
                .toList();

        Set<UUID> productIds = productRequests.stream()
                .map(ProductRequest::productId)
                .collect(Collectors.toSet());

        if (productIds.size() != productRequests.size()) {
            throw new CustomException(ErrorCode.DUPLICATE_PRODUCT_REQUEST);
        }

        List<ProductInfo> products = productPort.checkAvailability(productRequests);

        if (products.size() != productIds.size()) {
            throw new CustomException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        Map<UUID, ProductInfo> productMap = products.stream()
                .collect(Collectors.toMap(ProductInfo::productId, p -> p));

        UUID firstProductId = productRequests.get(0).productId();
        ProductInfo firstProduct = productMap.get(firstProductId);

        Order order = Order.create(
                memberId,
                request.address(),
                request.addressDetail(),
                request.zipCode(),
                request.receiver(),
                request.receiverPhone(),
                firstProduct.name(),
                firstProduct.thumbnailKeySnapshot(),
                productIds.size());

        request.orderItemRequest().forEach(item -> {
            ProductInfo product = productMap.get(item.productId());
            ProductOrderStatus status = product.productOrderStatus();

            if (status == ProductOrderStatus.INSUFFICIENT_STOCK) {
                throw new CustomException(ErrorCode.INSUFFICIENT_STOCK);
            } else if (status == ProductOrderStatus.NOT_FOR_SALE) {
                throw new CustomException(ErrorCode.PRODUCT_NOT_ORDERABLE);
            }

            order.addItem(
                    product.productId(),
                    product.sellerId(),
                    product.name(),
                    product.price(),
                    item.quantity(),
                    product.thumbnailKeySnapshot());
        });

        return OrderCreateResponse.from(orderRepository.save(order));
    }
}
