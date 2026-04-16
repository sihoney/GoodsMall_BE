package com.example.ai.application.service;

import com.example.ai.application.dto.ProductDeactivateCommand;
import com.example.ai.application.dto.ProductEmbeddingCommand;
import com.example.ai.application.usecase.ProductEmbeddingUseCase;
import com.example.ai.common.exception.AiEmbeddingException;
import com.example.ai.domain.entity.ProductEmbedding;
import com.example.ai.domain.repository.ProductEmbeddingRepository;
import com.example.ai.domain.service.EmbeddingGenerator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductEmbeddingService implements ProductEmbeddingUseCase {

    private final ProductEmbeddingTextComposer textComposer;
    private final EmbeddingGenerator embeddingGenerator;
    private final ProductEmbeddingRepository productEmbeddingRepository;

    @Override
    public void embedding(ProductEmbeddingCommand command) {
        validateCommand(command);

        String embeddingInput = textComposer.compose(command);
        List<Float> embeddingVector = embeddingGenerator.generate(embeddingInput);

        ProductEmbedding productEmbedding = productEmbeddingRepository.findByProductId(command.productId())
                .map(existing -> updateExistingEmbedding(existing, embeddingVector, command))
                .orElseGet(() -> ProductEmbedding.create(
                        command.productId(),
                        embeddingVector,
                        command.sourceUpdatedAt()
                ));

        productEmbeddingRepository.save(productEmbedding);
        log.info("Product embedding success: productId={}", command.productId());
    }

    @Override
    public void deactivate(ProductDeactivateCommand command) {
        validateDeactivateCommand(command);

        productEmbeddingRepository.findByProductId(command.productId())
                .ifPresentOrElse(
                        existing -> deactivateExistingEmbedding(existing, command),
                        () -> log.info("Skip deactivate: embedding not found, productId={}", command.productId())
                );
    }

    private void validateCommand(ProductEmbeddingCommand command) {
        if (command == null) {
            throw new AiEmbeddingException("임베딩 요청 데이터가 비어 있습니다.");
        }
        if (command.productId() == null) {
            throw new AiEmbeddingException("productId는 필수입니다.");
        }
        if (command.sourceUpdatedAt() == null) {
            throw new AiEmbeddingException("sourceUpdatedAt은 필수입니다.");
        }
        if (isAllBlank(command.productName(), command.categoryName(), command.description())) {
            throw new AiEmbeddingException("임베딩 입력 텍스트가 비어 있습니다.");
        }
    }

    private boolean isAllBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return false;
            }
        }
        return true;
    }

    private ProductEmbedding updateExistingEmbedding(
            ProductEmbedding existing,
            List<Float> embeddingVector,
            ProductEmbeddingCommand command
    ) {
        if (existing.getSourceUpdatedAt() != null && existing.getSourceUpdatedAt().isAfter(command.sourceUpdatedAt())) {
            log.info("Skip stale embedding event: productId={}, sourceUpdatedAt={}",
                     command.productId(), command.sourceUpdatedAt());
            return existing;
        }

        existing.updateEmbedding(embeddingVector, command.sourceUpdatedAt());
        return existing;
    }

    private void validateDeactivateCommand(ProductDeactivateCommand command) {
        if (command == null) {
            throw new AiEmbeddingException("비활성화 요청 데이터가 비어 있습니다.");
        }
        if (command.productId() == null) {
            throw new AiEmbeddingException("productId는 필수입니다.");
        }
        if (command.sourceUpdatedAt() == null) {
            throw new AiEmbeddingException("sourceUpdatedAt은 필수입니다.");
        }
    }

    private void deactivateExistingEmbedding(ProductEmbedding existing, ProductDeactivateCommand command) {
        if (existing.getSourceUpdatedAt() != null && existing.getSourceUpdatedAt().isAfter(command.sourceUpdatedAt())) {
            log.info("Skip stale deactivate event: productId={}, sourceUpdatedAt={}",
                    command.productId(), command.sourceUpdatedAt());
            return;
        }

        existing.deactivate(command.sourceUpdatedAt());
        productEmbeddingRepository.save(existing);
        log.info("Product embedding deactivated: productId={}", command.productId());
    }
}
