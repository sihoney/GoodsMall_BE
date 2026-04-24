CREATE EXTENSION IF NOT EXISTS vector SCHEMA public;

DROP INDEX IF EXISTS ai.idx_ai_product_embedding_vector_cosine;

DO
$$
DECLARE
    embedding_type text;
BEGIN
    SELECT format_type(attribute.atttypid, attribute.atttypmod)
      INTO embedding_type
      FROM pg_attribute attribute
      JOIN pg_class class ON class.oid = attribute.attrelid
      JOIN pg_namespace namespace ON namespace.oid = class.relnamespace
     WHERE namespace.nspname = 'ai'
       AND class.relname = 'product_embedding'
       AND attribute.attname = 'embedding'
       AND attribute.attnum > 0
       AND NOT attribute.attisdropped;

    IF embedding_type IS DISTINCT FROM 'vector(1536)' THEN
        ALTER TABLE ai.product_embedding
            ALTER COLUMN embedding TYPE vector(1536)
            USING embedding::vector(1536);
    END IF;
END
$$;

CREATE INDEX IF NOT EXISTS idx_ai_product_embedding_vector_cosine
    ON ai.product_embedding
    USING ivfflat (embedding vector_cosine_ops)
    WITH (lists = 100);