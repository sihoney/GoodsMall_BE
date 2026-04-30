package com.example.order.application.service;

import com.example.order.domain.entity.ReturnRequest;
import com.example.order.domain.enumtype.ReturnRequestStatus;
import com.example.order.domain.repository.ReturnRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnPickupScheduler {

    private static final int RECEIVE_DELAY_SECONDS = 10;

    private final ReturnRequestRepository returnRequestRepository;

    /**
     * Mock carrier 시뮬레이션: PICKED_UP 후 일정 시간 경과한 ReturnRequest를 RECEIVED로 자동 전이.
     * 실제 운영에선 carrier API 또는 셀러 수령 처리로 교체.
     */
    @Scheduled(fixedDelay = 5_000) // 5초마다
    @Transactional
    public void simulateSellerReceive() {
        LocalDateTime threshold = LocalDateTime.now().minusSeconds(RECEIVE_DELAY_SECONDS);
        List<ReturnRequest> targets = returnRequestRepository
                .findByStatusAndPickedUpAtBefore(ReturnRequestStatus.PICKED_UP, threshold);

        if (targets.isEmpty()) {
            return;
        }

        targets.forEach(ReturnRequest::receive);
        log.info("Mock 셀러 수령 처리 완료. count={}", targets.size());
    }
}
