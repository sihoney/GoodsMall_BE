CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE ai.product_embedding
(
    embedding_id       UUID PRIMARY KEY,
    product_id         UUID         NOT NULL UNIQUE,
    embedding          vector(1536) NOT NULL,
    source_updated_at  TIMESTAMP    NOT NULL,
    is_active          BOOLEAN      NOT NULL DEFAULT true,
    created_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ai_product_embedding_active
    ON ai.product_embedding (is_active);

CREATE INDEX idx_ai_product_embedding_source_updated_at
    ON ai.product_embedding (source_updated_at DESC);

CREATE INDEX idx_ai_product_embedding_vector_cosine
    ON ai.product_embedding
    USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 100);
