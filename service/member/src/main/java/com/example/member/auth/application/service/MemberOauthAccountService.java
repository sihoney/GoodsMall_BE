package com.example.member.auth.application.service;

import com.example.member.auth.application.dto.result.MemberOauthAccountItemResult;
import com.example.member.auth.application.dto.result.MemberOauthAccountListResult;
import com.example.member.auth.application.dto.result.MemberOauthAccountUnlinkResult;
import com.example.member.auth.application.port.out.MemberOauthAccountPersistencePort;
import com.example.member.member.application.port.out.MemberPersistencePort;
import com.example.member.auth.application.port.in.MemberOauthAccountUsecase;
import com.example.member.common.exception.LastLoginMethodRemovalNotAllowedException;
import com.example.member.common.exception.MemberNotFoundException;
import com.example.member.common.exception.MemberOauthAccountNotFoundException;
import com.example.member.member.domain.entity.Member;
import com.example.member.auth.domain.entity.MemberOauthAccount;
import com.example.member.auth.domain.enumtype.OAuthProvider;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberOauthAccountService implements MemberOauthAccountUsecase {

    private final MemberPersistencePort memberPersistencePort;
    private final MemberOauthAccountPersistencePort memberOauthAccountPersistencePort;

    @Override
    public MemberOauthAccountListResult getCurrentMemberOauthAccounts(UUID memberId) {
        Member member = getMember(memberId);
        List<MemberOauthAccount> accounts = memberOauthAccountPersistencePort.findAllByMemberId(memberId);
        boolean hasPasswordLogin = hasPasswordLogin(member);
        boolean canRemoveLastOauthAccount = hasPasswordLogin || accounts.size() > 1;
        boolean canUnlink = canUnlink(hasPasswordLogin, accounts.size());

        List<MemberOauthAccountItemResult> results = accounts.stream()
                .map(account -> new MemberOauthAccountItemResult(
                        account.getOauthAccountId(),
                        account.getProvider().name(),
                        account.getProviderEmail(),
                        account.getProviderNickname(),
                        account.getCreatedAt(),
                        account.getUpdatedAt(),
                        canUnlink
                ))
                .toList();

        return new MemberOauthAccountListResult(results, hasPasswordLogin, canRemoveLastOauthAccount);
    }

    @Transactional
    @Override
    public MemberOauthAccountUnlinkResult unlinkCurrentMemberOauthAccount(UUID memberId, String provider) {
        Member member = getMember(memberId);
        OAuthProvider oauthProvider = parseProvider(provider);
        MemberOauthAccount account = memberOauthAccountPersistencePort.findByMemberIdAndProvider(memberId, oauthProvider)
                .orElseThrow(MemberOauthAccountNotFoundException::new);

        List<MemberOauthAccount> accounts = memberOauthAccountPersistencePort.findAllByMemberId(memberId);
        if (!canUnlink(hasPasswordLogin(member), accounts.size())) {
            throw new LastLoginMethodRemovalNotAllowedException();
        }

        memberOauthAccountPersistencePort.delete(account);
        return new MemberOauthAccountUnlinkResult(true, oauthProvider.name());
    }

    private Member getMember(UUID memberId) {
        return memberPersistencePort.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
    }

    private OAuthProvider parseProvider(String provider) {
        if (provider == null || provider.trim().isEmpty()) {
            throw new IllegalArgumentException("provider는 필수입니다.");
        }

        try {
            return OAuthProvider.valueOf(provider.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("지원하지 않는 OAuth 제공자입니다. " + provider);
        }
    }

    private boolean hasPasswordLogin(Member member) {
        return member.getPassword() != null && !member.getPassword().isBlank();
    }

    private boolean canUnlink(boolean hasPasswordLogin, int oauthAccountCount) {
        return hasPasswordLogin || oauthAccountCount > 1;
    }
}

