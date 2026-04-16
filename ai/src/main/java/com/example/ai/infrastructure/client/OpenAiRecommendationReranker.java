package com.example.ai.infrastructure.client;

import com.example.ai.application.dto.RecommendedProductResult;
import com.example.ai.common.exception.AiEmbeddingException;
import com.example.ai.domain.service.RecommendationReranker;
import com.example.ai.infrastructure.client.dto.request.OpenAiChatCompletionRequest;
import com.example.ai.infrastructure.client.dto.request.OpenAiChatCompletionRequest.Message;
import com.example.ai.infrastructure.client.dto.request.OpenAiChatCompletionRequest.ResponseFormat;
import com.example.ai.infrastructure.client.dto.response.OpenAiChatCompletionResponse;
import com.example.ai.infrastructure.config.RecommendationRerankProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
public class OpenAiRecommendationReranker implements RecommendationReranker {

    private static final String DEFAULT_OPENAI_BASE_URL = "https://api.openai.com";
    private static final String DEFAULT_RERANK_MODEL = "gpt-5.4-nano";

    private final RestClient restClient;
    private final RecommendationRerankProperties properties;
    private final ObjectMapper objectMapper;

    public OpenAiRecommendationReranker(
            RestClient.Builder restClientBuilder,
            RecommendationRerankProperties properties,
            ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        String baseUrl = isBlank(properties.openaiBaseUrl()) ? DEFAULT_OPENAI_BASE_URL : properties.openaiBaseUrl();
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public List<UUID> rerank(UUID baseProductId, List<RecommendedProductResult> candidates, int selectCount) {
        if (!properties.enabled()) {
            return List.of();
        }
        if (isBlank(properties.openaiApiKey())) {
            throw new AiEmbeddingException("ai.recommendation.rerank.openai-api-key 설정이 필요합니다.");
        }
        if (baseProductId == null) {
            throw new AiEmbeddingException("baseProductId는 필수입니다.");
        }
        if (candidates == null || candidates.isEmpty() || selectCount <= 0) {
            return List.of();
        }

        OpenAiChatCompletionRequest request = buildRequest(baseProductId, candidates, selectCount);
        String content = requestRerank(request);
        return parseSelectedProductIds(content, candidates);
    }

    private OpenAiChatCompletionRequest buildRequest(
            UUID baseProductId,
            List<RecommendedProductResult> candidates,
            int selectCount
    ) {
        String model = isBlank(properties.model()) ? DEFAULT_RERANK_MODEL : properties.model();
        return new OpenAiChatCompletionRequest(
                model,
                List.of(
                        new Message("system", buildSystemPrompt()),
                        new Message("user", buildUserPrompt(baseProductId, candidates, selectCount))
                ),
                properties.temperature(),
                new ResponseFormat("json_object")
        );
    }

    private String requestRerank(OpenAiChatCompletionRequest request) {
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
        } catch (RestClientResponseException e) {
            throw new AiEmbeddingException(
                    "OpenAI 추천 재정렬 호출 실패: status=%s body=%s".formatted(e.getStatusCode(), e.getResponseBodyAsString()),
                    e
            );
        } catch (RestClientException e) {
            throw new AiEmbeddingException("OpenAI 추천 재정렬 호출 중 네트워크 오류가 발생했습니다.", e);
        }
    }

    private String extractContent(OpenAiChatCompletionResponse response) {
        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new AiEmbeddingException("OpenAI 추천 재정렬 응답이 비어 있습니다.");
        }

        OpenAiChatCompletionResponse.Choice choice = response.choices().getFirst();
        if (choice == null || choice.message() == null || isBlank(choice.message().content())) {
            throw new AiEmbeddingException("OpenAI 추천 재정렬 응답 content가 비어 있습니다.");
        }
        return choice.message().content();
    }

    private List<UUID> parseSelectedProductIds(String content, List<RecommendedProductResult> candidates) {
        Set<UUID> candidateSet = new LinkedHashSet<>();
        for (RecommendedProductResult candidate : candidates) {
            candidateSet.add(candidate.productId());
        }

        try {
            JsonNode root = objectMapper.readTree(normalizeJsonContent(content));
            JsonNode idsNode = root.path("selectedProductIds");
            if (!idsNode.isArray()) {
                throw new AiEmbeddingException("selectedProductIds 배열이 없습니다.");
            }

            List<UUID> result = new ArrayList<>();
            for (JsonNode node : idsNode) {
                if (!node.isTextual()) {
                    continue;
                }
                UUID id = UUID.fromString(node.asText());
                if (candidateSet.contains(id) && !result.contains(id)) {
                    result.add(id);
                }
            }
            return result;
        } catch (Exception e) {
            throw new AiEmbeddingException("OpenAI 추천 재정렬 응답 파싱에 실패했습니다.", e);
        }
    }

    private String buildSystemPrompt() {
        return """
                너는 상품 연관 추천 재정렬기다.
                입력 후보 목록에서만 선택해 selectedProductIds를 반환한다.
                응답은 반드시 JSON 객체 하나만 반환한다.
                형식:
                {"selectedProductIds":["uuid1","uuid2","uuid3","uuid4","uuid5"]}
                selectedProductIds 외 다른 필드는 반환하지 않는다.
                """;
    }

    private String buildUserPrompt(UUID baseProductId, List<RecommendedProductResult> candidates, int selectCount) {
        StringBuilder sb = new StringBuilder();
        sb.append("baseProductId=").append(baseProductId).append('\n');
        sb.append("selectCount=").append(selectCount).append('\n');
        sb.append("candidates:\n");

        for (int i = 0; i < candidates.size(); i++) {
            RecommendedProductResult candidate = candidates.get(i);
            sb.append(i + 1)
                    .append(". productId=")
                    .append(candidate.productId())
                    .append(", similarityScore=")
                    .append(candidate.similarityScore())
                    .append('\n');
        }

        sb.append("유사도와 연관성을 기준으로 상위 ").append(selectCount).append("개를 선택해라.");
        return sb.toString();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
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
}
