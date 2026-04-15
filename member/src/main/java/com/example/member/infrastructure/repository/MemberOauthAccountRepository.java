package com.example.member.infrastructure.repository;

import com.example.member.domain.entity.MemberOauthAccount;
import com.example.member.domain.enumtype.OAuthProvider;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberOauthAccountRepository {

    private final MemberOauthAccountJpaRepository memberOauthAccountJpaRepository;

    public MemberOauthAccount save(MemberOauthAccount memberOauthAccount) {
        return memberOauthAccountJpaRepository.save(memberOauthAccount);
    }

    public Optional<MemberOauthAccount> findByProviderAndProviderUserId(OAuthProvider provider, String providerUserId) {
        return memberOauthAccountJpaRepository.findByProviderAndProviderUserId(provider, providerUserId);
    }

    public Optional<MemberOauthAccount> findByMemberIdAndProvider(UUID memberId, OAuthProvider provider) {
        return memberOauthAccountJpaRepository.findByMemberIdAndProvider(memberId, provider);
    }

    public boolean existsByProviderAndProviderUserId(OAuthProvider provider, String providerUserId) {
        return memberOauthAccountJpaRepository.existsByProviderAndProviderUserId(provider, providerUserId);
    }

    public boolean existsByMemberIdAndProvider(UUID memberId, OAuthProvider provider) {
        return memberOauthAccountJpaRepository.existsByMemberIdAndProvider(memberId, provider);
    }
}
