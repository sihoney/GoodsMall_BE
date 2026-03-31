create database cart_db;

-- Cart 테이블 생성
CREATE TABLE IF NOT EXISTS cart
(
    cart_item_id UUID PRIMARY KEY NOT NULL,
    member_id    UUID             NOT NULL,
    product_id   UUID             NOT NULL,
    quantity     INTEGER          NOT NULL CHECK (quantity >= 1),
    added_at     TIMESTAMP        NOT NULL,
    CONSTRAINT uk_cart_member_product UNIQUE (member_id, product_id)
);

-- Cart 인덱스
CREATE INDEX IF NOT EXISTS idx_cart_member_id ON cart (member_id);
CREATE INDEX IF NOT EXISTS idx_cart_product_id ON cart (product_id);
CREATE INDEX IF NOT EXISTS idx_cart_member_product ON cart (member_id, product_id);
CREATE INDEX IF NOT EXISTS idx_cart_added_at ON cart (added_at DESC);

-- Cart 주석
COMMENT ON TABLE cart IS '장바구니';
COMMENT ON COLUMN cart.cart_item_id IS '장바구니 항목 ID (UUID)';
COMMENT ON COLUMN cart.member_id IS '회원 ID (UUID)';
COMMENT ON COLUMN cart.product_id IS '상품 ID (UUID)';
COMMENT ON COLUMN cart.quantity IS '상품 수량';
COMMENT ON COLUMN cart.added_at IS '장바구니 추가 시간';

-- Wish 테이블 생성
CREATE TABLE IF NOT EXISTS wish
(
    id         UUID PRIMARY KEY NOT NULL,
    member_id  UUID             NOT NULL,
    product_id UUID             NOT NULL,
    created_at TIMESTAMP        NOT NULL,
    CONSTRAINT uk_wish_member_product UNIQUE (member_id, product_id)
);

-- Wish 인덱스
CREATE INDEX IF NOT EXISTS idx_wish_member_id ON wish (member_id);
CREATE INDEX IF NOT EXISTS idx_wish_product_id ON wish (product_id);
CREATE INDEX IF NOT EXISTS idx_wish_member_product ON wish (member_id, product_id);
CREATE INDEX IF NOT EXISTS idx_wish_created_at ON wish (created_at DESC);

-- Wish 주석
COMMENT ON TABLE wish IS '찜 목록';
COMMENT ON COLUMN wish.id IS '찜 ID (UUID)';
COMMENT ON COLUMN wish.member_id IS '회원 ID (UUID)';
COMMENT ON COLUMN wish.product_id IS '상품 ID (UUID)';
COMMENT ON COLUMN wish.created_at IS '생성 시간';
