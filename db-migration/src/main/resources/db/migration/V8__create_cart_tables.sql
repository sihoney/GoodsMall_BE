-- 장바구니 테이블
CREATE TABLE IF NOT EXISTS cart.cart
(
    cart_id    UUID PRIMARY KEY NOT NULL,
    member_id  UUID             NOT NULL,
    product_id UUID             NOT NULL,
    quantity   INTEGER          NOT NULL CHECK (quantity >= 1),
    added_at   TIMESTAMP        NOT NULL,
    CONSTRAINT uk_cart_member_product UNIQUE (member_id, product_id)
);

CREATE INDEX IF NOT EXISTS idx_cart_member_id ON cart.cart (member_id);
CREATE INDEX IF NOT EXISTS idx_cart_product_id ON cart.cart (product_id);
CREATE INDEX IF NOT EXISTS idx_cart_member_product ON cart.cart (member_id, product_id);
CREATE INDEX IF NOT EXISTS idx_cart_added_at ON cart.cart (added_at DESC);

COMMENT ON TABLE cart.cart IS '장바구니';
COMMENT ON COLUMN cart.cart.cart_id IS '장바구니 항목 ID (UUID)';
COMMENT ON COLUMN cart.cart.member_id IS '회원 ID (UUID)';
COMMENT ON COLUMN cart.cart.product_id IS '상품 ID (UUID)';
COMMENT ON COLUMN cart.cart.quantity IS '상품 수량';
COMMENT ON COLUMN cart.cart.added_at IS '장바구니 추가 시간';

-- 찜 목록 테이블
CREATE TABLE IF NOT EXISTS cart.wish
(
    id         UUID PRIMARY KEY NOT NULL,
    member_id  UUID             NOT NULL,
    product_id UUID             NOT NULL,
    created_at TIMESTAMP        NOT NULL,
    CONSTRAINT uk_wish_member_product UNIQUE (member_id, product_id)
);

CREATE INDEX IF NOT EXISTS idx_wish_member_id ON cart.wish (member_id);
CREATE INDEX IF NOT EXISTS idx_wish_product_id ON cart.wish (product_id);
CREATE INDEX IF NOT EXISTS idx_wish_member_product ON cart.wish (member_id, product_id);
CREATE INDEX IF NOT EXISTS idx_wish_created_at ON cart.wish (created_at DESC);

COMMENT ON TABLE cart.wish IS '찜 목록';
COMMENT ON COLUMN cart.wish.id IS '찜 ID (UUID)';
COMMENT ON COLUMN cart.wish.member_id IS '회원 ID (UUID)';
COMMENT ON COLUMN cart.wish.product_id IS '상품 ID (UUID)';
COMMENT ON COLUMN cart.wish.created_at IS '생성 시간';
