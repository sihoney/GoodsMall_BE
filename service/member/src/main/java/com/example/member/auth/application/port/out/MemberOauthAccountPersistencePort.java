package com.example.member.auth.application.port.out;

import com.example.member.auth.domain.entity.MemberOauthAccount;
import com.example.member.auth.domain.enumtype.OAuthProvider;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MemberOauthAccountPersistencePort {

    MemberOauthAccount save(MemberOauthAccount memberOauthAccount);

    Optional<MemberOauthAccount> findByProviderAndProviderUserId(OAuthProvider provider, String providerUserId);

    Optional<MemberOauthAccount> findByMemberIdAndProvider(UUID memberId, OAuthProvider provider);

    List<MemberOauthAccount> findAllByMemberId(UUID memberId);

    boolean existsByProviderAndProviderUserId(OAuthProvider provider, String providerUserId);

    boolean existsByMemberIdAndProvider(UUID memberId, OAuthProvider provider);

    void delete(MemberOauthAccount memberOauthAccount);
}
