package com.example.ai.infrastructure.client;

import com.example.ai.application.dto.AuctionPriceRecommendationCommand;
import com.example.ai.application.dto.AuctionPriceRecommendationResult;
import com.example.ai.common.exception.AuctionPriceRecommendationConfigurationException;
import com.example.ai.common.exception.AuctionPriceRecommendationExternalCallException;
import com.example.ai.common.exception.AuctionPriceRecommendationResponseInvalidException;
import com.example.ai.infrastructure.client.dto.request.OpenAiChatCompletionRequest;
import com.example.ai.infrastructure.client.dto.request.OpenAiChatCompletionRequest.Message;
import com.example.ai.infrastructure.client.dto.request.OpenAiChatCompletionRequest.ResponseFormat;
import com.example.ai.infrastructure.client.dto.response.AuctionPriceRecommendationAiResponse;
import com.example.ai.infrastructure.client.dto.response.OpenAiChatCompletionResponse;
import com.example.ai.infrastructure.config.AuctionPriceRecommendationProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
public class OpenAiAuctionPriceRecommendationGenerator {

    private static final String DEFAULT_OPENAI_BASE_URL = "https://api.openai.com";
    private static final String DEFAULT_MODEL = "gpt-5.4-nano";
    private static final String GUARANTEE_WORD = "반드시";
    private static final String ABSOLUTE_WORD = "무조건";
    private static final String PRESSURE_WORD = "지금 바로";

    private final RestClient restClient;
    private final AuctionPriceRecommendationProperties properties;
    private final ObjectMapper objectMapper;

    public OpenAiAuctionPriceRecommendationGenerator(
            RestClient.Builder restClientBuilder,
            AuctionPriceRecommendationProperties properties,
            ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.objectMapper = objectMapper;

        String baseUrl = isBlank(properties.openaiBaseUrl()) ? DEFAULT_OPENAI_BASE_URL : properties.openaiBaseUrl();
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .requestFactory(buildRequestFactory())
                .build();
    }

    public AuctionPriceRecommendationResult generate(
            AuctionPriceRecommendationCommand command,
            AuctionPriceRecommendationResult fallbackResult
    ) {
        validateConfiguration();

        OpenAiChatCompletionRequest request = buildRequest(command, fallbackResult);
        String content = requestAuctionPriceRecommendation(request);
        AuctionPriceRecommendationAiResponse aiResponse = parseResponse(content);
        return mapToResult(aiResponse, fallbackResult);
    }

    private void validateConfiguration() {
        if (!properties.enabled()) {
            throw new AuctionPriceRecommendationConfigurationException(
                    "ai.auction-price-recommendation.enabled 설정이 필요합니다."
            );
        }
        if (isBlank(properties.openaiApiKey())) {
            throw new AuctionPriceRecommendationConfigurationException(
                    "ai.auction-price-recommendation.openai-api-key 설정이 필요합니다."
            );
        }
    }

    private OpenAiChatCompletionRequest buildRequest(
            AuctionPriceRecommendationCommand command,
            AuctionPriceRecommendationResult fallbackResult
    ) {
        String model = isBlank(properties.model()) ? DEFAULT_MODEL : properties.model();

        return new OpenAiChatCompletionRequest(
                model,
                java.util.List.of(
                        new Message("system", buildSystemPrompt()),
                        new Message("user", buildUserPrompt(command, fallbackResult))
                ),
                properties.temperature(),
                new ResponseFormat("json_object")
        );
    }

    private String requestAuctionPriceRecommendation(OpenAiChatCompletionRequest request) {
        try {
            OpenAiChatCompletionResponse response = restClient.post()
                    .uri("/v1/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.openaiApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(OpenAiChatCompletionResponse.class);
            return extractContent(response);
        } catch (RestClientResponseException exception) {
            throw new AuctionPriceRecommendationExternalCallException(
                    "OpenAI 경매 가격 추천 호출 실패: status=%s body=%s".formatted(
                            exception.getStatusCode(),
                            exception.getResponseBodyAsString()
                    ),
                    exception
            );
        } catch (RestClientException exception) {
            throw new AuctionPriceRecommendationExternalCallException(
                    "OpenAI 경매 가격 추천 호출 중 네트워크 오류가 발생했습니다.",
                    exception
            );
        }
    }

    private String extractContent(OpenAiChatCompletionResponse response) {
        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new AuctionPriceRecommendationResponseInvalidException("OpenAI 경매 가격 추천 응답이 비어 있습니다.");
        }

        OpenAiChatCompletionResponse.Choice choice = response.choices().getFirst();
        if (choice == null || choice.message() == null || isBlank(choice.message().content())) {
            throw new AuctionPriceRecommendationResponseInvalidException("OpenAI 경매 가격 추천 응답 content가 비어 있습니다.");
        }
        return choice.message().content();
    }

