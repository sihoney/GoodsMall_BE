package com.example.payment.domain.service;

import java.time.LocalDateTime;

public interface TimeProvider {

    LocalDateTime now();
}
