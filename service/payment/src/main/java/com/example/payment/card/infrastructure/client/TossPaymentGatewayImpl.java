package com.example.payment.card.infrastructure.client;

import com.example.payment.common.common.exception.PaymentGatewayException;
import com.example.payment.common.common.exception.PaymentGatewayAmountConversionException;
import com.example.payment.card.domain.service.TossPaymentGateway;
import com.example.payment.common.infrastructure.config.TossPaymentsProperties;
import java.math.BigDecimal;
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
 * Toss Payments API ?몄텧???대떦?섎뒗 infrastructure adapter??
 * ?몃? ?묐떟 ?뺤떇怨??대씪?댁뼵???덉쇅瑜?payment 怨듯넻 ?덉쇅濡?蹂?섑빐 application 怨꾩링???꾨떖?쒕떎.
 */
public class TossPaymentGatewayImpl implements TossPaymentGateway {

    private final RestClient restClient;
    private final TossPaymentsProperties properties;

    public TossPaymentGatewayImpl(RestClient.Builder restClientBuilder, TossPaymentsProperties properties) {
        this.properties = properties;
        this.restClient = restClientBuilder
                .baseUrl(properties.baseUrl()) // 湲곕낯 url ?ㅼ젙
                .defaultHeader(HttpHeaders.AUTHORIZATION, basicAuth(properties.secretKey())) // ?몄쬆 ?ㅻ뜑 ?ㅼ젙
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE) // json?쇰줈 諛쏅룄濡??ㅼ젙
                .build();
    }

    @Override
    /**
     * Toss ?뱀씤 API瑜??몄텧?섍퀬 application?먯꽌 ?꾩슂??理쒖냼 ?묐떟留?異붾젮 諛섑솚?쒕떎.
     * HTTP ?ㅻ쪟, 鍮??묐떟, ?뚯떛 ?ㅻ쪟??紐⑤몢 PaymentGatewayException?쇰줈 ?듭씪?쒕떎.
     */
    public TossPaymentConfirmation confirm(String paymentKey, String orderId, BigDecimal amount) {
        validateConfiguration();
        try {
            TossConfirmResponse response = restClient.post()
                    .uri("/v1/payments/confirm")
                    // ?ｌ뼱 ?몃? ?쒖뒪耳먯씠 以묐났泥섎━?섏? ?딅룄濡?硫깅벑???ㅻ? ?ㅻ뜑濡?蹂대궦??
                    .header("Idempotency-Key", orderId)
                    .body(new TossConfirmRequest(paymentKey, orderId, toTossAmount(amount)))
                    .retrieve()// ?묐떟??媛?몄샂
                    .body(TossConfirmResponse.class); //JSON ?묐떟??Java 媛앹껜濡???쭅?ы솕


            if (response == null) {
                throw new PaymentGatewayException("?좎뒪 寃곗젣 ?뱀씤 ?묐떟??鍮꾩뼱 ?덉뒿?덈떎.");
            }
            if (response.paymentKey() == null || response.orderId() == null
                    || response.totalAmount() == null || response.approvedAt() == null) {
                throw new PaymentGatewayException("?좎뒪 寃곗젣 ?뱀씤 ?묐떟???꾩닔 媛믪씠 ?꾨씫?섏뿀?듬땲??");
            }

            return new TossPaymentConfirmation(
                    response.paymentKey(),
                    response.orderId(),
                    BigDecimal.valueOf(response.totalAmount()),
                    OffsetDateTime.parse(response.approvedAt()).toLocalDateTime(),
                    response.method(),
                    response.transfer() == null ? null : response.transfer().bankCode(),
                    response.card() == null ? null : response.card().company()
            );
        } catch (RestClientResponseException e) {
            throw new PaymentGatewayException(
                    "?좎뒪 寃곗젣 ?뱀씤 ?몄텧???ㅽ뙣?덉뒿?덈떎. status=%s body=%s"
                            .formatted(e.getStatusCode(), e.getResponseBodyAsString()),
                    e
            );
        } catch (RestClientException e) {
            throw new PaymentGatewayException("?좎뒪 寃곗젣 ?뱀씤 API ?몄텧???ㅽ뙣?덉뒿?덈떎.", e);
        } catch (RuntimeException e) {
            if (e instanceof PaymentGatewayException) {
                throw e;
            }
            throw new PaymentGatewayException("?좎뒪 寃곗젣 ?뱀씤 ?묐떟 泥섎━???ㅽ뙣?덉뒿?덈떎.", e);
        }
    }

    @Override
    /**
     * Toss 痍⑥냼 API瑜??몄텧?섍퀬 留덉?留?cancel ??ぉ??湲곗??쇰줈 ?섎텋 寃곌낵瑜?援ъ꽦?쒕떎.
     * ?뱀씤怨??숈씪?섍쾶 ?몃? ?듭떊 諛??묐떟 ?ㅻ쪟??PaymentGatewayException?쇰줈 媛먯떬??
     */
    public TossPaymentCancellation cancel(String paymentKey, String cancelReason, BigDecimal cancelAmount) {
        validateConfiguration();

        try {
            TossCancelResponse response = restClient.post()
                    .uri("/v1/payments/{paymentKey}/cancel", paymentKey)
                    // 硫깅벑????                    .header("Idempotency-Key", paymentKey + "-cancel")
                    .body(new TossCancelRequest(cancelReason, toTossCancelAmount(cancelAmount))) // ?쇰? 痍⑥냼?섏? ?딆쓣 寃쎌슦 cancelAmount??null濡?蹂대궪 ???덈떎.
                    .retrieve()
                    .body(TossCancelResponse.class);


            if (response == null || response.cancels() == null || response.cancels().isEmpty()) {
                throw new PaymentGatewayException("Toss cancel response is empty.");
            }
            //痍⑥냼 紐⑸줉?먯꽌 留덉?留??섎굹??紐⑸줉???대뒗??
            TossCancelItem lastCancel = response.cancels().get(response.cancels().size() - 1);
            if (lastCancel.cancelAmount() == null || lastCancel.canceledAt() == null) {
                throw new PaymentGatewayException("Toss cancel response is missing required fields.");
            }

            return new TossPaymentCancellation(
                    response.paymentKey(),
                    BigDecimal.valueOf(lastCancel.cancelAmount()),
                    OffsetDateTime.parse(lastCancel.canceledAt()).toLocalDateTime()
            );
        } catch (RestClientResponseException e) {
            throw new PaymentGatewayException(
                    "Toss cancel failed. status=%s body=%s".formatted(e.getStatusCode(), e.getResponseBodyAsString()),
                    e
            );
        } catch (RestClientException e) {
            throw new PaymentGatewayException("?좎뒪 寃곗젣 痍⑥냼 API ?몄텧???ㅽ뙣?덉뒿?덈떎.", e);
        } catch (RuntimeException e) {
            if (e instanceof PaymentGatewayException) {
                throw e;
            }
            throw new PaymentGatewayException("?좎뒪 寃곗젣 痍⑥냼 ?묐떟 泥섎━???ㅽ뙣?덉뒿?덈떎.", e);
        }
    }

    /**
     * ?몃? API ?몄텧 ???꾩닔 ?ㅼ젙媛믪쓣 ?뺤씤?쒕떎.
     */
    private void validateConfiguration() {
        if (isBlank(properties.baseUrl())) {
            throw new PaymentGatewayException("?좎뒪 寃곗젣 base-url ?ㅼ젙? ?꾩닔?낅땲??");
        }
        if (isBlank(properties.secretKey())) {
            throw new PaymentGatewayException("?좎뒪 寃곗젣 secret-key ?ㅼ젙? ?꾩닔?낅땲??");
        }
    }

    private Long toTossAmount(BigDecimal amount) {
        if (amount == null) {
            throw new PaymentGatewayException("?좎뒪 寃곗젣 ?뱀씤 湲덉븸? ?꾩닔?낅땲??");
        }
        try {
            return amount.longValueExact();
        } catch (ArithmeticException e) {
            throw new PaymentGatewayAmountConversionException("Toss ?뱀씤 ?붿껌 湲덉븸? ???⑥쐞 ?뺤닔?ъ빞 ?⑸땲?? amount=" + amount, e);
        }
    }

    private Long toTossCancelAmount(BigDecimal cancelAmount) {
        if (cancelAmount == null) {
            return null;
        }
        try {
            return cancelAmount.longValueExact();
        } catch (ArithmeticException e) {
            throw new PaymentGatewayAmountConversionException(
                    "Toss 痍⑥냼 ?붿껌 湲덉븸? ???⑥쐞 ?뺤닔?ъ빞 ?⑸땲?? cancelAmount=" + cancelAmount,
                    e
            );
        }
    }

    // Toss API??secretKey瑜?username?쇰줈 ?섎뒗 Basic Auth瑜??ъ슜?쒕떎.
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
