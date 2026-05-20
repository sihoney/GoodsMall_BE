package com.example.order.infrastructure.client;

import com.example.order.infrastructure.client.dto.response.SweetTrackerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "tracking-client", url = "${sweet-tracker.api.base-url}")
public interface TrackingClient {

    @GetMapping("/api/v1/trackingInfo")
    SweetTrackerResponse getTrackingInfo(
            @RequestParam("t_key") String apiKey,
            @RequestParam("t_code") String courierCode,
            @RequestParam("t_invoice") String invoiceNumber
    );
}