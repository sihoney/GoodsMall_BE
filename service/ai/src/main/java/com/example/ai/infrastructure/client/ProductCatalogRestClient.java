package com.example.ai.infrastructure.client;

import com.example.ai.common.exception.AiEmbeddingException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
public class ProductCatalogRestClient implements ProductCatalogClient {

    private final RestClient restClient;
    private final int pageSize;

    public ProductCatalogRestClient(
            RestClient.Builder restClientBuilder,
            @Value("${ai.product-api.base-url:http://localhost:8081}") String baseUrl,
            @Value("${ai.product-api.page-size:200}") int pageSize
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.pageSize = pageSize;
    }

    @Override
    public List<ProductSnapshot> fetchAllProducts() {
        List<ProductSnapshot> products = new ArrayList<>();
        int page = 0;

        while (true) {
            ProductPageResponse response = requestPage(page);
            if (response == null || response.content() == null || response.content().isEmpty()) {
                break;
            }

            response.content().stream()
                    .map(this::toSnapshot)
                    .forEach(products::add);

            if (Boolean.TRUE.equals(response.last())) {
                break;
            }
            page++;
        }

        return products;
    }

    private ProductPageResponse requestPage(int page) {
        try {
            return restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/products/admin/all")
                            .queryParam("page", page)
                            .queryParam("size", pageSize)
                            .build())
                    .retrieve()
                    .body(ProductPageResponse.class);
        } catch (RestClientException e) {
            throw new AiEmbeddingException("Product 서비스 조회에 실패했습니다.", e);
        }
    }

    private ProductSnapshot toSnapshot(ProductPageResponse.ProductItem item) {
        UUID productId = parseUuid(item.productId());
        LocalDateTime sourceUpdatedAt = parseDateTime(item.createdAt());

        return new ProductSnapshot(
                productId,
                item.title(),
                item.categoryName(),
                item.description(),
                item.status(),
                sourceUpdatedAt == null ? LocalDateTime.now() : sourceUpdatedAt
        );
    }

    private UUID parseUuid(UUID productId) {
        if (productId == null) {
            throw new AiEmbeddingException("productId는 필수입니다.");
        }
        return productId;
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException ignore) {
            try {
                return OffsetDateTime.parse(value).toLocalDateTime();
            } catch (DateTimeParseException e) {
                log.warn("createdAt 파싱에 실패했습니다. value={}", value);
                return null;
            }
        }
    }

    private record ProductPageResponse(
            List<ProductItem> content,
            Boolean last
    ) {
        private record ProductItem(
                UUID productId,
                String title,
                String description,
                String status,
                String categoryName,
                String createdAt
        ) {
        }
    }
}

