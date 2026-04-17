package com.example.ai.application.service;

import com.example.ai.application.dto.ProductDraftAssistCommand;
import com.example.ai.application.dto.ProductDraftAssistResult;
import com.example.ai.common.exception.AiProductDraftAssistException;
import com.example.ai.application.usecase.ProductDraftAssistUseCase;
import com.example.ai.domain.service.ProductDraftAssistExecutionRepository;
import com.example.ai.domain.service.ProductDraftGenerator;
import com.example.ai.infrastructure.config.ProductDraftAssistProperties;
import java.time.Duration;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductDraftAssistService implements ProductDraftAssistUseCase {

    private final ProductDraftGenerator productDraftGenerator;
    private final ProductDraftAssistExecutionRepository executionRepository;
    private final ProductDraftAssistFingerprintGenerator fingerprintGenerator;
    private final ProductDraftAssistFallbackFactory fallbackFactory;
    private final ProductDraftAssistResultRefiner resultRefiner;
    private final ProductDraftAssistProperties properties;

    @Override
    public ProductDraftAssistResult createProductDraft(ProductDraftAssistCommand command) {
        log.info(
                "Product draft assist requested. imageCount={}, inputFieldCount={}, categoryName={}, categoryPathText={}, thumbnailIndex={}",
                command.images().size(),
                command.inputFields().size(),
                command.categoryName(),
                command.categoryPathText(),
                command.thumbnailIndex()
        );

        String fingerprint = fingerprintGenerator.generate(command);
        Optional<ProductDraftAssistResult> cachedResult = executionRepository.findCachedResult(fingerprint);
        if (cachedResult.isPresent()) {
            log.info("Product draft assist cache hit. fingerprint={}", fingerprint);
            return resultRefiner.refine(cachedResult.get());
        }

        ProductDraftAssistResult fallbackResult = fallbackFactory.create(
                command,
                "AI 추천이 불안정할 수 있어 현재 입력값 기준 초안을 우선 반환했습니다."
        );

        boolean locked = executionRepository.tryLock(
                fingerprint,
                Duration.ofSeconds(sanitizeLockTtlSeconds())
        );
        if (!locked) {
            Optional<ProductDraftAssistResult> waitedResult = waitForCachedResult(fingerprint);
            if (waitedResult.isPresent()) {
                log.info("Product draft assist reused cached result after wait. fingerprint={}", fingerprint);
                return resultRefiner.refine(waitedResult.get());
            }
            return resultRefiner.refine(fallbackResult);
        }

        try {
            ProductDraftAssistResult generatedResult = productDraftGenerator.generate(command);
            ProductDraftAssistResult mergedResult = fallbackFactory.merge(generatedResult, fallbackResult);
            ProductDraftAssistResult refinedResult = resultRefiner.refine(mergedResult);
            executionRepository.cacheResult(
                    fingerprint,
                    refinedResult,
                    Duration.ofSeconds(sanitizeResultTtlSeconds())
            );
            return refinedResult;
        } catch (AiProductDraftAssistException e) {
            log.warn("Product draft assist fallback applied. reason={}", e.getMessage(), e);
            ProductDraftAssistResult refinedFallback = resultRefiner.refine(fallbackResult);
            executionRepository.cacheResult(
                    fingerprint,
                    refinedFallback,
                    Duration.ofSeconds(sanitizeResultTtlSeconds())
            );
            return refinedFallback;
        } finally {
            executionRepository.unlock(fingerprint);
        }
    }

    private Optional<ProductDraftAssistResult> waitForCachedResult(String fingerprint) {
        long waitTimeoutMs = sanitizeWaitTimeoutMs();
        long pollIntervalMs = sanitizePollIntervalMs();
        long deadline = System.currentTimeMillis() + waitTimeoutMs;

        while (System.currentTimeMillis() < deadline) {
            Optional<ProductDraftAssistResult> cachedResult = executionRepository.findCachedResult(fingerprint);
            if (cachedResult.isPresent()) {
                return cachedResult;
            }

            try {
                Thread.sleep(pollIntervalMs);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    private long sanitizeLockTtlSeconds() {
        return properties.lockTtlSeconds() > 0 ? properties.lockTtlSeconds() : 30L;
    }

    private long sanitizeResultTtlSeconds() {
        return properties.resultTtlSeconds() > 0 ? properties.resultTtlSeconds() : 60L;
    }

    private int sanitizeWaitTimeoutMs() {
        return properties.waitTimeoutMs() > 0 ? properties.waitTimeoutMs() : 2000;
    }

    private int sanitizePollIntervalMs() {
        return properties.pollIntervalMs() > 0 ? properties.pollIntervalMs() : 150;
    }
}
