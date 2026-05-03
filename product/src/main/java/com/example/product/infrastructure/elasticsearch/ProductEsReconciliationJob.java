package com.example.product.infrastructure.elasticsearch;

import com.example.product.domain.entity.Product;
import com.example.product.domain.entity.ProductImage;
import com.example.product.domain.repository.ProductImageRepository;
import com.example.product.domain.repository.ProductRepository;
import com.example.product.domain.repository.ProductSearchRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(Integer.MAX_VALUE)
@ConditionalOnProperty(name = "product.elasticsearch.init-index", havingValue = "true")
public class ProductEsReconciliationJob implements ApplicationRunner {

    private static final long FIXED_DELAY_MS = 5 * 60_000L;
    private static final long INITIAL_DELAY_MS = 60_000L;
    private static final int LOOKBACK_MINUTES = 30;
    private static final int STARTUP_LOOKBACK_MINUTES = 24 * 60;

    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductSearchRepository productSearchRepository;

    @Override
    @Transactional(readOnly = true)
    public void run(ApplicationArguments args) {
        log.info("ES 재동기화 부팅 패스 시작: lookback={}분", STARTUP_LOOKBACK_MINUTES);
        runReconciliation(STARTUP_LOOKBACK_MINUTES);
    }

    @Scheduled(fixedDelay = FIXED_DELAY_MS, initialDelay = INITIAL_DELAY_MS)
    @Transactional(readOnly = true)
    public void reconcile() {
        runReconciliation(LOOKBACK_MINUTES);
    }

    private void runReconciliation(int lookbackMinutes) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(lookbackMinutes);
        List<UUID> recentIds = productRepository.findIdsByUpdatedAtAfter(since);
        if (recentIds.isEmpty()) {
            return;
        }

        Set<UUID> existing;
        try {
            existing = productSearchRepository.findExistingIds(recentIds);
        } catch (Exception e) {
            log.error("ES 존재 확인 실패, 재동기화 이번 주기 건너뜀", e);
            return;
        }

        List<UUID> missing = recentIds.stream()
                .filter(id -> !existing.contains(id))
                .toList();

        if (missing.isEmpty()) {
            return;
        }

        log.warn("ES 동기화 누락 감지: count={}, since={}", missing.size(), since);

        int recovered = 0;
        for (UUID productId : missing) {
            try {
                indexOne(productId);
                recovered++;
            } catch (Exception e) {
                log.error("ES 재동기화 실패: productId={}", productId, e);
            }
        }

        log.info("ES 재동기화 완료: recovered={}/{}", recovered, missing.size());
    }

    private void indexOne(UUID productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null || product.isDeleted()) {
            return;
        }

        String thumbnailS3Key = productImageRepository.findThumbnailByProductId(productId)
                .map(ProductImage::getS3Key)
                .orElse(null);

        List<String> categoryIds = product.getCategory().collectIdHierarchy().stream()
                .map(UUID::toString)
                .toList();

        productSearchRepository.index(product, categoryIds, thumbnailS3Key);
    }
}
