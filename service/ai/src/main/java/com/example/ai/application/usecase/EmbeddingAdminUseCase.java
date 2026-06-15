package com.example.ai.application.usecase;

import com.example.ai.application.dto.EmbeddingAdminResult;

public interface EmbeddingAdminUseCase {

    EmbeddingAdminResult backfillMissing();

    EmbeddingAdminResult reindexAll();
}

