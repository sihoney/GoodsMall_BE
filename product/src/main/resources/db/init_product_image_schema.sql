CREATE TABLE product_image
(
    image_id     UUID PRIMARY KEY,
    product_id   UUID         NOT NULL,
    s3_key       VARCHAR(500) NOT NULL,
    sort_order   INTEGER      NOT NULL,
    is_thumbnail BOOLEAN      NOT NULL DEFAULT false,
    created_at   TIMESTAMP    NOT NULL,

    FOREIGN KEY (product_id) REFERENCES product (product_id)
);

-- 인덱스 생성
CREATE INDEX idx_product_image_product_id ON product_image (product_id);
CREATE INDEX idx_product_image_is_thumbnail ON product_image (is_thumbnail);
