package com.example.member.presentation.web.dto;

import com.example.member.application.dto.result.MemberOauthAccountListResult;
import java.util.List;

public record MemberOauthAccountListResponse(
        List<MemberOauthAccountResponse> accounts,
        boolean hasPasswordLogin,
        boolean canRemoveLastOauthAccount
) {
    public static MemberOauthAccountListResponse from(MemberOauthAccountListResult result) {
        return new MemberOauthAccountListResponse(
                result.accounts().stream()
                        .map(MemberOauthAccountResponse::from)
                        .toList(),
                result.hasPasswordLogin(),
                result.canRemoveLastOauthAccount()
        );
    }
}

