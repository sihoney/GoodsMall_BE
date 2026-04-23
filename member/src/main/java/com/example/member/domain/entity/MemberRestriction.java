package com.example.member.domain.entity;

import com.example.member.domain.enumtype.RestrictionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "member_restriction")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberRestriction {

    @Id
    @Column(name = "restriction_id", nullable = false, updatable = false)
    private UUID restrictionId;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "admin_id", nullable = false)
    private UUID adminId;

    @Column(name = "reason", nullable = false)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "restriction_type", nullable = false)
    private RestrictionType restrictionType;

    @Column(name = "duration_hours", nullable = false)
    private Integer durationHours;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    private MemberRestriction(
            UUID restrictionId,
            UUID memberId,
            UUID adminId,
            String reason,
            RestrictionType restrictionType,
            Integer durationHours,
            LocalDateTime endAt,
            boolean active,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.restrictionId = Objects.requireNonNull(restrictionId);
        this.memberId = Objects.requireNonNull(memberId);
        this.adminId = Objects.requireNonNull(adminId);
        this.reason = Objects.requireNonNull(reason);
        this.restrictionType = Objects.requireNonNull(restrictionType);
        this.durationHours = Objects.requireNonNull(durationHours);
        this.endAt = Objects.requireNonNull(endAt);
        this.active = active;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = updatedAt;
    }

    public static MemberRestriction create(
            UUID restrictionId,
            UUID memberId,
            UUID adminId,
            String reason,
            RestrictionType restrictionType,
            Integer durationHours,
            LocalDateTime createdAt
    ) {
        validateReason(reason);
        validateDurationHours(durationHours);
        LocalDateTime normalizedCreatedAt = Objects.requireNonNull(createdAt);

        return new MemberRestriction(
                restrictionId,
                memberId,
                adminId,
                reason.trim(),
                restrictionType,
                durationHours,
                normalizedCreatedAt.plusHours(durationHours.longValue()),
                true,
                normalizedCreatedAt,
                normalizedCreatedAt
        );
    }

    public void deactivate(LocalDateTime updatedAt) {
        if (!active) {
            throw new IllegalStateException("이미 비활성화된 제재입니다.");
        }
        this.active = false;
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public boolean isEffectiveAt(LocalDateTime dateTime) {
        return active && endAt.isAfter(Objects.requireNonNull(dateTime));
    }

    private static void validateReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("reason은 필수입니다.");
        }
    }

    private static void validateDurationHours(Integer durationHours) {
        if (durationHours == null || durationHours <= 0) {
            throw new IllegalArgumentException("durationHours는 0보다 커야 합니다.");
        }
    }
}
