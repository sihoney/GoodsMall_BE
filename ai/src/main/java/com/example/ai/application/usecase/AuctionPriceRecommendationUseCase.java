package com.example.ai.application.usecase;

import com.example.ai.application.dto.AuctionPriceRecommendationCommand;
import com.example.ai.application.dto.AuctionPriceRecommendationResult;

public interface AuctionPriceRecommendationUseCase {

    AuctionPriceRecommendationResult recommend(AuctionPriceRecommendationCommand command);
}

