package com.example.order.application.service;

import com.example.order.common.exception.CustomException;
import com.example.order.common.exception.ErrorCode;
import com.example.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderNumberGenerator {

    private static final int MAX_ATTEMPTS = 3;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");

    private final OrderRepository orderRepository;

    public String generateUnique() {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            String orderNumber = generate();
            if (!orderRepository.existsByOrderNumber(orderNumber)) {
                return orderNumber;
            }
            log.warn("주문번호 중복, 재시도. attempt={}, orderNumber={}", attempt, orderNumber);
        }
        throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
    }

    private String generate() {
        String datePart = LocalDate.now().format(DATE_FORMATTER);
        String randomPart = String.format("%08d", ThreadLocalRandom.current().nextInt(0, 100_000_000));
        return datePart + randomPart;
    }
}
