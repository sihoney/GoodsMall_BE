package com.example.order.infrastructure.repository;

import com.example.order.domain.entity.CourierCompany;
import com.example.order.domain.repository.CourierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CourierRepositoryImpl implements CourierRepository {

    private final CourierJpaRepository courierJpaRepository;

    @Override
    public Optional<CourierCompany> findByNameAndActiveTrue(String name) {
        return courierJpaRepository.findByNameAndActiveTrue(name);
    }

    @Override
    public Map<String, String> findNamesByCode(Collection<String> codes) {
        return courierJpaRepository.findByCodeIn(codes).stream()
                .collect(Collectors.toMap(CourierCompany::getCode, CourierCompany::getName));
    }
}
