CREATE TABLE category
(
    category_id UUID PRIMARY KEY,
    parent_id   UUID                 DEFAULT NULL,
    name        VARCHAR(50) NOT NULL,
    depth       INTEGER     NOT NULL,              -- 0: 대, 1: 중, 2: 소
    sort_order  INTEGER     NOT NULL DEFAULT 0,
    created_at  TIMESTAMP   NOT NULL,

    FOREIGN KEY (parent_id) REFERENCES category (category_id)
);

CREATE INDEX idx_category_parent_id ON category (parent_id);

