package com.example.product.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.product.domain.entity.Category;
import com.example.product.domain.entity.Product;
import com.example.product.domain.enumtype.ProductStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

/**
 * ProductJpaRepository 통합 테스트
 * - 실제 JPA 쿼리 동작을 검증
 * - @SpringBootTest로 전체 컨텍스트와 연동하여 테스트
 */
@SpringBootTest
@Transactional
@DisplayName("ProductJpaRepository 통합 테스트")
class ProductJpaRepositoryTest {

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private Category rootCategory;
    private Category childCategory;
    private UUID sellerId1;
    private UUID sellerId2;

    @BeforeEach
    void setUp() {
        // 테스트용 카테고리 생성
        rootCategory = Category.createRoot("한식", "한식 카테고리", 1);
        entityManager.persist(rootCategory);

        childCategory = Category.createChildByAdmin(
                rootCategory, "국/찌개", "국물 요리", 1
        );
        entityManager.persist(childCategory);

        sellerId1 = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8");
        sellerId2 = UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c9");

        entityManager.flush();
        entityManager.clear();
    }

    @Nested
    @DisplayName("통합 필터 검색")
    class 통합_필터_검색 {

        @Test
        void 필터_없이_ACTIVE_상품_전체를_조회한다() {
            // given
            saveProduct("김치찌개", new BigDecimal("8000"), 50, ProductStatus.ACTIVE, sellerId1);
            saveProduct("된장찌개", new BigDecimal("7500"), 30, ProductStatus.ACTIVE, sellerId1);
            saveProduct("부대찌개", new BigDecimal("12000"), 0, ProductStatus.SOLD_OUT, sellerId1);
            entityManager.flush();

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Product> result = productJpaRepository.findDisplayProductsWithFilters(
                    null, null, null, null, ProductStatus.ACTIVE, pageable
            );

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                    .extracting(Product::getStatus)
                    .containsOnly(ProductStatus.ACTIVE);
        }

        @Test
        void 카테고리_ID로_상품을_필터링한다() {
            // given
            Product product1 = saveProduct("김치찌개", new BigDecimal("8000"), 50, ProductStatus.ACTIVE, sellerId1);
            Product product2 = saveProduct("된장찌개", new BigDecimal("7500"), 30, ProductStatus.ACTIVE, sellerId1);

            List<UUID> categoryIds = List.of(childCategory.getCategoryId());
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Product> result = productJpaRepository.findDisplayProductsWithFilters(
                    categoryIds, null, null, null, ProductStatus.ACTIVE, pageable
            );

            // then
            assertThat(result.getContent()).hasSize(2);
        }

        @Test
        void 키워드로_상품을_검색한다() {
            // given
            saveProduct("김치찌개", "매콤한 김치찌개", new BigDecimal("8000"), 50, ProductStatus.ACTIVE, sellerId1);
            saveProduct("된장찌개", "구수한 된장찌개", new BigDecimal("7500"), 30, ProductStatus.ACTIVE, sellerId1);
            saveProduct("비빔밥", "신선한 나물 비빔밥", new BigDecimal("9000"), 40, ProductStatus.ACTIVE, sellerId1);
            entityManager.flush();

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Product> result = productJpaRepository.findDisplayProductsWithFilters(
                    null, "찌개", null, null, ProductStatus.ACTIVE, pageable
            );

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                    .extracting(Product::getTitle)
                    .containsExactlyInAnyOrder("김치찌개", "된장찌개");
        }

        @Test
        void 키워드가_제목_또는_설명에_포함된_상품을_검색한다() {
            // given
            saveProduct("김치찌개", "매콤한 맛", new BigDecimal("8000"), 50, ProductStatus.ACTIVE, sellerId1);
            saveProduct("매운 떡볶이", "달콤한 맛", new BigDecimal("5000"), 30, ProductStatus.ACTIVE, sellerId1);
            entityManager.flush();

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Product> result = productJpaRepository.findDisplayProductsWithFilters(
                    null, "매", null, null, ProductStatus.ACTIVE, pageable
            );

            // then
            assertThat(result.getContent()).hasSize(2);
        }

