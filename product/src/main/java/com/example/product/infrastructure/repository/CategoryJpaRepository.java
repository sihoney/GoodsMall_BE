package com.example.product.infrastructure.repository;

import com.example.product.domain.entity.Category;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryJpaRepository extends JpaRepository<Category, UUID> {
}
