create database cart_db;
-- Cart 테이블 생성
CREATE TABLE IF NOT EXISTS cart (
    id UUID PRIMARY KEY NOT NULL,
    member_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- Cart 인덱스
CREATE INDEX IF NOT EXISTS idx_cart_member_id ON cart(member_id);
CREATE INDEX IF NOT EXISTS idx_cart_created_at ON cart(created_at DESC);

-- Cart 주석
COMMENT ON TABLE cart IS '장바구니';
COMMENT ON COLUMN cart.id IS '장바구니 ID (UUID)';
COMMENT ON COLUMN cart.member_id IS '회원 ID (UUID)';
COMMENT ON COLUMN cart.created_at IS '생성 시간';
COMMENT ON COLUMN cart.updated_at IS '수정 시간';

-- CartItem 테이블 생성
CREATE TABLE IF NOT EXISTS cart_item (
    id UUID PRIMARY KEY NOT NULL,
    cart_id UUID NOT NULL,
    product_id UUID NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity >= 1),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    FOREIGN KEY (cart_id) REFERENCES cart(id) ON DELETE CASCADE
);

-- CartItem 인덱스
CREATE INDEX IF NOT EXISTS idx_cart_item_cart_id ON cart_item(cart_id);
CREATE INDEX IF NOT EXISTS idx_cart_item_product_id ON cart_item(product_id);
CREATE INDEX IF NOT EXISTS idx_cart_item_cart_product ON cart_item(cart_id, product_id);
CREATE INDEX IF NOT EXISTS idx_cart_item_created_at ON cart_item(created_at DESC);

-- CartItem 주석
COMMENT ON TABLE cart_item IS '장바구니 항목';
COMMENT ON COLUMN cart_item.id IS '장바구니 항목 ID (UUID)';
COMMENT ON COLUMN cart_item.cart_id IS '장바구니 ID (UUID)';
COMMENT ON COLUMN cart_item.product_id IS '상품 ID (UUID)';
COMMENT ON COLUMN cart_item.quantity IS '상품 수량';
COMMENT ON COLUMN cart_item.created_at IS '생성 시간';
COMMENT ON COLUMN cart_item.updated_at IS '수정 시간';

-- Wish 테이블 생성
CREATE TABLE IF NOT EXISTS wish (
    id UUID PRIMARY KEY NOT NULL,
    member_id UUID NOT NULL,
    product_id UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_wish_member_product UNIQUE (member_id, product_id)
);

-- Wish 인덱스
CREATE INDEX IF NOT EXISTS idx_wish_member_id ON wish(member_id);
CREATE INDEX IF NOT EXISTS idx_wish_product_id ON wish(product_id);
CREATE INDEX IF NOT EXISTS idx_wish_member_product ON wish(member_id, product_id);
CREATE INDEX IF NOT EXISTS idx_wish_created_at ON wish(created_at DESC);

-- Wish 주석
COMMENT ON TABLE wish IS '찜 목록';
COMMENT ON COLUMN wish.id IS '찜 ID (UUID)';
COMMENT ON COLUMN wish.member_id IS '회원 ID (UUID)';
COMMENT ON COLUMN wish.product_id IS '상품 ID (UUID)';
COMMENT ON COLUMN wish.created_at IS '생성 시간';