        @Test
        void 최소_가격으로_필터링한다() {
            // given
            saveProduct("김치찌개", new BigDecimal("8000"), 50, ProductStatus.ACTIVE, sellerId1);
            saveProduct("된장찌개", new BigDecimal("7500"), 30, ProductStatus.ACTIVE, sellerId1);
            saveProduct("비빔밥", new BigDecimal("9000"), 40, ProductStatus.ACTIVE, sellerId1);
            entityManager.flush();

            Pageable pageable = PageRequest.of(0, 10);
            BigDecimal minPrice = new BigDecimal("8000");

            // when
            Page<Product> result = productJpaRepository.findDisplayProductsWithFilters(
                    null, null, minPrice, null, ProductStatus.ACTIVE, pageable
            );

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                    .extracting(Product::getPrice)
                    .allMatch(price -> price.compareTo(minPrice) >= 0);
        }

        @Test
        void 최대_가격으로_필터링한다() {
            // given
            saveProduct("김치찌개", new BigDecimal("8000"), 50, ProductStatus.ACTIVE, sellerId1);
            saveProduct("된장찌개", new BigDecimal("7500"), 30, ProductStatus.ACTIVE, sellerId1);
            saveProduct("비빔밥", new BigDecimal("9000"), 40, ProductStatus.ACTIVE, sellerId1);
            entityManager.flush();

            Pageable pageable = PageRequest.of(0, 10);
            BigDecimal maxPrice = new BigDecimal("8000");

            // when
            Page<Product> result = productJpaRepository.findDisplayProductsWithFilters(
                    null, null, null, maxPrice, ProductStatus.ACTIVE, pageable
            );

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                    .extracting(Product::getPrice)
                    .allMatch(price -> price.compareTo(maxPrice) <= 0);
        }

        @Test
        void 가격_범위로_필터링한다() {
            // given
            saveProduct("김치찌개", new BigDecimal("8000"), 50, ProductStatus.ACTIVE, sellerId1);
            saveProduct("된장찌개", new BigDecimal("7500"), 30, ProductStatus.ACTIVE, sellerId1);
            saveProduct("비빔밥", new BigDecimal("9000"), 40, ProductStatus.ACTIVE, sellerId1);
            saveProduct("저렴한 메뉴", new BigDecimal("5000"), 20, ProductStatus.ACTIVE, sellerId1);
            saveProduct("비싼 메뉴", new BigDecimal("15000"), 10, ProductStatus.ACTIVE, sellerId1);
            entityManager.flush();

            Pageable pageable = PageRequest.of(0, 10);
            BigDecimal minPrice = new BigDecimal("7000");
            BigDecimal maxPrice = new BigDecimal("9000");

            // when
            Page<Product> result = productJpaRepository.findDisplayProductsWithFilters(
                    null, null, minPrice, maxPrice, ProductStatus.ACTIVE, pageable
            );

            // then
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getContent())
                    .extracting(Product::getPrice)
                    .allMatch(price -> price.compareTo(minPrice) >= 0 && price.compareTo(maxPrice) <= 0);
        }

        @Test
        void 모든_필터를_조합하여_검색한다() {
            // given
            saveProduct("김치찌개", "매콤한 김치찌개", new BigDecimal("8000"), 50, ProductStatus.ACTIVE, sellerId1);
            saveProduct("된장찌개", "구수한 된장찌개", new BigDecimal("7500"), 30, ProductStatus.ACTIVE, sellerId1);
            saveProduct("부대찌개", "햄과 소시지", new BigDecimal("12000"), 40, ProductStatus.ACTIVE, sellerId1);
            entityManager.flush();

            List<UUID> categoryIds = List.of(childCategory.getCategoryId());
            String keyword = "찌개";
            BigDecimal minPrice = new BigDecimal("7000");
            BigDecimal maxPrice = new BigDecimal("10000");
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Product> result = productJpaRepository.findDisplayProductsWithFilters(
                    categoryIds, keyword, minPrice, maxPrice, ProductStatus.ACTIVE, pageable
            );

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                    .extracting(Product::getTitle)
                    .containsExactlyInAnyOrder("김치찌개", "된장찌개");
        }

