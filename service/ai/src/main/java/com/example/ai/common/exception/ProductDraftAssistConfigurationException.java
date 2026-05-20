package com.example.ai.common.exception;

public class ProductDraftAssistConfigurationException extends AiProductDraftAssistException {

    public ProductDraftAssistConfigurationException(String message) {
        super(ErrorCode.AI_PRODUCT_DRAFT_ASSIST_CONFIGURATION_ERROR, message);
    }
}
