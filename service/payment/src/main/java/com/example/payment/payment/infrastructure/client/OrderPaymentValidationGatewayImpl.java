package com.example.payment.payment.infrastructure.client;


import com.example.payment.common.infrastructure.client.OrderClient;
import com.example.payment.common.exception.InvalidCardPaymentRequestException;
import com.example.payment.payment.domain.service.OrderPaymentValidationData;
import com.example.payment.payment.domain.service.OrderPaymentValidationItemData;
import com.example.payment.payment.domain.service.OrderPaymentValidationGateway;
import com.example.payment.payment.infrastructure.client.dto.request.OrderPaymentValidationRequest;
import com.example.payment.payment.infrastructure.client.dto.response.OrderPaymentValidationResponse;
import feign.FeignException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class OrderPaymentValidationGatewayImpl implements OrderPaymentValidationGateway {

    private final OrderClient orderClient;

    public OrderPaymentValidationGatewayImpl(OrderClient orderClient) {
        this.orderClient = orderClient;
    }

    @Override
    public OrderPaymentValidationData validate(UUID orderId, UUID buyerId, java.math.BigDecimal amount) {
        try {
            var response = orderClient.validatePayment(
                    orderId,
                    new OrderPaymentValidationRequest(buyerId, amount)
            );
            if (response == null) {
                throw new InvalidCardPaymentRequestException("二쇰Ц ?쒕퉬??寃곗젣 寃利??묐떟??鍮꾩뼱 ?덉뒿?덈떎.");
            }
            if (!response.success()) {
                String message = response.error() != null && response.error().message() != null
                        ? response.error().message()
                        : "二쇰Ц ?쒕퉬??寃곗젣 寃利앹뿉 ?ㅽ뙣?덉뒿?덈떎.";
                throw new InvalidCardPaymentRequestException("二쇰Ц ?쒕퉬??寃곗젣 寃利앹뿉 ?ㅽ뙣?덉뒿?덈떎. " + message);
            }
            if (response.data() == null) {
                throw new InvalidCardPaymentRequestException("二쇰Ц ?쒕퉬??寃곗젣 寃利??묐떟 ?곗씠?곌? 鍮꾩뼱 ?덉뒿?덈떎.");
            }

            List<OrderPaymentValidationItemData> orderItems = response.data().items() == null
                    ? Collections.emptyList()
                    : response.data().items().stream()
                            .map(orderItem -> new OrderPaymentValidationItemData(
                                    orderItem.orderItemId(),
                                    orderItem.sellerId(),
                                    orderItem.lineAmount()
                            ))
                            .toList();

            return new OrderPaymentValidationData(orderItems);
        } catch (FeignException e) {
            throw new InvalidCardPaymentRequestException(
                    "二쇰Ц ?쒕퉬??寃곗젣 寃利??몄텧???ㅽ뙣?덉뒿?덈떎. status=%s body=%s"
                            .formatted(e.status(), e.contentUTF8()),
                    e
            );
        } catch (RuntimeException e) {
            if (e instanceof InvalidCardPaymentRequestException) {
                throw e;
            }
            throw new InvalidCardPaymentRequestException("二쇰Ц 寃곗젣 寃利?以??ㅻ쪟媛 諛쒖깮?덉뒿?덈떎.", e);
        }
    }
}
