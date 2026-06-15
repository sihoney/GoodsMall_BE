package com.example.order.infrastructure.repository;

import com.example.order.domain.entity.CourierCompany;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CourierJpaRepository extends JpaRepository<CourierCompany, String> {

    Optional<CourierCompany> findByNameAndActiveTrue(String name);

    List<CourierCompany> findByCodeIn(Collection<String> codes);
}
