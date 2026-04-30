package com.example.product.infrastructure.messaging.kafka;

import com.example.product.common.exception.ProductNotFoundException;
import com.example.product.domain.entity.Product;
import com.example.product.domain.repository.ProductRepository;
import com.example.product.infrastructure.messaging.kafka.contract.OrderCanceledPayload;
import com.example.product.infrastructure.messaging.kafka.contract.OrderCanceledPayload.CanceledOrderLine;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.todaylunch.common.event.contract.EventEnvelope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCanceledEventConsumer {

    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    @KafkaListener(topics = KafkaTopics.ORDER_CANCELED, groupId = "product-group")
    public void handle(String message) {
        try {
            EventEnvelope<OrderCanceledPayload> envelope = objectMapper.readValue(
                    message, new TypeReference<>() {}
            );
            OrderCanceledPayload payload = envelope.payload();

            for (CanceledOrderLine line : payload.canceledLines()) {
                restoreStock(line);
            }

            log.info("주문 취소 재고 복원 완료: orderId={}, lines={}",
                    payload.orderId(), payload.canceledLines().size());

        } catch (Exception e) {
            log.error("주문 취소 재고 복원 실패: message={}", message, e);
        }
    }

    private void restoreStock(CanceledOrderLine line) {
        Product product = productRepository.findByIdWithLock(line.productId())
                .orElseThrow(ProductNotFoundException::new);
        product.increaseStock(line.quantity());
        productRepository.save(product);
        log.debug("재고 복원: productId={}, quantity={}", line.productId(), line.quantity());
    }
}
