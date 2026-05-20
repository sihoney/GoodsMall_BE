package com.example.payment.domain.service;

import java.util.UUID;

/**
 * 식별자 생성 전략을 추상화한 도메인 서비스 포트다.
 */
public interface IdentifierGenerator {

    UUID generateUuid();
}
