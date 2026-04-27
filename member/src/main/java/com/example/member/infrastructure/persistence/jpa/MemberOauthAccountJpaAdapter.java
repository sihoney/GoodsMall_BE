package com.example.member.infrastructure.persistence.jpa;

import com.example.member.application.port.out.MemberOauthAccountPersistencePort;
import com.example.member.domain.entity.MemberOauthAccount;
import com.example.member.domain.enumtype.OAuthProvider;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberOauthAccountJpaAdapter implements MemberOauthAccountPersistencePort {

    private final MemberOauthAccountJpaRepository memberOauthAccountJpaRepository;

    @Override
    public MemberOauthAccount save(MemberOauthAccount memberOauthAccount) {
        return memberOauthAccountJpaRepository.save(memberOauthAccount);
    }

    @Override
    public Optional<MemberOauthAccount> findByProviderAndProviderUserId(OAuthProvider provider, String providerUserId) {
        return memberOauthAccountJpaRepository.findByProviderAndProviderUserId(provider, providerUserId);
    }

    @Override
    public Optional<MemberOauthAccount> findByMemberIdAndProvider(UUID memberId, OAuthProvider provider) {
        return memberOauthAccountJpaRepository.findByMemberIdAndProvider(memberId, provider);
    }

    @Override
    public List<MemberOauthAccount> findAllByMemberId(UUID memberId) {
        return memberOauthAccountJpaRepository.findAllByMemberIdOrderByCreatedAtDesc(memberId);
    }

    @Override
    public boolean existsByProviderAndProviderUserId(OAuthProvider provider, String providerUserId) {
        return memberOauthAccountJpaRepository.existsByProviderAndProviderUserId(provider, providerUserId);
    }

    @Override
    public boolean existsByMemberIdAndProvider(UUID memberId, OAuthProvider provider) {
        return memberOauthAccountJpaRepository.existsByMemberIdAndProvider(memberId, provider);
    }

    @Override
    public void delete(MemberOauthAccount memberOauthAccount) {
        memberOauthAccountJpaRepository.delete(memberOauthAccount);
    }
}
