package com.example.product.infrastructure.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import com.example.product.domain.entity.Product;
import com.example.product.domain.entity.ProductImage;
import com.example.product.domain.repository.ProductImageRepository;
import com.example.product.domain.repository.ProductRepository;
import com.example.product.domain.repository.ProductSearchRepository;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "product.elasticsearch.init-index", havingValue = "true")
public class ProductIndexInitializer implements ApplicationRunner {

    private static final String INDEX_NAME = "products";
    private static final int BATCH_SIZE = 100;

    private final ElasticsearchClient client;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductSearchRepository productSearchRepository;

    @Override
    @Transactional(readOnly = true)
    public void run(ApplicationArguments args) {
        try {
            boolean exists = client.indices().exists(r -> r.index(INDEX_NAME)).value();
            if (exists) {
                log.info("Elasticsearch 인덱스 이미 존재, 초기화 건너뜀: {}", INDEX_NAME);
                return;
            }

            createIndex();
            reindexAll();

        } catch (IOException e) {
            log.error("Elasticsearch 인덱스 초기화 실패: {}", e.getMessage(), e);
            throw new RuntimeException("Elasticsearch 인덱스 초기화 실패", e);
        }
    }

    private void createIndex() throws IOException {
        try (InputStream settings = new ClassPathResource("elasticsearch/product-settings.json").getInputStream();
             InputStream mapping = new ClassPathResource("elasticsearch/product-mapping.json").getInputStream()) {

            CreateIndexResponse response = client.indices().create(c -> c
                    .index(INDEX_NAME)
                    .settings(s -> s.withJson(settings))
                    .mappings(m -> m.withJson(mapping))
            );

            log.info("Elasticsearch 인덱스 생성 완료: {}, acknowledged={}", INDEX_NAME, response.acknowledged());
        }
    }

    private void reindexAll() {
        int indexed = 0;
        int failed = 0;
        int page = 0;

        Page<Product> productPage;
        do {
            productPage = productRepository.findAll(PageRequest.of(page++, BATCH_SIZE));

            for (Product product : productPage.getContent()) {
                if (product.isDeleted()) {
                    continue;
                }
                try {
                    String thumbnailS3Key = productImageRepository
                            .findThumbnailByProductId(product.getProductId())
                            .map(ProductImage::getS3Key)
                            .orElse(null);

                    List<String> categoryIds = product.getCategory().collectIdHierarchy().stream()
                            .map(java.util.UUID::toString)
                            .toList();

                    productSearchRepository.index(product, categoryIds, thumbnailS3Key);
                    indexed++;
                } catch (Exception e) {
                    log.error("상품 초기 인덱싱 실패: productId={}", product.getProductId(), e);
                    failed++;
                }
            }
        } while (!productPage.isLast());

        log.info("Elasticsearch 초기 인덱싱 완료: indexed={}, failed={}", indexed, failed);
    }
}
