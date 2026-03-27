package com.example.member.domain.entity;

import com.example.member.domain.enumtype.MemberRole;
import com.example.member.domain.enumtype.MemberStatus;

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

    @Column(name = "profile_image_url")
    private String profileImageUrl;

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
        String profileImageUrl,
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
        this.profileImageUrl = profileImageUrl;
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
        String profileImageUrl,
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
            profileImageUrl,
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
        String profileImageUrl,
        LocalDateTime updatedAt
    ) {
        this.email = Objects.requireNonNull(email);
        this.password = Objects.requireNonNull(password);
        this.nickname = Objects.requireNonNull(nickname);
        this.phone = phone;
        this.address = address;
        this.profileImageUrl = profileImageUrl;
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public void updateProfile(String phone, String address, String profileImageUrl, LocalDateTime updatedAt) {
        this.phone = phone;
        this.address = address;
        this.profileImageUrl = profileImageUrl;
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public void changeStatus(MemberStatus status, LocalDateTime updatedAt) {
        this.status = Objects.requireNonNull(status);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public void changeRole(MemberRole role, LocalDateTime updatedAt) {
        this.role = Objects.requireNonNull(role);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }
}
