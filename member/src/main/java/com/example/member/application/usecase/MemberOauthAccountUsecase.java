package com.example.member.application.usecase;

import com.example.member.presentation.dto.MemberOauthAccountListResponse;
import com.example.member.presentation.dto.MemberOauthAccountUnlinkResponse;
import java.util.UUID;

public interface MemberOauthAccountUsecase {

    MemberOauthAccountListResponse getCurrentMemberOauthAccounts(UUID memberId);

    MemberOauthAccountUnlinkResponse unlinkCurrentMemberOauthAccount(UUID memberId, String provider);
}
