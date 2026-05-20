package com.example.ai.infrastructure.client;

import com.example.ai.common.exception.AiEmbeddingException;
import com.example.ai.domain.service.EmbeddingGenerator;
import com.example.ai.infrastructure.client.dto.request.OpenAiEmbeddingRequest;
import com.example.ai.infrastructure.client.dto.response.OpenAiEmbeddingResponse;
import com.example.ai.infrastructure.config.OpenAiEmbeddingProperties;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Slf4j
@Component
public class OpenAiEmbeddingClient implements EmbeddingGenerator {

    private static final String DEFAULT_OPENAI_BASE_URL = "https://api.openai.com";

    private final RestClient restClient;
    private final OpenAiEmbeddingProperties properties;

    public OpenAiEmbeddingClient(RestClient.Builder restClientBuilder, OpenAiEmbeddingProperties properties) {
        this.properties = properties;
        String baseUrl = isBlank(properties.openaiBaseUrl()) ? DEFAULT_OPENAI_BASE_URL : properties.openaiBaseUrl();
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
    }

    @Override
    public List<Float> generate(String text) {
        if (text == null || text.isBlank()) {
            throw new AiEmbeddingException("임베딩 입력 텍스트가 비어 있습니다.");
        }
        validateConfiguration();

        OpenAiEmbeddingRequest request = new OpenAiEmbeddingRequest(properties.model(), text);
        try {
            OpenAiEmbeddingResponse response = restClient.post()
                    .uri("/v1/embeddings")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.openaiApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(OpenAiEmbeddingResponse.class);

            return extractEmbedding(response);
        } catch (RestClientResponseException e) {
            throw new AiEmbeddingException(
                    "OpenAI 임베딩 호출 실패: status=%s body=%s".formatted(e.getStatusCode(), e.getResponseBodyAsString()),
                    e
            );
        } catch (RestClientException e) {
            throw new AiEmbeddingException("OpenAI 임베딩 호출 중 네트워크 오류가 발생했습니다.", e);
        }
    }

    private List<Float> extractEmbedding(OpenAiEmbeddingResponse response) {
        if (response == null || response.data() == null || response.data().isEmpty()) {
            throw new AiEmbeddingException("OpenAI 임베딩 응답이 비어 있습니다.");
        }
        OpenAiEmbeddingResponse.EmbeddingData first = response.data().getFirst();
        if (first == null || first.embedding() == null || first.embedding().isEmpty()) {
            throw new AiEmbeddingException("OpenAI 임베딩 벡터 데이터가 비어 있습니다.");
        }
        return first.embedding();
    }

    private void validateConfiguration() {
        if (isBlank(properties.model())) {
            throw new AiEmbeddingException("ai.embedding.model 설정이 필요합니다.");
        }
        if (isBlank(properties.openaiApiKey())) {
            throw new AiEmbeddingException("ai.embedding.openai-api-key 설정이 필요합니다.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
