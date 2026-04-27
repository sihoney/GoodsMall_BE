package com.example.member.application.dto.result;

import java.util.List;

public record MemberOauthAccountListResult(
        List<MemberOauthAccountItemResult> accounts,
        boolean hasPasswordLogin,
        boolean canRemoveLastOauthAccount
) {
}