    private AuctionPriceRecommendationAiResponse parseResponse(String content) {
        try {
            AuctionPriceRecommendationAiResponse response = objectMapper.readValue(
                    normalizeJsonContent(content),
                    AuctionPriceRecommendationAiResponse.class
            );
            validateParsedResponse(response);
            return response;
        } catch (AuctionPriceRecommendationResponseInvalidException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new AuctionPriceRecommendationResponseInvalidException(
                    "OpenAI 경매 가격 추천 응답 파싱에 실패했습니다.",
                    exception
            );
        }
    }

    private void validateParsedResponse(AuctionPriceRecommendationAiResponse response) {
        if (response == null) {
            throw new AuctionPriceRecommendationResponseInvalidException("OpenAI 응답이 null입니다.");
        }
        if (response.expectedFinalPrice() == null || response.expectedFinalPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new AuctionPriceRecommendationResponseInvalidException("expectedFinalPrice가 올바르지 않습니다.");
        }
        if (response.recommendedBidPrice() == null || response.recommendedBidPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new AuctionPriceRecommendationResponseInvalidException("recommendedBidPrice가 올바르지 않습니다.");
        }
        if (response.expectedFinalPrice().compareTo(response.recommendedBidPrice()) < 0) {
            throw new AuctionPriceRecommendationResponseInvalidException("expectedFinalPrice는 recommendedBidPrice보다 작을 수 없습니다.");
        }
        if (isBlank(response.priceReason())) {
            throw new AuctionPriceRecommendationResponseInvalidException("priceReason이 비어 있습니다.");
        }
    }

    private AuctionPriceRecommendationResult mapToResult(
            AuctionPriceRecommendationAiResponse aiResponse,
            AuctionPriceRecommendationResult fallbackResult
    ) {
        String reason = sanitizeReason(aiResponse.priceReason(), fallbackResult.priceReason());
        return new AuctionPriceRecommendationResult(
                aiResponse.expectedFinalPrice(),
                aiResponse.recommendedBidPrice(),
                reason,
                fallbackResult.notes()
        );
    }

    private String buildSystemPrompt() {
        return """
                너는 경매 가격 추천 보조 AI다.
                입력 경매 정보를 보고 아래 필드를 JSON으로만 반환한다.
                - expectedFinalPrice: 예상 형성 가격(정수)
                - recommendedBidPrice: 현재 시점 추천 입찰가(정수)
                - priceReason: 추천 이유(한 문장)
                규칙:
                1) 응답은 JSON 객체 하나만 반환한다.
                2) 숫자는 0보다 큰 값으로 반환한다.
                3) expectedFinalPrice >= recommendedBidPrice 조건을 반드시 만족한다.
                4) 과장/확정/압박 표현(반드시, 무조건, 지금 바로)은 사용하지 않는다.
                """;
    }

    private String buildUserPrompt(
            AuctionPriceRecommendationCommand command,
            AuctionPriceRecommendationResult fallbackResult
    ) {
        return """
                auctionId=%s
                productId=%s
                productName=%s
                currentBidPrice=%s
                startPrice=%s
                bidCount=%s
                remainingSeconds=%s

                참고용 규칙 기반 값:
                expectedFinalPrice=%s
                recommendedBidPrice=%s
                priceReason=%s

                위 정보를 바탕으로 JSON만 반환해라.
                """.formatted(
                command.auctionId(),
                command.productId(),
                nullable(command.productName()),
                command.currentBidPrice(),
                command.startPrice(),
                nullable(command.bidCount()),
                nullable(command.remainingSeconds()),
                fallbackResult.expectedFinalPrice(),
                fallbackResult.recommendedBidPrice(),
                fallbackResult.priceReason()
        );
    }

    private String normalizeJsonContent(String content) {
        String trimmed = content == null ? "" : content.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed
                    .replaceFirst("^```json\\s*", "")
                    .replaceFirst("^```\\s*", "")
                    .replaceFirst("\\s*```$", "")
                    .trim();
        }
        return trimmed;
    }

    private SimpleClientHttpRequestFactory buildRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(sanitizeTimeout(properties.connectTimeoutMs(), 3000));
        factory.setReadTimeout(sanitizeTimeout(properties.readTimeoutMs(), 8000));
        return factory;
    }

    private int sanitizeTimeout(int timeoutMs, int defaultValue) {
        return timeoutMs > 0 ? timeoutMs : defaultValue;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String sanitizeReason(String aiReason, String fallbackReason) {
        String normalizedReason = normalize(aiReason);
        if (normalizedReason.isEmpty()) {
            return fallbackReason;
        }

        if (normalizedReason.contains(GUARANTEE_WORD)
                || normalizedReason.contains(ABSOLUTE_WORD)
                || normalizedReason.contains(PRESSURE_WORD)) {
            return fallbackReason;
        }

        return normalizedReason;
    }

    private String nullable(Object value) {
        return value == null ? "null" : value.toString();
    }
}

