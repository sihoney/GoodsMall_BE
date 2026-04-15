package com.example.member.infrastructure.repository;

import com.example.member.domain.entity.MemberOauthAccount;
import com.example.member.domain.enumtype.OAuthProvider;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberOauthAccountJpaRepository extends JpaRepository<MemberOauthAccount, UUID> {

    Optional<MemberOauthAccount> findByProviderAndProviderUserId(OAuthProvider provider, String providerUserId);

    Optional<MemberOauthAccount> findByMemberIdAndProvider(UUID memberId, OAuthProvider provider);

    boolean existsByProviderAndProviderUserId(OAuthProvider provider, String providerUserId);

    boolean existsByMemberIdAndProvider(UUID memberId, OAuthProvider provider);
}
