-- DRAFT ONLY
-- This file is not part of the active Flyway scan path.
-- Current active path remains classpath:db/migration.
--
-- Absorbed legacy migrations:
-- V32, V33, V48

CREATE SCHEMA IF NOT EXISTS ai;
CREATE EXTENSION IF NOT EXISTS vector SCHEMA public;

CREATE TABLE IF NOT EXISTS ai.product_embedding (
    embedding_id       UUID          NOT NULL,
    product_id         UUID          NOT NULL,
    embedding          vector(1536)  NOT NULL,
    source_updated_at  TIMESTAMP     NOT NULL,
    is_active          BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_ai_product_embedding PRIMARY KEY (embedding_id),
    CONSTRAINT uq_ai_product_embedding_product_id UNIQUE (product_id)
);

CREATE INDEX IF NOT EXISTS idx_ai_product_embedding_active
    ON ai.product_embedding (is_active);

CREATE INDEX IF NOT EXISTS idx_ai_product_embedding_source_updated_at
    ON ai.product_embedding (source_updated_at DESC);

CREATE INDEX IF NOT EXISTS idx_ai_product_embedding_vector_cosine
    ON ai.product_embedding
    USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 100);
