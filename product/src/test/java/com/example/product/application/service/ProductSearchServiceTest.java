package com.example.product.application.service;

import com.example.product.common.exception.ProductNotFoundException;
import com.example.product.domain.entity.Category;
import com.example.product.domain.entity.Product;
import com.example.product.domain.entity.ProductImage;
import com.example.product.domain.enumtype.ProductStatus;
import com.example.product.domain.enumtype.ProductType;
import com.example.product.domain.model.ProductSearchResult;
import com.example.product.domain.repository.CategoryRepository;
import com.example.product.domain.repository.FileStorageRepository;
import com.example.product.domain.repository.ProductImageRepository;
import com.example.product.domain.repository.ProductRepository;
import com.example.product.domain.repository.ProductSearchRepository;
import com.example.product.presentation.dto.response.ProductResponse;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * ProductSearchService 단위 테스트
 * - 상품 검색, 조회 관련 비즈니스 로직을 검증
 * - Repository를 Mocking하여 Service 계층만 독립적으로 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductSearchService 테스트")
class ProductSearchServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductImageRepository productImageRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private FileStorageRepository fileStorageRepository;

    @Mock
    private ProductSearchRepository productSearchRepository;

    @InjectMocks
    private ProductSearchService productSearchService;

    private Category category;
    private Product product;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        category = createCategory("한식");
        product = createProduct("6ba7b810-9dad-11d1-80b4-00c04fd430c8", "김치찌개");
        pageable = PageRequest.of(0, 10);
    }

    @Nested
    @DisplayName("통합 필터 검색")
    class 통합_필터_검색 {

        @Test
        void 필터_조건_없이_전체_상품을_조회한다() {
            // given
            Page<ProductSearchResult> searchResult = new PageImpl<>(List.of(createSearchResult("김치찌개")));
            given(productSearchRepository.searchProducts(isNull(), isNull(), isNull(), isNull(), eq(pageable)))
                    .willReturn(searchResult);
            given(fileStorageRepository.generatePresignedUrl(anyString())).willReturn("http://presigned-url");

            // when
            Page<ProductResponse> result = productSearchService.findDisplayProducts(
                    null, null, null, null, pageable
            );

            // then
            assertThat(result).hasSize(1);
            assertThat(result.getContent().get(0).title()).isEqualTo("김치찌개");
            verify(productSearchRepository, times(1)).searchProducts(isNull(), isNull(), isNull(), isNull(), eq(pageable));
        }

        @Test
        void 카테고리_ID로_상품을_필터링한다() {
            // given
            String categoryId = category.getCategoryId().toString();
            List<UUID> categoryIds = List.of(category.getCategoryId());
            Page<ProductSearchResult> searchResult = new PageImpl<>(List.of(createSearchResult("김치찌개")));

            given(categoryRepository.findAllDescendantIds(any(UUID.class))).willReturn(Collections.emptyList());
            given(productSearchRepository.searchProducts(eq(categoryIds), isNull(), isNull(), isNull(), eq(pageable)))
                    .willReturn(searchResult);
            given(fileStorageRepository.generatePresignedUrl(anyString())).willReturn("http://presigned-url");

            // when
            Page<ProductResponse> result = productSearchService.findDisplayProducts(
                    categoryId, null, null, null, pageable
            );

            // then
            assertThat(result).hasSize(1);
            verify(categoryRepository, times(1)).findAllDescendantIds(any(UUID.class));
            verify(productSearchRepository, times(1)).searchProducts(eq(categoryIds), isNull(), isNull(), isNull(), eq(pageable));
        }

        @Test
        void 하위_카테고리를_포함하여_상품을_조회한다() {
            // given
            String categoryId = category.getCategoryId().toString();
            UUID subCategoryId = UUID.randomUUID();
            List<UUID> categoryIds = new ArrayList<>();
            categoryIds.add(category.getCategoryId());
            categoryIds.add(subCategoryId);
            Page<ProductSearchResult> searchResult = new PageImpl<>(List.of(createSearchResult("김치찌개")));

            given(categoryRepository.findAllDescendantIds(any(UUID.class))).willReturn(List.of(subCategoryId));
            given(productSearchRepository.searchProducts(eq(categoryIds), isNull(), isNull(), isNull(), eq(pageable)))
                    .willReturn(searchResult);
            given(fileStorageRepository.generatePresignedUrl(anyString())).willReturn("http://presigned-url");

            // when
            Page<ProductResponse> result = productSearchService.findDisplayProducts(
                    categoryId, null, null, null, pageable
            );

            // then
            assertThat(result).hasSize(1);
            verify(categoryRepository, times(1)).findAllDescendantIds(any(UUID.class));
        }

        @Test
        void 키워드로_상품을_검색한다() {
            // given
            String keyword = "김치";
            Page<ProductSearchResult> searchResult = new PageImpl<>(List.of(createSearchResult("김치찌개")));
            given(productSearchRepository.searchProducts(isNull(), eq(keyword), isNull(), isNull(), eq(pageable)))
                    .willReturn(searchResult);
            given(fileStorageRepository.generatePresignedUrl(anyString())).willReturn("http://presigned-url");

            // when
            Page<ProductResponse> result = productSearchService.findDisplayProducts(
                    null, keyword, null, null, pageable
            );

            // then
            assertThat(result).hasSize(1);
            verify(productSearchRepository, times(1)).searchProducts(isNull(), eq(keyword), isNull(), isNull(), eq(pageable));
        }

        @Test
        void 가격_범위로_상품을_필터링한다() {
            // given
            BigDecimal minPrice = new BigDecimal("5000");
            BigDecimal maxPrice = new BigDecimal("10000");
            Page<ProductSearchResult> searchResult = new PageImpl<>(List.of(createSearchResult("김치찌개")));
            given(productSearchRepository.searchProducts(isNull(), isNull(), eq(minPrice), eq(maxPrice), eq(pageable)))
                    .willReturn(searchResult);
            given(fileStorageRepository.generatePresignedUrl(anyString())).willReturn("http://presigned-url");

            // when
            Page<ProductResponse> result = productSearchService.findDisplayProducts(
                    null, null, minPrice, maxPrice, pageable
            );

            // then
            assertThat(result).hasSize(1);
            verify(productSearchRepository, times(1)).searchProducts(isNull(), isNull(), eq(minPrice), eq(maxPrice), eq(pageable));
        }

        @Test
        void 모든_필터_조건을_조합하여_상품을_검색한다() {
            // given
            String categoryId = category.getCategoryId().toString();
            String keyword = "김치";
            BigDecimal minPrice = new BigDecimal("5000");
            BigDecimal maxPrice = new BigDecimal("10000");
            List<UUID> categoryIds = List.of(category.getCategoryId());
            Page<ProductSearchResult> searchResult = new PageImpl<>(List.of(createSearchResult("김치찌개")));

            given(categoryRepository.findAllDescendantIds(any(UUID.class))).willReturn(Collections.emptyList());
            given(productSearchRepository.searchProducts(eq(categoryIds), eq(keyword), eq(minPrice), eq(maxPrice), eq(pageable)))
                    .willReturn(searchResult);
            given(fileStorageRepository.generatePresignedUrl(anyString())).willReturn("http://presigned-url");

            // when
            Page<ProductResponse> result = productSearchService.findDisplayProducts(
                    categoryId, keyword, minPrice, maxPrice, pageable
            );

            // then
            assertThat(result).hasSize(1);
            verify(productSearchRepository, times(1)).searchProducts(eq(categoryIds), eq(keyword), eq(minPrice), eq(maxPrice), eq(pageable));
        }

        @Test
        void 카테고리_ID가_빈_문자열이면_null로_처리한다() {
            // given
            Page<ProductSearchResult> searchResult = new PageImpl<>(List.of(createSearchResult("김치찌개")));
            given(productSearchRepository.searchProducts(isNull(), isNull(), isNull(), isNull(), eq(pageable)))
                    .willReturn(searchResult);
            given(fileStorageRepository.generatePresignedUrl(anyString())).willReturn("http://presigned-url");

            // when
            Page<ProductResponse> result = productSearchService.findDisplayProducts(
                    "", null, null, null, pageable
            );

            // then
            assertThat(result).hasSize(1);
            verify(categoryRepository, never()).findAllDescendantIds(any());
        }

        @Test
        void 검색_결과가_없으면_빈_페이지를_반환한다() {
            // given
            given(productSearchRepository.searchProducts(isNull(), isNull(), isNull(), isNull(), eq(pageable)))
                    .willReturn(Page.empty());

            // when
            Page<ProductResponse> result = productSearchService.findDisplayProducts(
                    null, null, null, null, pageable
            );

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("인기 상품 조회")
    class 인기_상품_조회 {

        @Test
        void 인기_상품_목록을_조회한다() {
            // given
            Page<Product> productPage = new PageImpl<>(List.of(product));
            given(productRepository.findPopularProducts(eq(pageable)))
                    .willReturn(productPage);

            // when
            Page<ProductResponse> result = productSearchService.findPopularProducts(pageable);

            // then
            assertThat(result).hasSize(1);
            verify(productRepository, times(1)).findPopularProducts(eq(pageable));
        }

        @Test
        void 인기_상품이_없으면_빈_페이지를_반환한다() {
            // given
            given(productRepository.findPopularProducts(eq(pageable)))
                    .willReturn(Page.empty());

            // when
            Page<ProductResponse> result = productSearchService.findPopularProducts(pageable);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("전체 상품 조회")
    class 전체_상품_조회 {

        @Test
        void 전체_상품을_조회한다() {
            // given
            Page<Product> productPage = new PageImpl<>(List.of(product));
            given(productRepository.findAll(eq(pageable))).willReturn(productPage);

            // when
            Page<ProductResponse> result = productSearchService.getAllProducts(pageable);

            // then
            assertThat(result).hasSize(1);
            verify(productRepository, times(1)).findAll(eq(pageable));
        }
    }

    @Nested
    @DisplayName("판매자별 상품 조회")
    class 판매자별_상품_조회 {

        @Test
        void 판매자_ID로_상품을_조회한다() {
            // given
            String sellerId = "6ba7b810-9dad-11d1-80b4-00c04fd430c8";
            Page<Product> productPage = new PageImpl<>(List.of(product));
            given(productRepository.findBySellerId(any(UUID.class), eq(pageable)))
                    .willReturn(productPage);

            // when
            Page<ProductResponse> result = productSearchService.findBySellerId(sellerId, pageable);

            // then
            assertThat(result).hasSize(1);
            verify(productRepository, times(1)).findBySellerId(any(UUID.class), eq(pageable));
        }

        @Test
        void 해당_판매자의_상품이_없으면_빈_페이지를_반환한다() {
            // given
            String sellerId = "6ba7b810-9dad-11d1-80b4-00c04fd430c8";
            given(productRepository.findBySellerId(any(UUID.class), eq(pageable)))
                    .willReturn(Page.empty());

            // when
            Page<ProductResponse> result = productSearchService.findBySellerId(sellerId, pageable);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("상품 상세 조회")
    class 상품_상세_조회 {

        @Test
        void 상품_ID로_상세_정보를_조회하고_조회수를_증가시킨다() {
            // given
            String productId = product.getProductId().toString();
            List<ProductImage> images = List.of(
                    createProductImage(product.getProductId(), "test.jpg", true)
            );
            int initialViewCount = product.getViewCount();

            given(productRepository.findById(any(UUID.class))).willReturn(Optional.of(product));
            given(productRepository.save(any(Product.class))).willReturn(product);
            given(productImageRepository.findByProductId(any(UUID.class))).willReturn(images);
            given(fileStorageRepository.generatePresignedUrl(anyString())).willReturn("http://presigned-url");

            // when
            ProductResponse result = productSearchService.findById(productId);

            // then
            assertThat(result).isNotNull();
            assertThat(result.title()).isEqualTo("김치찌개");
            assertThat(product.getViewCount()).isEqualTo(initialViewCount + 1);
            verify(productRepository, times(1)).findById(any(UUID.class));
            verify(productRepository, times(1)).save(eq(product));
            verify(productImageRepository, times(1)).findByProductId(any(UUID.class));
        }

        @Test
        void 존재하지_않는_상품을_조회하면_예외가_발생한다() {
            // given
            String productId = UUID.randomUUID().toString();
            given(productRepository.findById(any(UUID.class))).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> productSearchService.findById(productId))
                    .isInstanceOf(ProductNotFoundException.class);
            verify(productRepository, times(1)).findById(any(UUID.class));
            verify(productRepository, never()).save(any());
        }
    }

    // 테스트 헬퍼 메소드
    private Product createProduct(String sellerId, String title) {
        return Product.create(
                sellerId,
                title,
                "맛있는 " + title,
                new BigDecimal("8000"),
                50,
                category,
                ProductType.GENERAL
        );
    }

    private Category createCategory(String name) {
        return Category.createRoot(name, "테스트 카테고리", 1);
    }

    private ProductSearchResult createSearchResult(String title) {
        return new ProductSearchResult(
                UUID.randomUUID(),
                UUID.randomUUID(),
                category.getCategoryId(),
                category.getName(),
                title,
                "맛있는 " + title,
                new BigDecimal("8000"),
                50,
                ProductStatus.ACTIVE.name(),
                ProductType.GENERAL.name(),
                0,
                "thumbnail.jpg",
                LocalDateTime.now()
        );
    }

    private ProductImage createProductImage(UUID productId, String s3Key, boolean isThumbnail) {
        return ProductImage.create(
                UUID.randomUUID(),
                productId,
                s3Key,
                1,
                isThumbnail,
                java.time.LocalDateTime.now()
        );
    }
}
