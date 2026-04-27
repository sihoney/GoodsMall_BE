package com.example.order.infrastructure.repository;

import com.example.order.domain.entity.CourierCompany;
import com.example.order.domain.repository.CourierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CourierRepositoryImpl implements CourierRepository {

    private final CourierJpaRepository courierJpaRepository;

    @Override
    public Optional<CourierCompany> findByNameAndActiveTrue(String name) {
        return courierJpaRepository.findByNameAndActiveTrue(name);
    }

}
