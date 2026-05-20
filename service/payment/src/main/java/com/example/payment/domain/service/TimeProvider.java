package com.example.payment.domain.service;

import java.time.LocalDateTime;

/**
 * 현재 시각 조회를 추상화한 도메인 서비스 포트다.
 */
public interface TimeProvider {

    LocalDateTime now();
}
