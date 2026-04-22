package com.example.member.application.service;

import com.example.member.application.usecase.MemberOauthAccountUsecase;
import com.example.member.common.exception.LastLoginMethodRemovalNotAllowedException;
import com.example.member.common.exception.MemberNotFoundException;
import com.example.member.common.exception.MemberOauthAccountNotFoundException;
import com.example.member.domain.entity.Member;
import com.example.member.domain.entity.MemberOauthAccount;
import com.example.member.domain.enumtype.OAuthProvider;
import com.example.member.infrastructure.repository.MemberOauthAccountRepository;
import com.example.member.infrastructure.repository.MemberRepository;
import com.example.member.presentation.dto.MemberOauthAccountListResponse;
import com.example.member.presentation.dto.MemberOauthAccountResponse;
import com.example.member.presentation.dto.MemberOauthAccountUnlinkResponse;
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

    private final MemberRepository memberRepository;
    private final MemberOauthAccountRepository memberOauthAccountRepository;

    @Override
    public MemberOauthAccountListResponse getCurrentMemberOauthAccounts(UUID memberId) {
        Member member = getMember(memberId);
        List<MemberOauthAccount> accounts = memberOauthAccountRepository.findAllByMemberId(memberId);
        boolean hasPasswordLogin = hasPasswordLogin(member);
        boolean canRemoveLastOauthAccount = hasPasswordLogin || accounts.size() > 1;
        boolean canUnlink = canUnlink(hasPasswordLogin, accounts.size());

        List<MemberOauthAccountResponse> responses = accounts.stream()
                .map(account -> MemberOauthAccountResponse.from(account, canUnlink))
                .toList();

        return new MemberOauthAccountListResponse(responses, hasPasswordLogin, canRemoveLastOauthAccount);
    }

    @Transactional
    @Override
    public MemberOauthAccountUnlinkResponse unlinkCurrentMemberOauthAccount(UUID memberId, String provider) {
        Member member = getMember(memberId);
        OAuthProvider oauthProvider = parseProvider(provider);
        MemberOauthAccount account = memberOauthAccountRepository.findByMemberIdAndProvider(memberId, oauthProvider)
                .orElseThrow(MemberOauthAccountNotFoundException::new);

        List<MemberOauthAccount> accounts = memberOauthAccountRepository.findAllByMemberId(memberId);
        if (!canUnlink(hasPasswordLogin(member), accounts.size())) {
            throw new LastLoginMethodRemovalNotAllowedException();
        }

        memberOauthAccountRepository.delete(account);
        return new MemberOauthAccountUnlinkResponse(true, oauthProvider.name());
    }

    private Member getMember(UUID memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(MemberNotFoundException::new);
    }

    private OAuthProvider parseProvider(String provider) {
        if (provider == null || provider.trim().isEmpty()) {
            throw new IllegalArgumentException("provider is required.");
        }

        try {
            return OAuthProvider.valueOf(provider.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unsupported oauth provider: " + provider);
        }
    }

    private boolean hasPasswordLogin(Member member) {
        return member.getPassword() != null && !member.getPassword().isBlank();
    }

    private boolean canUnlink(boolean hasPasswordLogin, int oauthAccountCount) {
        return hasPasswordLogin || oauthAccountCount > 1;
    }
}
