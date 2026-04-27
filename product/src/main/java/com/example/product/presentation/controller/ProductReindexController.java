package com.example.product.presentation.controller;

import com.example.product.domain.entity.Product;
import com.example.product.domain.entity.ProductImage;
import com.example.product.domain.repository.ProductImageRepository;
import com.example.product.domain.repository.ProductRepository;
import com.example.product.domain.repository.ProductSearchRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class ProductReindexController {

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductSearchRepository productSearchRepository;

    @PostMapping("/reindex")
    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Integer>> reindexAll() {
        int indexed = 0;
        int failed = 0;
        int page = 0;
        final int PAGE_SIZE = 100;

        Page<Product> productPage;
        do {
            productPage = productRepository.findAll(PageRequest.of(page++, PAGE_SIZE));

            for (Product product : productPage.getContent()) {
                if (product.isDeleted()) {
                    continue;
                }
                try {
                    String thumbnailS3Key = productImageRepository
                            .findThumbnailByProductId(product.getProductId())
                            .map(ProductImage::getS3Key)
                            .orElse(null);

                    List<String> categoryIds = List.of(
                            product.getCategory().getCategoryId().toString()
                    );

                    productSearchRepository.index(product, categoryIds, thumbnailS3Key);
                    indexed++;
                } catch (Exception e) {
                    log.error("ES 인덱싱 실패: productId={}, error={}", product.getProductId(), e.getMessage());
                    failed++;
                }
            }
        } while (!productPage.isLast());

        log.info("ES 재인덱싱 완료: indexed={}, failed={}", indexed, failed);
        return ResponseEntity.ok(Map.of("indexed", indexed, "failed", failed));
    }
}
