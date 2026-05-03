package com.example.product.infrastructure.messaging.kafka;

import com.example.product.common.exception.ProductNotFoundException;
import com.example.product.domain.entity.Product;
import com.example.product.domain.repository.ProductRepository;
import com.example.product.infrastructure.messaging.kafka.contract.PaymentFailedPayload;
import com.example.product.infrastructure.messaging.kafka.contract.PaymentFailedPayload.FailedOrderLine;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.todaylunch.common.event.contract.EventEnvelope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentFailedEventConsumer {

    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    @KafkaListener(topics = KafkaTopics.ORDER_PAYMENT_FAILED, groupId = "product-group")
    public void handle(String message) {
        try {
            EventEnvelope<PaymentFailedPayload> envelope = objectMapper.readValue(
                    message, new TypeReference<>() {}
            );
            PaymentFailedPayload payload = envelope.payload();

            for (FailedOrderLine line : payload.failedLines()) {
                restoreStock(line);
            }

            log.info("결제 실패 재고 복원 완료: orderId={}, lines={}",
                    payload.orderId(), payload.failedLines().size());

        } catch (Exception e) {
            log.error("결제 실패 재고 복원 실패: message={}", message, e);
        }
    }

    private void restoreStock(FailedOrderLine line) {
        Product product = productRepository.findByIdWithLock(line.productId())
                .orElseThrow(ProductNotFoundException::new);
        product.increaseStock(line.quantity());
        productRepository.save(product);
        log.debug("재고 복원: productId={}, quantity={}", line.productId(), line.quantity());
    }
}
