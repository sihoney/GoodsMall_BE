CREATE TABLE category
(
    category_id UUID PRIMARY KEY,
    parent_id   UUID                 DEFAULT NULL,
    seller_id   UUID                 DEFAULT NULL,  -- NULL: 관리자, UUID: 판매자
    name        VARCHAR(50) NOT NULL,
    description VARCHAR(500),                       -- 카테고리 설명
    depth       INTEGER     NOT NULL,              -- 0: 대(관리자만), 1: 중(판매자), 2: 소(판매자)
    sort_order  INTEGER     NOT NULL DEFAULT 0,
    created_at  TIMESTAMP   NOT NULL,
    updated_at  TIMESTAMP   NOT NULL,
    deleted_at  TIMESTAMP            DEFAULT NULL,

    FOREIGN KEY (parent_id) REFERENCES category (category_id)
);

CREATE INDEX idx_category_parent_id ON category (parent_id);
CREATE INDEX idx_category_seller_id ON category (seller_id);
CREATE INDEX idx_category_deleted_at ON category (deleted_at);
CREATE INDEX idx_category_depth ON category (depth);

