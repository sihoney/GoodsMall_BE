package com.example.product.application.service;

import com.example.product.common.exception.ProductNotFoundException;
import com.example.product.domain.entity.Category;
import com.example.product.domain.entity.Product;
import com.example.product.domain.entity.ProductImage;
import com.example.product.domain.enumtype.ProductStatus;
import com.example.product.domain.repository.CategoryRepository;
import com.example.product.domain.repository.ProductImageRepository;
import com.example.product.domain.repository.ProductRepository;
import com.example.product.domain.service.ImageUploadService;
import com.example.product.presentation.dto.request.ProductCheckRequest;
import com.example.product.presentation.dto.response.ProductAvailabilityResponse;
import com.example.product.presentation.dto.response.ProductResponse;
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
    private ImageUploadService imageUploadService;

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
            Page<Product> productPage = new PageImpl<>(List.of(product));
            given(productRepository.findDisplayProductsWithFilters(
                    isNull(), isNull(), isNull(), isNull(), eq(pageable)
            )).willReturn(productPage);

            // when
            Page<ProductResponse> result = productSearchService.findDisplayProducts(
                    null, null, null, null, pageable
            );

            // then
            assertThat(result).hasSize(1);
            assertThat(result.getContent().get(0).title()).isEqualTo("김치찌개");
            verify(productRepository, times(1)).findDisplayProductsWithFilters(
                    isNull(), isNull(), isNull(), isNull(), eq(pageable)
            );
        }

        @Test
        void 카테고리_ID로_상품을_필터링한다() {
            // given
            String categoryId = category.getCategoryId().toString();
            List<UUID> categoryIds = List.of(category.getCategoryId());
            Page<Product> productPage = new PageImpl<>(List.of(product));

            given(categoryRepository.findAllDescendantIds(any(UUID.class)))
                    .willReturn(Collections.emptyList());
            given(productRepository.findDisplayProductsWithFilters(
                    eq(categoryIds), isNull(), isNull(), isNull(), eq(pageable)
            )).willReturn(productPage);

            // when
            Page<ProductResponse> result = productSearchService.findDisplayProducts(
                    categoryId, null, null, null, pageable
            );

            // then
            assertThat(result).hasSize(1);
            verify(categoryRepository, times(1)).findAllDescendantIds(any(UUID.class));
            verify(productRepository, times(1)).findDisplayProductsWithFilters(
                    eq(categoryIds), isNull(), isNull(), isNull(), eq(pageable)
            );
        }

        @Test
        void 하위_카테고리를_포함하여_상품을_조회한다() {
            // given
            String categoryId = category.getCategoryId().toString();
            UUID subCategoryId = UUID.randomUUID();
            List<UUID> categoryIds = new ArrayList<>();
            categoryIds.add(category.getCategoryId());
            categoryIds.add(subCategoryId);

            given(categoryRepository.findAllDescendantIds(any(UUID.class)))
                    .willReturn(List.of(subCategoryId));
            given(productRepository.findDisplayProductsWithFilters(
                    eq(categoryIds), isNull(), isNull(), isNull(), eq(pageable)
            )).willReturn(new PageImpl<>(List.of(product)));

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
            Page<Product> productPage = new PageImpl<>(List.of(product));
            given(productRepository.findDisplayProductsWithFilters(
                    isNull(), eq(keyword), isNull(), isNull(), eq(pageable)
            )).willReturn(productPage);

            // when
            Page<ProductResponse> result = productSearchService.findDisplayProducts(
                    null, keyword, null, null, pageable
            );

            // then
            assertThat(result).hasSize(1);
            verify(productRepository, times(1)).findDisplayProductsWithFilters(
                    isNull(), eq(keyword), isNull(), isNull(), eq(pageable)
            );
        }

        @Test
        void 가격_범위로_상품을_필터링한다() {
            // given
            BigDecimal minPrice = new BigDecimal("5000");
            BigDecimal maxPrice = new BigDecimal("10000");
            Page<Product> productPage = new PageImpl<>(List.of(product));
            given(productRepository.findDisplayProductsWithFilters(
                    isNull(), isNull(), eq(minPrice), eq(maxPrice), eq(pageable)
            )).willReturn(productPage);

            // when
            Page<ProductResponse> result = productSearchService.findDisplayProducts(
                    null, null, minPrice, maxPrice, pageable
            );

            // then
            assertThat(result).hasSize(1);
            verify(productRepository, times(1)).findDisplayProductsWithFilters(
                    isNull(), isNull(), eq(minPrice), eq(maxPrice), eq(pageable)
            );
        }

        @Test
        void 모든_필터_조건을_조합하여_상품을_검색한다() {
            // given
            String categoryId = category.getCategoryId().toString();
            String keyword = "김치";
            BigDecimal minPrice = new BigDecimal("5000");
            BigDecimal maxPrice = new BigDecimal("10000");
            List<UUID> categoryIds = List.of(category.getCategoryId());

            given(categoryRepository.findAllDescendantIds(any(UUID.class)))
                    .willReturn(Collections.emptyList());
            given(productRepository.findDisplayProductsWithFilters(
                    eq(categoryIds), eq(keyword), eq(minPrice), eq(maxPrice), eq(pageable)
            )).willReturn(new PageImpl<>(List.of(product)));

            // when
            Page<ProductResponse> result = productSearchService.findDisplayProducts(
                    categoryId, keyword, minPrice, maxPrice, pageable
            );

            // then
            assertThat(result).hasSize(1);
            verify(productRepository, times(1)).findDisplayProductsWithFilters(
                    eq(categoryIds), eq(keyword), eq(minPrice), eq(maxPrice), eq(pageable)
            );
        }

        @Test
        void 카테고리_ID가_빈_문자열이면_null로_처리한다() {
            // given
            Page<Product> productPage = new PageImpl<>(List.of(product));
            given(productRepository.findDisplayProductsWithFilters(
                    isNull(), isNull(), isNull(), isNull(), eq(pageable)
            )).willReturn(productPage);

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
            given(productRepository.findDisplayProductsWithFilters(
                    isNull(), isNull(), isNull(), isNull(), eq(pageable)
            )).willReturn(Page.empty());

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
            given(imageUploadService.generatePresignedUrl(anyString())).willReturn("http://presigned-url");

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

    @Nested
    @DisplayName("상품 재고 확인")
    class 상품_재고_확인 {

        @Test
        void 단일_상품의_재고를_확인한다() {
            // given
            UUID productId = product.getProductId();
            Integer quantity = 10;
            ProductCheckRequest request = new ProductCheckRequest(productId, quantity);
            ProductImage thumbnail = createProductImage(productId, "thumb.jpg", true);

            given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
            given(productRepository.findThumbnailImageByProductId(eq(productId)))
                    .willReturn(Optional.of(thumbnail));

            // when
            List<ProductAvailabilityResponse> result =
                    productSearchService.checkAvailability(List.of(request));

            // then
            assertThat(result).hasSize(1);
            ProductAvailabilityResponse response = result.get(0);
            assertThat(response.getProductId()).isEqualTo(productId);
            assertThat(response.getName()).isEqualTo(product.getTitle());
            assertThat(response.getThumbnailKeySnapshot()).isEqualTo(thumbnail.getS3Key());
        }

        @Test
        void 여러_상품의_재고를_한번에_확인한다() {
            // given
            Product product1 = createProduct("6ba7b810-9dad-11d1-80b4-00c04fd430c8", "김치찌개");
            Product product2 = createProduct("6ba7b810-9dad-11d1-80b4-00c04fd430c9", "된장찌개");

            List<ProductCheckRequest> requests = List.of(
                    new ProductCheckRequest(product1.getProductId(), 5),
                    new ProductCheckRequest(product2.getProductId(), 10)
            );

            given(productRepository.findById(eq(product1.getProductId())))
                    .willReturn(Optional.of(product1));
            given(productRepository.findById(eq(product2.getProductId())))
                    .willReturn(Optional.of(product2));
            given(productRepository.findThumbnailImageByProductId(any(UUID.class)))
                    .willReturn(Optional.empty());

            // when
            List<ProductAvailabilityResponse> result =
                    productSearchService.checkAvailability(requests);

            // then
            assertThat(result).hasSize(2);
            verify(productRepository, times(2)).findById(any(UUID.class));
        }

        @Test
        void 재고_확인시_썸네일이_없으면_null을_반환한다() {
            // given
            UUID productId = product.getProductId();
            ProductCheckRequest request = new ProductCheckRequest(productId, 10);

            given(productRepository.findById(eq(productId))).willReturn(Optional.of(product));
            given(productRepository.findThumbnailImageByProductId(eq(productId)))
                    .willReturn(Optional.empty());

            // when
            List<ProductAvailabilityResponse> result =
                    productSearchService.checkAvailability(List.of(request));

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getThumbnailKeySnapshot()).isNull();
        }

        @Test
        void 존재하지_않는_상품의_재고를_확인하면_예외가_발생한다() {
            // given
            UUID nonExistentId = UUID.randomUUID();
            ProductCheckRequest request = new ProductCheckRequest(nonExistentId, 10);
            given(productRepository.findById(eq(nonExistentId))).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> productSearchService.checkAvailability(List.of(request)))
                    .isInstanceOf(ProductNotFoundException.class);
        }

        @Test
        void 빈_요청_목록이면_빈_결과를_반환한다() {
            // given
            List<ProductCheckRequest> emptyRequests = Collections.emptyList();

            // when
            List<ProductAvailabilityResponse> result =
                    productSearchService.checkAvailability(emptyRequests);

            // then
            assertThat(result).isEmpty();
            verify(productRepository, never()).findById(any());
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
                category
        );
    }

    private Category createCategory(String name) {
        return Category.createRoot(name, "테스트 카테고리", 1);
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
