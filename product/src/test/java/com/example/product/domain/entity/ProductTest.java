package com.example.product.domain.entity;

import com.example.product.common.exception.ProductAlreadyDeletedException;
import com.example.product.common.exception.SellerNotAuthorizedException;
import com.example.product.domain.enumtype.ProductStatus;
import com.example.product.domain.enumtype.ProductType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Product Entity 도메인 로직 테스트
 * - 상품 생성, 정보 수정, 재고 관리, 상태 변경, 삭제/복구 등의 비즈니스 로직을 검증
 */
@DisplayName("Product 엔티티")
class ProductTest {

    private static final String SELLER_ID = "6ba7b810-9dad-11d1-80b4-00c04fd430c8";
    private static final String OTHER_SELLER_ID = "6ba7b810-9dad-11d1-80b4-00c04fd430c9";

    @Nested
    @DisplayName("상품 생성")
    class 상품_생성 {

        @Test
        void 정상적으로_상품을_생성한다() {
            // given
            Category category = createCategory("한식");
            String title = "김치찌개";
            String description = "매콤한 김치찌개";
            BigDecimal price = new BigDecimal("8000");
            Integer stock = 50;

            // when
            Product product = Product.create(SELLER_ID, title, description, price, stock, category, ProductType.GENERAL);

            // then
            assertThat(product).isNotNull();
            assertThat(product.getProductId()).isNotNull();
            assertThat(product.getSellerId()).isEqualTo(UUID.fromString(SELLER_ID));
            assertThat(product.getTitle()).isEqualTo(title);
            assertThat(product.getDescription()).isEqualTo(description);
            assertThat(product.getPrice()).isEqualTo(price);
            assertThat(product.getStockQuantity()).isEqualTo(stock);
            assertThat(product.getCategory()).isEqualTo(category);
            assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
            assertThat(product.getViewCount()).isZero();
            assertThat(product.getCreatedAt()).isNotNull();
            assertThat(product.getUpdatedAt()).isNotNull();
            assertThat(product.getDeletedAt()).isNull();
        }

        @Test
        void 제목이_null이면_예외가_발생한다() {
            // given
            Category category = createCategory("한식");

            // when & then
            assertThatThrownBy(() -> Product.create(
                    SELLER_ID, null, "설명", new BigDecimal("8000"), 50, category, ProductType.GENERAL
            )).isInstanceOf(NullPointerException.class);
        }

        @Test
        void 가격이_null이면_예외가_발생한다() {
            // given
            Category category = createCategory("한식");

            // when & then
            assertThatThrownBy(() -> Product.create(
                    SELLER_ID, "김치찌개", "설명", null, 50, category, ProductType.GENERAL
            )).isInstanceOf(NullPointerException.class);
        }

        @Test
        void 카테고리가_null이면_예외가_발생한다() {
            // when & then
            assertThatThrownBy(() -> Product.create(
                    SELLER_ID, "김치찌개", "설명", new BigDecimal("8000"), 50, null, ProductType.GENERAL
            )).isInstanceOf(NullPointerException.class);
        }

