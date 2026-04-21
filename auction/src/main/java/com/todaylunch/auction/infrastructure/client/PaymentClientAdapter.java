package com.todaylunch.auction.infrastructure.client;

import com.todaylunch.auction.application.port.BidFeeChargePort;
import com.todaylunch.auction.application.port.dto.request.BidFeeChargeRequest;
import com.todaylunch.auction.application.port.dto.response.BidFeeChargeResponse;
import com.todaylunch.auction.infrastructure.client.dto.request.ClientBidFeeChargeRequest;
import com.todaylunch.auction.infrastructure.client.dto.response.ClientBidFeeChargeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentClientAdapter implements BidFeeChargePort {

    private final PaymentClient paymentClient;

    @Override
    public BidFeeChargeResponse chargeBidFee(BidFeeChargeRequest request) {
        ClientBidFeeChargeRequest externalRequest = toExternalRequest(request);
        ClientBidFeeChargeResponse externalResponse = paymentClient.chargeBidFee(externalRequest).data();
        return toResponse(externalResponse);
    }

    private ClientBidFeeChargeRequest toExternalRequest(BidFeeChargeRequest request) {
        return new ClientBidFeeChargeRequest(
                request.auctionId(),
                request.previousBidderId(),
                request.previousBidderPaidFee(),
                request.highestBidderId(),
                request.highestBidderFee()
        );
    }

    private BidFeeChargeResponse toResponse(ClientBidFeeChargeResponse response) {
        return new BidFeeChargeResponse(
                response.auctionId(),
                response.highestBidderId(),
                response.heldAmount(),
                response.previousBidderId(),
                response.refundedAmount()
        );
    }
}
