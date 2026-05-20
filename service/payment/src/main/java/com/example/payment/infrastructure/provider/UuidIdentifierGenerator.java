package com.example.payment.infrastructure.provider;

import com.example.payment.domain.service.IdentifierGenerator;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
/**
 * UUID 기반 식별자를 생성하는 IdentifierGenerator 구현체다.
 */
public class UuidIdentifierGenerator implements IdentifierGenerator {

    @Override
    public UUID generateUuid() {
        return UUID.randomUUID();
    }
}
