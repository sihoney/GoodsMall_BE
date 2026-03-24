package com.example.payment.infrastructure.repository;

import com.example.payment.domain.entity.Charge;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChargeJpaRepository extends JpaRepository<Charge, UUID> {
}
