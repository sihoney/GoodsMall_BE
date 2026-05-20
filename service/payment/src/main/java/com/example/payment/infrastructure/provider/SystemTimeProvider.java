package com.example.payment.infrastructure.provider;

import com.example.payment.domain.service.TimeProvider;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
/**
 * 애플리케이션 실행 시점의 현재 시각을 제공하는 TimeProvider 구현체다.
 */
public class SystemTimeProvider implements TimeProvider {

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }
}
