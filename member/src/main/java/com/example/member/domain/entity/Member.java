package com.example.member.domain.entity;

import com.example.member.domain.enumtype.MemberStatus;
import com.todaylunch.common.security.auth.enumtype.MemberRole;

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
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id
    @Column(name = "member_id", nullable = false, updatable = false)
    private UUID memberId;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address")
    private String address;

    @Column(name = "profile_image_key")
    private String profileImageKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private MemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MemberStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private Member(
        UUID memberId,
        String email,
        String password,
        String nickname,
        String phone,
        String address,
        String profileImageKey,
        MemberRole role,
        MemberStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.memberId = Objects.requireNonNull(memberId);
        this.email = Objects.requireNonNull(email);
        this.password = Objects.requireNonNull(password);
        this.nickname = Objects.requireNonNull(nickname);
        this.phone = phone;
        this.address = address;
        this.profileImageKey = profileImageKey;
        this.role = Objects.requireNonNull(role);
        this.status = Objects.requireNonNull(status);
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static Member create(
        UUID memberId,
        String email,
        String password,
        String nickname,
        String phone,
        String address,
        String profileImageKey,
        MemberRole role,
        MemberStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new Member(
            memberId,
            email,
            password,
            nickname,
            phone,
            address,
            profileImageKey,
            role,
            status,
            createdAt,
            updatedAt
        );
    }

    public void changeNickname(String nickname, LocalDateTime updatedAt) {
        this.nickname = Objects.requireNonNull(nickname);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public void updateAccount(
        String email,
        String password,
        String nickname,
        String phone,
        String address,
        String profileImageKey,
        LocalDateTime updatedAt
    ) {
        this.email = Objects.requireNonNull(email);
        this.password = Objects.requireNonNull(password);
        this.nickname = Objects.requireNonNull(nickname);
        this.phone = phone;
        this.address = address;
        this.profileImageKey = profileImageKey;
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public void updateProfile(String phone, String address, String profileImageKey, LocalDateTime updatedAt) {
        this.phone = phone;
        this.address = address;
        this.profileImageKey = profileImageKey;
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public void changeStatus(MemberStatus status, LocalDateTime updatedAt) {
        MemberStatus nextStatus = Objects.requireNonNull(status);
        validateStatusTransition(nextStatus);
        this.status = nextStatus;
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    // TODO: 관리자 기능으로 회원 상태를 변경하는 경우, 특정 상태로의 전환이 허용되는지 검증하는 로직 추가 (예: PENDING_VERIFICATION -> ACTIVE, ACTIVE -> SUSPENDED 등)
    public void changeRole(MemberRole role, LocalDateTime updatedAt) {
        this.role = Objects.requireNonNull(role);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public boolean isActive() {
        return status == MemberStatus.ACTIVE;
    }

    private void validateStatusTransition(MemberStatus nextStatus) {
        if (status == nextStatus) {
            return;
        }

        boolean allowed = switch (status) {
            case PENDING_VERIFICATION -> nextStatus == MemberStatus.ACTIVE || nextStatus == MemberStatus.DELETED;
            case ACTIVE -> nextStatus == MemberStatus.SUSPENDED || nextStatus == MemberStatus.WITHDRAWN;
            case SUSPENDED -> nextStatus == MemberStatus.ACTIVE;
            case WITHDRAWN -> nextStatus == MemberStatus.DELETED;
            case DELETED -> false;
        };

        if (!allowed) {
            throw new IllegalStateException("허용되지 않는 회원 상태 전이입니다: " + status + " -> " + nextStatus);
        }
    }
}
