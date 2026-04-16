package com.example.ai.application.service;

public final class RecommendationLimitPolicy {

    public static final int DEFAULT_LIMIT = 20;
    public static final int MAX_LIMIT = 50;

    private RecommendationLimitPolicy() {
    }

    public static int normalize(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }
}
