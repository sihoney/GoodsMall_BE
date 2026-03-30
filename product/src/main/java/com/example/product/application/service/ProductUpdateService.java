package com.example.product.application.service;

import com.example.product.application.usecase.ProductUpdateUseCase;
import com.example.product.common.exception.ProductNotFoundException;
import com.example.product.domain.entity.Category;
import com.example.product.domain.entity.Product;
import com.example.product.domain.enumtype.ProductStatus;
import com.example.product.domain.repository.CategoryRepository;
import com.example.product.domain.repository.ProductRepository;
import com.example.product.presentation.dto.request.ProductUpdateRequest;
import com.example.product.presentation.dto.response.ProductResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductUpdateService implements ProductUpdateUseCase {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public ProductResponse updateProduct(String sellerId, String productId, ProductUpdateRequest request) {
        Product product = findProduct(productId);
        validateSellerAuthorization(product, sellerId);

        Category category = categoryRepository.findById(request.categoryId());

        product.updateProductInfo(request.title(), request.description(), request.price());
        product.updateCategory(category);
        product.updateStock(request.stockQuantity());

        Product saved = saveProduct(product);
        return ProductResponse.from(saved);
    }

    @Override
    public ProductResponse increaseStock(String sellerId, String productId, Integer quantity) {
        Product product = findProduct(productId);
        validateSellerAuthorization(product, sellerId);
        product.increaseStock(quantity);
        Product saved = saveProduct(product);
        return ProductResponse.from(saved);
    }

    @Override
    public ProductResponse decreaseStock(String sellerId, String productId, Integer quantity) {
        Product product = findProduct(productId);
        validateSellerAuthorization(product, sellerId);
        product.decreaseStock(quantity);
        Product saved = saveProduct(product);
        return ProductResponse.from(saved);
    }

    @Override
    public ProductResponse updateStatus(String sellerId, String productId, ProductStatus status) {
        Product product = findProduct(productId);
        validateSellerAuthorization(product, sellerId);
        product.updateStatus(status);
        Product saved = saveProduct(product);
        return ProductResponse.from(saved);
    }

    @Override
    public ProductResponse restoreProduct(String sellerId, String productId) {
        Product product = findProduct(productId);
        validateSellerAuthorization(product, sellerId);
        product.restore();
        Product saved = saveProduct(product);
        return ProductResponse.from(saved);
    }

    private Product findProduct(String productId) {
        return productRepository.findById(UUID.fromString(productId))
                .orElseThrow(ProductNotFoundException::new);
    }

    private void validateSellerAuthorization(Product product, String sellerId) {
        product.validateSeller(UUID.fromString(sellerId));
    }

    private Product saveProduct(Product product) {
        return productRepository.save(product);
    }
}