        @Test
        void 재고가_0이어도_정상적으로_생성된다() {
            // given
            Category category = createCategory("한식");

            // when
            Product product = Product.create(
                    SELLER_ID, "김치찌개", "매콤한 김치찌개", new BigDecimal("8000"), 0, category, ProductType.GENERAL
            );

            // then
            assertThat(product.getStockQuantity()).isZero();
            assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("판매자 검증")
    class 판매자_검증 {

        @Test
        void 판매자_ID가_일치하면_검증을_통과한다() {
            // given
            Product product = createProduct(SELLER_ID);
            UUID requestSellerId = UUID.fromString(SELLER_ID);

            // when & then
            assertThatCode(() -> product.validateSeller(requestSellerId))
                    .doesNotThrowAnyException();
        }

        @Test
        void 판매자_ID가_다르면_예외가_발생한다() {
            // given
            Product product = createProduct(SELLER_ID);
            UUID otherSellerId = UUID.fromString(OTHER_SELLER_ID);

            // when & then
            assertThatThrownBy(() -> product.validateSeller(otherSellerId))
                    .isInstanceOf(SellerNotAuthorizedException.class);
        }
    }

    @Nested
    @DisplayName("상품 정보 수정")
    class 상품_정보_수정 {

        @Test
        void 상품_정보를_정상적으로_수정한다() {
            // given
            Product product = createProduct(SELLER_ID);
            String newTitle = "된장찌개";
            String newDescription = "구수한 된장찌개";
            BigDecimal newPrice = new BigDecimal("7500");

            // when
            product.updateProductInfo(newTitle, newDescription, newPrice);

            // then
            assertThat(product.getTitle()).isEqualTo(newTitle);
            assertThat(product.getDescription()).isEqualTo(newDescription);
            assertThat(product.getPrice()).isEqualTo(newPrice);
        }

        @Test
        void 상품명이_255자를_초과하면_예외가_발생한다() {
            // given
            Product product = createProduct(SELLER_ID);
            String longTitle = "a".repeat(256);

            // when & then
            assertThatThrownBy(() ->
                    product.updateProductInfo(longTitle, "설명", new BigDecimal("8000"))
            ).isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("상품명은 255자를 초과할 수 없습니다");
        }

        @Test
        void 상품명이_정확히_255자면_수정된다() {
            // given
            Product product = createProduct(SELLER_ID);
            String title255 = "a".repeat(255);

            // when
            product.updateProductInfo(title255, "설명", new BigDecimal("8000"));

            // then
            assertThat(product.getTitle()).hasSize(255);
        }

        @Test
        void 설명은_null이어도_수정된다() {
            // given
            Product product = createProduct(SELLER_ID);

            // when
            product.updateProductInfo("김치찌개", null, new BigDecimal("8000"));

            // then
            assertThat(product.getDescription()).isNull();
        }
    }

    @Nested
    @DisplayName("카테고리 변경")
    class 카테고리_변경 {

        @Test
        void 카테고리를_정상적으로_변경한다() {
            // given
            Product product = createProduct(SELLER_ID);
            Category newCategory = createCategory("중식");

            // when
            product.updateCategory(newCategory);

            // then
            assertThat(product.getCategory()).isEqualTo(newCategory);
        }

        @Test
        void 카테고리가_null이면_예외가_발생한다() {
            // given
            Product product = createProduct(SELLER_ID);

            // when & then
            assertThatThrownBy(() -> product.updateCategory(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("카테고리는 필수입니다");
        }
    }

    @Nested
    @DisplayName("재고 수정")
    class 재고_수정 {

        @Test
        void 재고를_정상적으로_수정한다() {
            // given
            Product product = createProduct(SELLER_ID);

            // when
            product.updateStock(100);

            // then
            assertThat(product.getStockQuantity()).isEqualTo(100);
        }

        @Test
        void 재고가_0이_되면_품절_상태로_자동_전환된다() {
            // given
            Product product = createProduct(SELLER_ID);
            assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);

            // when
            product.updateStock(0);

            // then
            assertThat(product.getStockQuantity()).isZero();
            assertThat(product.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);
        }

        @Test
        void 품절_상태에서_재고가_증가하면_활성_상태로_자동_전환된다() {
            // given
            Product product = createProduct(SELLER_ID);
            product.updateStock(0);
            assertThat(product.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);

            // when
            product.updateStock(10);

            // then
            assertThat(product.getStockQuantity()).isEqualTo(10);
            assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        }

        @Test
        void 재고가_0에서_0으로_변경되어도_품절_상태를_유지한다() {
            // given
            Product product = createProduct(SELLER_ID);
            product.updateStock(0);

            // when
            product.updateStock(0);

            // then
            assertThat(product.getStockQuantity()).isZero();
            assertThat(product.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);
        }
    }

    @Nested
    @DisplayName("재고 증가")
    class 재고_증가 {

        @Test
        void 재고를_정상적으로_증가시킨다() {
            // given
            Product product = createProduct(SELLER_ID);
            int initialStock = product.getStockQuantity();

            // when
            product.increaseStock(10);

            // then
            assertThat(product.getStockQuantity()).isEqualTo(initialStock + 10);
        }

        @Test
        void 품절_상태에서_재고_증가시_활성_상태로_전환된다() {
            // given
            Product product = createProduct(SELLER_ID);
            product.updateStock(0);
            assertThat(product.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);

            // when
            product.increaseStock(5);

            // then
            assertThat(product.getStockQuantity()).isEqualTo(5);
            assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        }

        @Test
        void 재고_0에서_1_증가하면_품절에서_활성으로_전환된다() {
            // given
            Product product = createProduct(SELLER_ID);
            product.updateStock(0);

            // when
            product.increaseStock(1);

            // then
            assertThat(product.getStockQuantity()).isEqualTo(1);
            assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("재고 감소")
    class 재고_감소 {

        @Test
        void 재고를_정상적으로_감소시킨다() {
            // given
            Product product = createProduct(SELLER_ID);
            int initialStock = product.getStockQuantity();

            // when
            product.decreaseStock(10);

            // then
            assertThat(product.getStockQuantity()).isEqualTo(initialStock - 10);
        }

        @Test
        void 재고가_정확히_0이_되면_품절_상태로_전환된다() {
            // given
            Product product = createProduct(SELLER_ID);
            int currentStock = product.getStockQuantity();

            // when
            product.decreaseStock(currentStock);

            // then
            assertThat(product.getStockQuantity()).isZero();
            assertThat(product.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);
        }

        @Test
        void 재고보다_많은_수량을_감소시키면_예외가_발생한다() {
            // given
            Product product = createProduct(SELLER_ID);
            int currentStock = product.getStockQuantity();

            // when & then
            assertThatThrownBy(() -> product.decreaseStock(currentStock + 1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("재고가 부족합니다");
        }

        @Test
        void 재고가_0일_때_감소시키면_예외가_발생한다() {
            // given
            Product product = createProduct(SELLER_ID);
            product.updateStock(0);

            // when & then
            assertThatThrownBy(() -> product.decreaseStock(1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("재고가 부족합니다");
        }
    }

    @Nested
    @DisplayName("상태 변경")
    class 상태_변경 {

        @Test
        void 상태를_INACTIVE로_정상적으로_변경한다() {
            // given
            Product product = createProduct(SELLER_ID);

            // when
            product.updateStatus(ProductStatus.INACTIVE);

            // then
            assertThat(product.getStatus()).isEqualTo(ProductStatus.INACTIVE);
        }

        @Test
        void 상태가_null이면_예외가_발생한다() {
            // given
            Product product = createProduct(SELLER_ID);

            // when & then
            assertThatThrownBy(() -> product.updateStatus(null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessage("상태는 필수입니다");
        }

        @Test
        void 재고가_남아있는데_품절로_변경하면_예외가_발생한다() {
            // given
            Product product = createProduct(SELLER_ID);
            assertThat(product.getStockQuantity()).isGreaterThan(0);

            // when & then
            assertThatThrownBy(() -> product.updateStatus(ProductStatus.SOLD_OUT))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("재고가 남아있는 상품은 품절 상태로 변경할 수 없습니다");
        }

        @Test
        void 재고가_0일_때는_품절로_변경할_수_있다() {
            // given
            Product product = createProduct(SELLER_ID);
            product.updateStock(0);

            // when
            product.updateStatus(ProductStatus.SOLD_OUT);

            // then
            assertThat(product.getStatus()).isEqualTo(ProductStatus.SOLD_OUT);
        }
    }

    @Nested
    @DisplayName("상품 삭제")
    class 상품_삭제 {

        @Test
        void 상품을_정상적으로_삭제한다() {
            // given
            Product product = createProduct(SELLER_ID);
            assertThat(product.getDeletedAt()).isNull();

            // when
            product.delete();

            // then
            assertThat(product.getDeletedAt()).isNotNull();
            assertThat(product.getStatus()).isEqualTo(ProductStatus.INACTIVE);
            assertThat(product.isDeleted()).isTrue();
        }

        @Test
        void 이미_삭제된_상품을_다시_삭제하면_예외가_발생한다() {
            // given
            Product product = createProduct(SELLER_ID);
            product.delete();

            // when & then
            assertThatThrownBy(() -> product.delete())
                    .isInstanceOf(ProductAlreadyDeletedException.class);
        }
    }

    @Nested
    @DisplayName("상품 복구")
    class 상품_복구 {

        @Test
        void 삭제된_상품을_정상적으로_복구한다() {
            // given
            Product product = createProduct(SELLER_ID);
            product.delete();
            assertThat(product.isDeleted()).isTrue();

            // when
            product.restore();

            // then
            assertThat(product.getDeletedAt()).isNull();
            assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
            assertThat(product.isDeleted()).isFalse();
        }

        @Test
        void 삭제되지_않은_상품을_복구하면_예외가_발생한다() {
            // given
            Product product = createProduct(SELLER_ID);
            assertThat(product.isDeleted()).isFalse();

            // when & then
            assertThatThrownBy(() -> product.restore())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("삭제되지 않은 상품은 복구할 수 없습니다");
        }
    }

    @Nested
    @DisplayName("조회수 증가")
    class 조회수_증가 {

        @Test
        void 조회수가_1씩_증가한다() {
            // given
            Product product = createProduct(SELLER_ID);
            int initialViewCount = product.getViewCount();

            // when
            product.increaseViewCount();

            // then
            assertThat(product.getViewCount()).isEqualTo(initialViewCount + 1);
        }

        @Test
        void 조회수를_여러번_증가시킨다() {
            // given
            Product product = createProduct(SELLER_ID);
            int initialViewCount = product.getViewCount();

            // when
            product.increaseViewCount();
            product.increaseViewCount();
            product.increaseViewCount();

            // then
            assertThat(product.getViewCount()).isEqualTo(initialViewCount + 3);
        }
    }

    @Nested
    @DisplayName("상품 상태 확인")
    class 상품_상태_확인 {

        @Test
        void 삭제되지_않은_상품은_isDeleted가_false이다() {
            // given
            Product product = createProduct(SELLER_ID);

            // when & then
            assertThat(product.isDeleted()).isFalse();
        }

        @Test
        void 삭제된_상품은_isDeleted가_true이다() {
            // given
            Product product = createProduct(SELLER_ID);
            product.delete();

            // when & then
            assertThat(product.isDeleted()).isTrue();
        }

        @Test
        void ACTIVE_상태이고_삭제되지_않은_상품은_isActive가_true이다() {
            // given
            Product product = createProduct(SELLER_ID);

            // when & then
            assertThat(product.isActive()).isTrue();
        }

        @Test
        void 삭제된_상품은_isActive가_false이다() {
            // given
            Product product = createProduct(SELLER_ID);
            product.delete();

            // when & then
            assertThat(product.isActive()).isFalse();
        }

        @Test
        void INACTIVE_상태_상품은_isActive가_false이다() {
            // given
            Product product = createProduct(SELLER_ID);
            product.updateStatus(ProductStatus.INACTIVE);

            // when & then
            assertThat(product.isActive()).isFalse();
        }

        @Test
        void SOLD_OUT_상태_상품은_isActive가_false이다() {
            // given
            Product product = createProduct(SELLER_ID);
            product.updateStock(0);

            // when & then
            assertThat(product.isActive()).isFalse();
        }
    }

    // 테스트 헬퍼 메소드
    private Product createProduct(String sellerId) {
        Category category = createCategory("한식");
        return Product.create(
                sellerId,
                "김치찌개",
                "매콤한 김치찌개",
                new BigDecimal("8000"),
                50,
                category,
                ProductType.GENERAL
        );
    }

    private Category createCategory(String name) {
        return Category.createRoot(name, "테스트 카테고리", 1);
    }
}
