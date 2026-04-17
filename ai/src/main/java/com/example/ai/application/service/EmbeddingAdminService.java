package com.example.ai.application.service;

import com.example.ai.application.dto.EmbeddingAdminResult;
import com.example.ai.application.dto.ProductDeactivateCommand;
import com.example.ai.application.dto.ProductEmbeddingCommand;
import com.example.ai.application.usecase.EmbeddingAdminUseCase;
import com.example.ai.application.usecase.ProductEmbeddingUseCase;
import com.example.ai.domain.repository.ProductEmbeddingRepository;
import com.example.ai.infrastructure.client.ProductCatalogClient;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class EmbeddingAdminService implements EmbeddingAdminUseCase {

    private static final String INACTIVE_STATUS = "INACTIVE";

    private final ProductCatalogClient productCatalogClient;
    private final ProductEmbeddingRepository productEmbeddingRepository;
    private final ProductEmbeddingUseCase productEmbeddingUseCase;

    @Override
    public EmbeddingAdminResult backfillMissing() {
        ProcessingCounter counter = new ProcessingCounter();

        productCatalogClient.fetchAllProducts().forEach(product -> {
            counter.processed++;
            try {
                if (isInactive(product.status())) {
                    counter.skipped++;
                    return;
                }

                if (productEmbeddingRepository.findByProductId(product.productId()).isPresent()) {
                    counter.skipped++;
                    return;
                }

                productEmbeddingUseCase.embedding(toEmbeddingCommand(product));
                counter.success++;
            } catch (RuntimeException e) {
                counter.failed++;
                log.warn("Backfill failed: productId={}", product.productId(), e);
            }
        });

        return counter.toResult();
    }

    @Override
    public EmbeddingAdminResult reindexAll() {
        ProcessingCounter counter = new ProcessingCounter();

        productCatalogClient.fetchAllProducts().forEach(product -> {
            counter.processed++;
            try {
                if (isInactive(product.status())) {
                    productEmbeddingUseCase.deactivate(new ProductDeactivateCommand(
                            product.productId(),
                            safeSourceUpdatedAt(product.sourceUpdatedAt())
                    ));
                    counter.success++;
                    return;
                }

                productEmbeddingUseCase.embedding(toEmbeddingCommand(product));
                counter.success++;
            } catch (RuntimeException e) {
                counter.failed++;
                log.warn("Reindex failed: productId={}", product.productId(), e);
            }
        });

        return counter.toResult();
    }

    private ProductEmbeddingCommand toEmbeddingCommand(ProductCatalogClient.ProductSnapshot product) {
        return new ProductEmbeddingCommand(
                product.productId(),
                product.title(),
                product.categoryName(),
                product.description(),
                safeSourceUpdatedAt(product.sourceUpdatedAt())
        );
    }

    private LocalDateTime safeSourceUpdatedAt(LocalDateTime sourceUpdatedAt) {
        return sourceUpdatedAt == null ? LocalDateTime.now() : sourceUpdatedAt;
    }

    private boolean isInactive(String status) {
        return INACTIVE_STATUS.equalsIgnoreCase(status);
    }

    private static class ProcessingCounter {
        private int processed;
        private int success;
        private int skipped;
        private int failed;

        private EmbeddingAdminResult toResult() {
            return new EmbeddingAdminResult(processed, success, skipped, failed);
        }
    }
}

