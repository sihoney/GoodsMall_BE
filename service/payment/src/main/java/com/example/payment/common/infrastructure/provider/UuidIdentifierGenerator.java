package com.example.payment.common.infrastructure.provider;

import com.example.payment.common.domain.service.IdentifierGenerator;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
/**
 * UUID 湲곕컲 ?앸퀎?먮? ?앹꽦?섎뒗 IdentifierGenerator 援ы쁽泥대떎.
 */
public class UuidIdentifierGenerator implements IdentifierGenerator {

    @Override
    public UUID generateUuid() {
        return UUID.randomUUID();
    }
}
