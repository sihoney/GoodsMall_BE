package com.example.payment.infrastructure.provider;

import com.example.payment.domain.service.IdentifierGenerator;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class UuidIdentifierGenerator implements IdentifierGenerator {

    @Override
    public UUID generateUuid() {
        return UUID.randomUUID();
    }
}
