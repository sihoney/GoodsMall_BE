package com.example.order.domain.repository;

import com.example.order.domain.entity.CourierCompany;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface CourierRepository {
    Optional<CourierCompany> findByNameAndActiveTrue(String name);
    Map<String, String> findNamesByCode(Collection<String> codes);
}
