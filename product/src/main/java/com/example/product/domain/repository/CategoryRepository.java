package com.example.product.domain.repository;

import com.example.product.domain.entity.Category;
import java.util.List;
import java.util.UUID;

public interface CategoryRepository {

    Category save(Category category);

    List<Category> findAll();

    Category findById(UUID categoryId);

    List<Category> findByDepth(Integer depth);

    List<Category> findByParentCategory(Category category);

    boolean hasChildren(UUID categoryId);
}
