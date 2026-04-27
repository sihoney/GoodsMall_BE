package com.example.product.infrastructure.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "product.elasticsearch.init-index", havingValue = "true")
public class ProductIndexInitializer {

    private static final String INDEX_NAME = "products";

    private final ElasticsearchClient client;

    public ProductIndexInitializer(ElasticsearchClient client) {
        this.client = client;
    }

    @PostConstruct
    public void initIndex() {
        try {
            boolean exists = client.indices().exists(r -> r.index(INDEX_NAME)).value();
            if (exists) {
                log.info("Elasticsearch 인덱스 이미 존재: {}", INDEX_NAME);
                return;
            }

            try (InputStream settings = new ClassPathResource("elasticsearch/product-settings.json").getInputStream();
                 InputStream mapping = new ClassPathResource("elasticsearch/product-mapping.json").getInputStream()) {

                CreateIndexResponse response = client.indices().create(c -> c
                        .index(INDEX_NAME)
                        .settings(s -> s.withJson(settings))
                        .mappings(m -> m.withJson(mapping))
                );

                log.info("Elasticsearch 인덱스 생성 완료: {}, acknowledged={}", INDEX_NAME, response.acknowledged());
            }

        } catch (IOException e) {
            log.error("Elasticsearch 인덱스 생성 실패: {}", e.getMessage(), e);
            throw new RuntimeException("Elasticsearch 인덱스 초기화 실패", e);
        }
    }
}