        @Test
        void 삭제된_상품은_검색_결과에서_제외된다() {
            // given
            Product product1 = saveProduct("김치찌개", new BigDecimal("8000"), 50, ProductStatus.ACTIVE, sellerId1);
            Product product2 = saveProduct("된장찌개", new BigDecimal("7500"), 30, ProductStatus.ACTIVE, sellerId1);
            product2.delete();
            entityManager.persist(product2);
            entityManager.flush();

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Product> result = productJpaRepository.findDisplayProductsWithFilters(
                    null, null, null, null, ProductStatus.ACTIVE, pageable
            );

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("김치찌개");
        }

        @Test
        void 키워드가_빈_문자열이면_전체_상품을_조회한다() {
            // given
            saveProduct("김치찌개", new BigDecimal("8000"), 50, ProductStatus.ACTIVE, sellerId1);
            saveProduct("된장찌개", new BigDecimal("7500"), 30, ProductStatus.ACTIVE, sellerId1);
            entityManager.flush();

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Product> result = productJpaRepository.findDisplayProductsWithFilters(
                    null, "", null, null, ProductStatus.ACTIVE, pageable
            );

            // then
            assertThat(result.getContent()).hasSize(2);
        }

        @Test
        void 조건에_맞는_상품이_없으면_빈_결과를_반환한다() {
            // given
            saveProduct("김치찌개", new BigDecimal("8000"), 50, ProductStatus.ACTIVE, sellerId1);
            entityManager.flush();

            Pageable pageable = PageRequest.of(0, 10);
            BigDecimal minPrice = new BigDecimal("20000");

            // when
            Page<Product> result = productJpaRepository.findDisplayProductsWithFilters(
                    null, null, minPrice, null, ProductStatus.ACTIVE, pageable
            );

            // then
            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("인기 상품 조회")
    class 인기_상품_조회 {

        @Test
        void 조회수가_높은_순으로_상품을_조회한다() {
            // given
            Product product1 = saveProduct("김치찌개", new BigDecimal("8000"), 50, ProductStatus.ACTIVE, sellerId1);
            Product product2 = saveProduct("된장찌개", new BigDecimal("7500"), 30, ProductStatus.ACTIVE, sellerId1);
            Product product3 = saveProduct("비빔밥", new BigDecimal("9000"), 40, ProductStatus.ACTIVE, sellerId1);

            increaseViewCount(product1, 100);
            increaseViewCount(product2, 200);
            increaseViewCount(product3, 50);
            entityManager.flush();

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Product> result = productJpaRepository.findPopularProducts(pageable);

            // then
            assertThat(result.getContent()).hasSize(3);
            assertThat(result.getContent())
                    .extracting(Product::getTitle)
                    .containsExactly("된장찌개", "김치찌개", "비빔밥");
        }

        @Test
        void 삭제된_상품은_인기_상품에서_제외된다() {
            // given
            Product product1 = saveProduct("김치찌개", new BigDecimal("8000"), 50, ProductStatus.ACTIVE, sellerId1);
            Product product2 = saveProduct("된장찌개", new BigDecimal("7500"), 30, ProductStatus.ACTIVE, sellerId1);

            increaseViewCount(product1, 100);
            increaseViewCount(product2, 200);
            product2.delete();
            entityManager.persist(product2);
            entityManager.flush();

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Product> result = productJpaRepository.findPopularProducts(pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("김치찌개");
        }

        @Test
        void 조회수가_같으면_최신순으로_정렬한다() {
            // given
            Product product1 = saveProduct("김치찌개", new BigDecimal("8000"), 50, ProductStatus.ACTIVE, sellerId1);
            Product product2 = saveProduct("된장찌개", new BigDecimal("7500"), 30, ProductStatus.ACTIVE, sellerId1);

            increaseViewCount(product1, 100);
            increaseViewCount(product2, 100);
            entityManager.flush();

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Product> result = productJpaRepository.findPopularProducts(pageable);

            // then
            assertThat(result.getContent()).hasSize(2);
            // 최신순이므로 product2가 먼저
            assertThat(result.getContent().get(0).getTitle()).isEqualTo("된장찌개");
        }
    }

    @Nested
    @DisplayName("판매자별 상품 조회")
    class 판매자별_상품_조회 {

        @Test
        void 특정_판매자의_상품만_조회한다() {
            // given
            saveProduct("김치찌개", new BigDecimal("8000"), 50, ProductStatus.ACTIVE, sellerId1);
            saveProduct("된장찌개", new BigDecimal("7500"), 30, ProductStatus.ACTIVE, sellerId1);
            saveProduct("짜장면", new BigDecimal("6000"), 60, ProductStatus.ACTIVE, sellerId2);
            entityManager.flush();

            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Product> result = productJpaRepository.findBySellerId(sellerId1, pageable);

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                    .extracting(Product::getSellerId)
                    .containsOnly(sellerId1);
        }

        @Test
        void 해당_판매자의_상품이_없으면_빈_결과를_반환한다() {
            // given
            UUID nonExistentSellerId = UUID.randomUUID();
            Pageable pageable = PageRequest.of(0, 10);

            // when
            Page<Product> result = productJpaRepository.findBySellerId(nonExistentSellerId, pageable);

            // then
            assertThat(result.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("페이징 처리")
    class 페이징_처리 {

        @Test
        void 페이지_크기만큼_상품을_조회한다() {
            // given
            for (int i = 0; i < 15; i++) {
                saveProduct("상품" + i, new BigDecimal("8000"), 50, ProductStatus.ACTIVE, sellerId1);
            }
            entityManager.flush();

            Pageable pageable = PageRequest.of(0, 5);

            // when
            Page<Product> result = productJpaRepository.findDisplayProductsWithFilters(
                    null, null, null, null, ProductStatus.ACTIVE, pageable
            );

            // then
            assertThat(result.getContent()).hasSize(5);
            assertThat(result.getTotalElements()).isEqualTo(15);
            assertThat(result.getTotalPages()).isEqualTo(3);
        }

        @Test
        void 두_번째_페이지를_조회한다() {
            // given
            for (int i = 0; i < 15; i++) {
                saveProduct("상품" + i, new BigDecimal("8000"), 50, ProductStatus.ACTIVE, sellerId1);
            }
            entityManager.flush();

            Pageable pageable = PageRequest.of(1, 5);

            // when
            Page<Product> result = productJpaRepository.findDisplayProductsWithFilters(
                    null, null, null, null, ProductStatus.ACTIVE, pageable
            );

            // then
            assertThat(result.getContent()).hasSize(5);
            assertThat(result.getNumber()).isEqualTo(1);
            assertThat(result.isFirst()).isFalse();
            assertThat(result.isLast()).isFalse();
        }
    }

    // 테스트 헬퍼 메소드
    private Product saveProduct(String title, BigDecimal price, Integer stock,
                                 ProductStatus status, UUID sellerId) {
        return saveProduct(title, title + " 설명", price, stock, status, sellerId);
    }

    private Product saveProduct(String title, String description, BigDecimal price,
                                 Integer stock, ProductStatus status, UUID sellerId) {
        Product product = Product.create(
                sellerId.toString(),
                title,
                description,
                price,
                stock,
                childCategory
        );
        product.updateStatus(status);
        entityManager.persist(product);
        return product;
    }

    private void increaseViewCount(Product product, int count) {
        for (int i = 0; i < count; i++) {
            product.increaseViewCount();
        }
        entityManager.persist(product);
    }
}
