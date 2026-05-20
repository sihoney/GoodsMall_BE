package com.example.notification.infrastructure.repository;

import com.example.notification.domain.entity.Notification;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationJpaRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findAllByMemberIdOrderByCreatedAtDesc(UUID memberId, Pageable pageable);

    boolean existsByEventIdAndMemberIdAndType(UUID eventId, UUID memberId, com.example.notification.domain.enumtype.NotificationType type);

    long countByMemberIdAndReadFalse(UUID memberId);
}
