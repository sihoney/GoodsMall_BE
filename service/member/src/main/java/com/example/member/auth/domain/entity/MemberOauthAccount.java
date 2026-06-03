package com.example.member.auth.domain.entity;

import com.example.member.auth.domain.enumtype.OAuthProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
// TODO: Separate persistence mapping from the domain model so this can become a pure domain entity.
@Entity
@Table(
        name = "member_oauth_account",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_member_oauth_provider_user", columnNames = {"provider", "provider_user_id"}),
                @UniqueConstraint(name = "uq_member_oauth_member_provider", columnNames = {"member_id", "provider"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberOauthAccount {

    @Id
    @Column(name = "oauth_account_id", nullable = false, updatable = false)
    private UUID oauthAccountId;

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false)
    private OAuthProvider provider;

    @Column(name = "provider_user_id", nullable = false)
    private String providerUserId;

    @Column(name = "provider_email")
    private String providerEmail;

    @Column(name = "provider_nickname")
    private String providerNickname;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private MemberOauthAccount(
            UUID oauthAccountId,
            UUID memberId,
            OAuthProvider provider,
            String providerUserId,
            String providerEmail,
            String providerNickname,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.oauthAccountId = Objects.requireNonNull(oauthAccountId);
        this.memberId = Objects.requireNonNull(memberId);
        this.provider = Objects.requireNonNull(provider);
        this.providerUserId = Objects.requireNonNull(providerUserId);
        this.providerEmail = providerEmail;
        this.providerNickname = providerNickname;
        this.createdAt = Objects.requireNonNull(createdAt);
        this.updatedAt = Objects.requireNonNull(updatedAt);
    }

    public static MemberOauthAccount create(
            UUID oauthAccountId,
            UUID memberId,
            OAuthProvider provider,
            String providerUserId,
            String providerEmail,
            String providerNickname,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new MemberOauthAccount(
                oauthAccountId,
                memberId,
                provider,
                providerUserId,
                providerEmail,
                providerNickname,
                createdAt,
                updatedAt
        );
    }
}
