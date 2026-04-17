package com.example.ai.domain.service;

import java.util.List;

public interface EmbeddingGenerator {

    List<Float> generate(String text);
}
