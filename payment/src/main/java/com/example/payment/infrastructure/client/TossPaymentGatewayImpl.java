package com.example.payment.infrastructure.client;

import com.example.payment.common.exception.PaymentGatewayException;
import com.example.payment.domain.service.TossPaymentGateway;
import com.example.payment.infrastructure.config.TossPaymentsProperties;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Base64;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
/**
 * Toss Payments API 호출을 담당하는 infrastructure adapter다.
 * 외부 응답 형식과 클라이언트 예외를 payment 공통 예외로 변환해 application 계층에 전달한다.
 */
// TODO: TossPaymentGatewayImpl -> TossPaymentGatewayAdapter 네이밍 검토
// 구현체가 단순 구현보다 outbound adapter 역할에 가깝다
// TODO: infrastructure.client 보다 infrastructure.tosspayment 같은 외부 시스템 기준 패키지 분리 검토
// Todo: 에러 메시지 에러코드로 통일하고 한글로 통일할 것.
public class TossPaymentGatewayImpl implements TossPaymentGateway {

    private final RestClient restClient;
    private final TossPaymentsProperties properties;

    public TossPaymentGatewayImpl(RestClient.Builder restClientBuilder, TossPaymentsProperties properties) {
        this.properties = properties;
        this.restClient = restClientBuilder
                .baseUrl(properties.baseUrl()) // 기본 url 설정
                .defaultHeader(HttpHeaders.AUTHORIZATION, basicAuth(properties.secretKey())) // 인증 헤더 설정
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE) // json으로 받도록 설정
                .build();
    }

    @Override
    /**
     * Toss 승인 API를 호출하고 application에서 필요한 최소 응답만 추려 반환한다.
     * HTTP 오류, 빈 응답, 파싱 오류는 모두 PaymentGatewayException으로 통일한다.
     */
    public TossPaymentConfirmation confirm(String paymentKey, String orderId, Long amount) {
        validateConfiguration();
        // todo: 성공 url을 통해서 받은 amount와 orderId가 저장된 데이터와 일치하는지 보안상 확인이 필요할 수 있다.
        try {
            TossConfirmResponse response = restClient.post()
                    .uri("/v1/payments/confirm")
                    // 넣어 외부 시스켐이 중복처리하지 않도록 멱등성 키를 헤더로 보낸다.
                    .header("Idempotency-Key", orderId)
                    .body(new TossConfirmRequest(paymentKey, orderId, amount))
                    .retrieve()// 응답을 가져옴
                    .body(TossConfirmResponse.class); //JSON 응답을 Java 객체로 역직렬화

            // todo : 멱등성키를 사용한 상태에서 에러 응답 처리에 대한 로직이 필요

            if (response == null) {
                throw new PaymentGatewayException("Toss confirm response is empty.");
            }
            if (response.paymentKey() == null || response.orderId() == null
                    || response.totalAmount() == null || response.approvedAt() == null) {
                throw new PaymentGatewayException("Toss confirm response is missing required fields.");
            }

            return new TossPaymentConfirmation(
                    response.paymentKey(),
                    response.orderId(),
                    response.totalAmount(),
                    OffsetDateTime.parse(response.approvedAt()).toLocalDateTime(),
                    response.method(),
                    response.transfer() == null ? null : response.transfer().bankCode(),
                    response.card() == null ? null : response.card().company()
            );
        } catch (RestClientResponseException e) {
            throw new PaymentGatewayException(
                    "Toss confirm failed. status=%s body=%s".formatted(e.getStatusCode(), e.getResponseBodyAsString()),
                    e
            );
        } catch (RestClientException e) {
            throw new PaymentGatewayException("Failed to call Toss confirm API.", e);
        } catch (RuntimeException e) {
            if (e instanceof PaymentGatewayException) {
                throw e;
            }
            throw new PaymentGatewayException("Failed to parse Toss confirm response.", e);
        }
    }

    @Override
    /**
     * Toss 취소 API를 호출하고 마지막 cancel 항목을 기준으로 환불 결과를 구성한다.
     * 승인과 동일하게 외부 통신 및 응답 오류는 PaymentGatewayException으로 감싼다.
     */
    public TossPaymentCancellation cancel(String paymentKey, String cancelReason, Long cancelAmount) {
        validateConfiguration();

        try {
            TossCancelResponse response = restClient.post()
                    .uri("/v1/payments/{paymentKey}/cancel", paymentKey)
                    // 멱등성 키
                    .header("Idempotency-Key", paymentKey + "-cancel")
                    .body(new TossCancelRequest(cancelReason, cancelAmount)) // 일부 취소하지 않을 경우 cancelAmount을 null로 보낼 수 있다.
                    .retrieve()
                    .body(TossCancelResponse.class);

            //todo: 멱등성 키를 사용한 다음 로직이 없으므로 추가해야함.

            if (response == null || response.cancels() == null || response.cancels().isEmpty()) {
                throw new PaymentGatewayException("Toss cancel response is empty.");
            }
            //취소 목록에서 마지막 하나의 목록을 담는다.
            // todo: get()을 getLast() 변경하기 -> java21 버전이므로 가능.
            TossCancelItem lastCancel = response.cancels().get(response.cancels().size() - 1);
            if (lastCancel.cancelAmount() == null || lastCancel.canceledAt() == null) {
                throw new PaymentGatewayException("Toss cancel response is missing required fields.");
            }

            return new TossPaymentCancellation(
                    response.paymentKey(),
                    lastCancel.cancelAmount(),
                    OffsetDateTime.parse(lastCancel.canceledAt()).toLocalDateTime()
            );
        } catch (RestClientResponseException e) {
            throw new PaymentGatewayException(
                    "Toss cancel failed. status=%s body=%s".formatted(e.getStatusCode(), e.getResponseBodyAsString()),
                    e
            );
        } catch (RestClientException e) {
            throw new PaymentGatewayException("Failed to call Toss cancel API.", e);
        } catch (RuntimeException e) {
            if (e instanceof PaymentGatewayException) {
                throw e;
            }
            throw new PaymentGatewayException("Failed to parse Toss cancel response.", e);
        }
    }

    /**
     * 외부 API 호출 전 필수 설정값을 확인한다.
     */
    private void validateConfiguration() {
        if (isBlank(properties.baseUrl())) {
            throw new PaymentGatewayException("toss.payments.base-url is required.");
        }
        if (isBlank(properties.secretKey())) {
            throw new PaymentGatewayException("toss.payments.secret-key is required.");
        }
    }

    // Toss API는 secretKey를 username으로 하는 Basic Auth를 사용한다.
    private static String basicAuth(String secretKey) {
        String credentials = (secretKey == null ? "" : secretKey) + ":";
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record TossConfirmRequest(
            String paymentKey,
            String orderId,
            Long amount
    ) {
    }

    private record TossConfirmResponse(
            String paymentKey,
            String orderId,
            Long totalAmount,
            String approvedAt,
            String method,
            TossTransfer transfer,
            TossCard card
    ) {
    }

    private record TossTransfer(
            String bankCode
    ) {
    }

    private record TossCard(
            String company
    ) {
    }

    private record TossCancelRequest(
            String cancelReason,
            Long cancelAmount
    ) {
    }

    private record TossCancelResponse(
            String paymentKey,
            List<TossCancelItem> cancels
    ) {
    }

    private record TossCancelItem(
            Long cancelAmount,
            String canceledAt
    ) {
    }
}
