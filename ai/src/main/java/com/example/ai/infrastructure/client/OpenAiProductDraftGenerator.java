package com.example.ai.infrastructure.client;

import com.example.ai.application.dto.ProductDraftAssistCommand;
import com.example.ai.application.dto.ProductDraftAssistResult;
import com.example.ai.application.service.ProductDraftAssistPromptBuilder;
import com.example.ai.common.exception.ProductDraftAssistConfigurationException;
import com.example.ai.common.exception.ProductDraftAssistExternalCallException;
import com.example.ai.common.exception.ProductDraftAssistImageReadException;
import com.example.ai.common.exception.ProductDraftAssistResponseInvalidException;
import com.example.ai.domain.service.ProductDraftGenerator;
import com.example.ai.infrastructure.client.dto.request.OpenAiMultimodalChatCompletionRequest;
import com.example.ai.infrastructure.client.dto.request.OpenAiMultimodalChatCompletionRequest.ContentPart;
import com.example.ai.infrastructure.client.dto.request.OpenAiMultimodalChatCompletionRequest.Message;
import com.example.ai.infrastructure.client.dto.request.OpenAiMultimodalChatCompletionRequest.ResponseFormat;
import com.example.ai.infrastructure.client.dto.response.OpenAiChatCompletionResponse;
import com.example.ai.infrastructure.client.dto.response.ProductDraftAssistAiResponse;
import com.example.ai.infrastructure.config.ProductDraftAssistProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
public class OpenAiProductDraftGenerator implements ProductDraftGenerator {

    private static final String DEFAULT_OPENAI_BASE_URL = "https://api.openai.com";
    private static final String DEFAULT_MODEL = "gpt-5.4-nano";

    private final RestClient restClient;
    private final ProductDraftAssistProperties properties;
    private final ProductDraftAssistPromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;

    public OpenAiProductDraftGenerator(
            RestClient.Builder restClientBuilder,
            ProductDraftAssistProperties properties,
            ProductDraftAssistPromptBuilder promptBuilder,
            ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.promptBuilder = promptBuilder;
        this.objectMapper = objectMapper;
        String baseUrl = isBlank(properties.openaiBaseUrl()) ? DEFAULT_OPENAI_BASE_URL : properties.openaiBaseUrl();
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .requestFactory(buildRequestFactory())
                .build();
    }

    @Override
    public ProductDraftAssistResult generate(ProductDraftAssistCommand command) {
        validateConfiguration();

        OpenAiMultimodalChatCompletionRequest request = buildRequest(command);
        String content = requestProductDraftAssist(request);
        ProductDraftAssistAiResponse response = parseResponse(content);
        return new ProductDraftAssistResult(
                normalize(response.suggestedTitle()),
                normalize(response.suggestedDescription()),
                response.suggestedPrice() == null ? BigDecimal.ZERO : response.suggestedPrice(),
                response.suggestedKeywords() == null ? List.of() : response.suggestedKeywords(),
                normalize(response.notes())
        );
    }

    private void validateConfiguration() {
        if (!properties.enabled()) {
            throw new ProductDraftAssistConfigurationException("ai.product-draft.assist.enabled 설정이 필요합니다.");
        }
        if (isBlank(properties.openaiApiKey())) {
            throw new ProductDraftAssistConfigurationException("ai.product-draft.assist.openai-api-key 설정이 필요합니다.");
        }
    }

    private OpenAiMultimodalChatCompletionRequest buildRequest(ProductDraftAssistCommand command) {
        String model = isBlank(properties.model()) ? DEFAULT_MODEL : properties.model();
        return new OpenAiMultimodalChatCompletionRequest(
                model,
                List.of(
                        new Message("system", List.of(ContentPart.text(promptBuilder.buildSystemPrompt()))),
                        new Message("user", buildUserContent(command))
                ),
                properties.temperature(),
                new ResponseFormat("json_object")
        );
    }

    private List<ContentPart> buildUserContent(ProductDraftAssistCommand command) {
        List<ContentPart> contentParts = new ArrayList<>();
        contentParts.add(ContentPart.text(promptBuilder.buildUserPrompt(command)));
        for (MultipartFile image : command.images()) {
            contentParts.add(ContentPart.imageUrl(buildDataUrl(image)));
        }
        return contentParts;
    }

    private String requestProductDraftAssist(OpenAiMultimodalChatCompletionRequest request) {
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
            throw new ProductDraftAssistExternalCallException(
                    "OpenAI 상품 초안 생성 호출 실패: status=%s body=%s".formatted(
                            e.getStatusCode(),
                            e.getResponseBodyAsString()
                    ),
                    e
            );
        } catch (RestClientException e) {
            throw new ProductDraftAssistExternalCallException("OpenAI 상품 초안 생성 호출 중 네트워크 오류가 발생했습니다.", e);
        }
    }

    private String extractContent(OpenAiChatCompletionResponse response) {
        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new ProductDraftAssistResponseInvalidException("OpenAI 상품 초안 생성 응답이 비어 있습니다.");
        }

        OpenAiChatCompletionResponse.Choice choice = response.choices().getFirst();
        if (choice == null || choice.message() == null || isBlank(choice.message().content())) {
            throw new ProductDraftAssistResponseInvalidException("OpenAI 상품 초안 생성 응답 content가 비어 있습니다.");
        }
        return choice.message().content();
    }

    private ProductDraftAssistAiResponse parseResponse(String content) {
        try {
            return objectMapper.readValue(normalizeJsonContent(content), ProductDraftAssistAiResponse.class);
        } catch (Exception e) {
            throw new ProductDraftAssistResponseInvalidException("OpenAI 상품 초안 생성 응답 파싱에 실패했습니다.", e);
        }
    }

    private String buildDataUrl(MultipartFile image) {
        try {
            String mimeType = image.getContentType();
            String base64 = Base64.getEncoder().encodeToString(image.getBytes());
            return "data:%s;base64,%s".formatted(mimeType, base64);
        } catch (IOException e) {
            throw new ProductDraftAssistImageReadException("이미지 파일을 읽는 중 오류가 발생했습니다.", e);
        }
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

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private SimpleClientHttpRequestFactory buildRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(sanitizeTimeout(properties.connectTimeoutMs(), 3000));
        factory.setReadTimeout(sanitizeTimeout(properties.readTimeoutMs(), 10000));
        return factory;
    }

    private int sanitizeTimeout(int timeoutMs, int defaultValue) {
        return timeoutMs > 0 ? timeoutMs : defaultValue;
    }
}
