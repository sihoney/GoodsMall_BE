-- 카테고리 테이블
CREATE TABLE IF NOT EXISTS product.category
(
    category_id UUID PRIMARY KEY,
    parent_id   UUID                 DEFAULT NULL,
    seller_id   UUID                 DEFAULT NULL,
    name        VARCHAR(50) NOT NULL,
    description VARCHAR(500),
    depth       INTEGER     NOT NULL,
    sort_order  INTEGER     NOT NULL DEFAULT 0,
    created_at  TIMESTAMP   NOT NULL,
    updated_at  TIMESTAMP   NOT NULL,
    deleted_at  TIMESTAMP            DEFAULT NULL,

    FOREIGN KEY (parent_id) REFERENCES product.category (category_id)
);

CREATE INDEX IF NOT EXISTS idx_category_parent_id ON product.category (parent_id);
CREATE INDEX IF NOT EXISTS idx_category_seller_id ON product.category (seller_id);
CREATE INDEX IF NOT EXISTS idx_category_deleted_at ON product.category (deleted_at);
CREATE INDEX IF NOT EXISTS idx_category_depth ON product.category (depth);

-- 상품 테이블
CREATE TABLE IF NOT EXISTS product.product
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

    FOREIGN KEY (category_id) REFERENCES product.category (category_id)
);

CREATE INDEX IF NOT EXISTS idx_product_seller_id ON product.product (seller_id);
CREATE INDEX IF NOT EXISTS idx_product_status ON product.product (status);
CREATE INDEX IF NOT EXISTS idx_product_created_at ON product.product (created_at DESC);

-- 상품 이미지 테이블
CREATE TABLE IF NOT EXISTS product.product_image
(
    image_id     UUID PRIMARY KEY,
    product_id   UUID         NOT NULL,
    s3_key       VARCHAR(500) NOT NULL,
    sort_order   INTEGER      NOT NULL,
    is_thumbnail BOOLEAN      NOT NULL DEFAULT false,
    created_at   TIMESTAMP    NOT NULL,

    FOREIGN KEY (product_id) REFERENCES product.product (product_id)
);

CREATE INDEX IF NOT EXISTS idx_product_image_product_id ON product.product_image (product_id);
CREATE INDEX IF NOT EXISTS idx_product_image_is_thumbnail ON product.product_image (is_thumbnail);
