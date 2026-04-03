CREATE TABLE product
(
    product_id     UUID PRIMARY KEY,
    seller_id      UUID                                                             NOT NULL,
    category_id    UUID                                                             NOT NULL,
    title          VARCHAR(255)                                                     NOT NULL,
    description    TEXT,
    price          DECIMAL(10, 2)                                                   NOT NULL,
    stock_quantity INTEGER                                                          NOT NULL,
    status         VARCHAR(20) CHECK (status IN ('ACTIVE', 'SOLD_OUT', 'INACTIVE')) NOT NULL,
    view_count     INTEGER   DEFAULT 0                                              NOT NULL,
    created_at     TIMESTAMP                                                        NOT NULL,
    updated_at     TIMESTAMP                                                        NOT NULL,
    deleted_at     TIMESTAMP DEFAULT NULL,

    FOREIGN KEY (category_id) REFERENCES category (category_id)
);

-- 인덱스 생성
CREATE INDEX idx_product_seller_id ON product (seller_id);
CREATE INDEX idx_product_status ON product (status);
CREATE INDEX idx_product_created_at ON product (created_at DESC);

-- 특정 판매자의 물건 조회 시 인덱스화를 진행하게 되면 조회 성능 개선
-- 최신순 정렬 시 도 위와 동일 한 이유
-- 판매 중 상품 조회 시 도 위와 동일 한 이유
