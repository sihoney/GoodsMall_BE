package com.example.notification.presentation.controller;

import com.example.notification.application.usecase.NotificationUsecase;
import com.example.notification.presentation.dto.ApiResponse;
import com.example.notification.presentation.dto.NotificationResponse;
import com.example.notification.presentation.dto.NotificationUnreadCountResponse;
import com.example.notification.presentation.dto.PagedResponse;
import com.todaylunch.common.security.auth.annotation.CurrentMember;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notification", description = "notification API")
public class NotificationController {

    private final NotificationUsecase notificationUsecase;

    public NotificationController(NotificationUsecase notificationUsecase) {
        this.notificationUsecase = notificationUsecase;
    }

    @GetMapping
    @Operation(summary = "내 알림 목록 조회")
    public ResponseEntity<ApiResponse<PagedResponse<NotificationResponse>>> getMyNotifications(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationUsecase.getMyNotifications(authenticatedMember.memberId(), page, size)
        ));
    }

    @GetMapping("/unread-count")
    @Operation(summary = "읽지 않은 알림 개수 조회")
    public ResponseEntity<ApiResponse<NotificationUnreadCountResponse>> getUnreadCount(
            @CurrentMember AuthenticatedMember authenticatedMember
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationUsecase.getUnreadNotificationCount(authenticatedMember.memberId())
        ));
    }

    @PatchMapping("/{notificationId}/read")
    @Operation(summary = "알림 읽음 처리")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @CurrentMember AuthenticatedMember authenticatedMember,
            @PathVariable java.util.UUID notificationId
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                notificationUsecase.markAsRead(authenticatedMember.memberId(), notificationId)
        ));
    }
}
