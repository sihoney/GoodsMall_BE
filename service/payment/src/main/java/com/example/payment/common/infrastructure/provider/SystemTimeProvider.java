package com.example.payment.common.infrastructure.provider;

import com.example.payment.common.domain.service.TimeProvider;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;

@Component
/**
 * ?좏뵆由ъ??댁뀡 ?ㅽ뻾 ?쒖젏???꾩옱 ?쒓컖???쒓났?섎뒗 TimeProvider 援ы쁽泥대떎.
 */
public class SystemTimeProvider implements TimeProvider {

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }
}
