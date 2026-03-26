package com.example.product.domain.repository;

import com.example.product.domain.entity.Category;
import java.util.UUID;

public interface CategoryRepository {
    Category findById(UUID categoryId);
}
