package com.example.product.infrastructure.elasticsearch.repository;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.mapping.FieldType;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.example.product.domain.entity.Product;
import com.example.product.domain.model.ProductSearchResult;
import com.example.product.domain.repository.ProductSearchRepository;
import com.example.product.infrastructure.elasticsearch.document.ProductDocument;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.stereotype.Repository;

@Repository
public class ProductSearchRepositoryImpl implements ProductSearchRepository {

    private static final Logger log = LoggerFactory.getLogger(ProductSearchRepositoryImpl.class);
    private static final String INDEX_NAME = "products";

    private final ElasticsearchOperations operations;

    public ProductSearchRepositoryImpl(ElasticsearchOperations operations) {
        this.operations = operations;
    }

    @Override
    public Page<ProductSearchResult> searchProducts(
            List<UUID> categoryIds,
            String keyword,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable) {

        Query query = buildQuery(categoryIds, keyword, minPrice, maxPrice);

        SearchHits<ProductDocument> searchHits;
        try {
            SortOptions sort = SortOptions.of(s -> s
                    .field(f -> f
                            .field("createdAt")
                            .order(SortOrder.Desc)
                            .missing("_last")
                            .unmappedType(FieldType.Date)
                    )
            );

            NativeQuery nativeQuery = NativeQuery.builder()
                    .withQuery(query)
                    .withPageable(pageable)
                    .withSort(sort)
                    .build();

            searchHits = operations.search(
                    nativeQuery, ProductDocument.class, IndexCoordinates.of(INDEX_NAME));
        } catch (Exception e) {
            log.error("상품 검색 ES 통신 실패: {}", e.getMessage(), e);
            return Page.empty(pageable);
        }

        List<ProductSearchResult> results = new ArrayList<>();
        for (SearchHit<ProductDocument> hit : searchHits) {
            try {
                results.add(hit.getContent().toSearchResult());
            } catch (Exception e) {
                log.error("상품 검색 결과 변환 실패: id={}, error={}", hit.getId(), e.getMessage());
            }
        }

        return new PageImpl<>(results, pageable, searchHits.getTotalHits());
    }

    @Override
    public void index(Product product, List<String> allCategoryIds, String thumbnailS3Key) {
        ProductDocument document = new ProductDocument(product, allCategoryIds, thumbnailS3Key);
        try {
            operations.save(document, IndexCoordinates.of(INDEX_NAME));
            log.debug("상품 인덱싱 완료: {}", product.getProductId());
        } catch (Exception e) {
            log.error("상품 인덱싱 실패: productId={}, error={}", product.getProductId(), e.getMessage());
            throw new RuntimeException("상품 인덱싱 실패", e);
        }
    }

    @Override
    public void delete(UUID productId) {
        try {
            operations.delete(productId.toString(), IndexCoordinates.of(INDEX_NAME));
            log.debug("상품 인덱스 삭제 완료: {}", productId);
        } catch (Exception e) {
            log.error("상품 인덱스 삭제 실패: productId={}, error={}", productId, e.getMessage());
            throw new RuntimeException("상품 인덱스 삭제 실패", e);
        }
    }

    private Query buildQuery(List<UUID> categoryIds, String keyword,
                             BigDecimal minPrice, BigDecimal maxPrice) {

        List<Query> must = new ArrayList<>();
        List<Query> filter = new ArrayList<>();

        if (hasText(keyword)) {
            must.add(Query.of(query -> query.multiMatch(multiMatch -> multiMatch
                    .fields("title", "description")
                    .query(keyword)
            )));
        }

        if (categoryIds != null && !categoryIds.isEmpty()) {
            filter.add(termsQuery("categoryIds", categoryIds));
        }

        if (minPrice != null || maxPrice != null) {
            filter.add(rangeQuery("price", minPrice, maxPrice));
        }

        filter.add(Query.of(query -> query.term(term -> term
                .field("status")
                .value("ACTIVE")
        )));

        return Query.of(query -> query.bool(bool -> {
            if (!must.isEmpty()) {
                bool.must(must);
            }
            bool.filter(filter);
            return bool;
        }));
    }

    private Query termsQuery(String field, List<UUID> values) {
        List<FieldValue> fieldValues = values.stream()
                .map(uuid -> FieldValue.of(uuid.toString()))
                .toList();

        return Query.of(query -> query.terms(terms -> terms
                .field(field)
                .terms(termsValue -> termsValue.value(fieldValues))
        ));
    }

    private Query rangeQuery(String field, BigDecimal min, BigDecimal max) {
        return Query.of(query -> query.range(range -> range
                .number(numberRange -> {
                    numberRange.field(field);
                    if (min != null) {
                        numberRange.gte(min.doubleValue());
                    }
                    if (max != null) {
                        numberRange.lte(max.doubleValue());
                    }
                    return numberRange;
                })
        ));
    }

    private boolean hasText(String text) {
        return text != null && !text.isBlank();
    }
}
