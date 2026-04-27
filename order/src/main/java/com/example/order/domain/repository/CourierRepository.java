package com.example.order.domain.repository;

import com.example.order.domain.entity.CourierCompany;

import java.util.Optional;

public interface CourierRepository {
    Optional<CourierCompany> findByNameAndActiveTrue(String name);
}
