package com.example.order.infrastructure.client;

import com.example.order.application.port.TrackingPort;
import com.example.order.common.exception.CustomException;
import com.example.order.common.exception.ErrorCode;
import com.example.order.infrastructure.client.dto.response.SweetTrackerResponse;
import com.example.order.presentation.dto.response.DeliveryTrackingDetailResponse;
import com.example.order.presentation.dto.response.DeliveryTrackingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SweetTrackerClientAdapter implements TrackingPort {

    private final TrackingClient trackingClient;

    @Value("${sweet-tracker.api.key}")
    private String apiKey;

    @Override
    public DeliveryTrackingResponse getTrackingInfo(String courierCode, String invoiceNumber) {
        SweetTrackerResponse response = trackingClient.getTrackingInfo(apiKey, courierCode, invoiceNumber);
        if (response == null) {
            throw new CustomException(ErrorCode.EXTERNAL_SERVICE_ERROR);
        }
        return toTrackingResponse(courierCode, invoiceNumber, response);
    }

    private DeliveryTrackingResponse toTrackingResponse(
            String courierCode,
            String invoiceNumber,
            SweetTrackerResponse response
    ) {
        List<DeliveryTrackingDetailResponse> details =
                Optional.ofNullable(response.trackingDetails())
                        .orElse(List.of())
                        .stream()
                        .map(detail -> new DeliveryTrackingDetailResponse(
                                detail.timeString(),
                                detail.where(),
                                detail.kind()
                        ))
                        .toList();
        String currentStatus = details.isEmpty()
                ? null
                : details.get(details.size() - 1).status();

        boolean delivered = "배송완료".equals(currentStatus);
        return new DeliveryTrackingResponse(
                courierCode,
                invoiceNumber,
                delivered,
                details
        );
    }
}
