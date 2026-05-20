package com.example.member.auth.application.port.in;

import com.example.member.auth.application.dto.result.MemberOauthAccountListResult;
import com.example.member.auth.application.dto.result.MemberOauthAccountUnlinkResult;
import java.util.UUID;

public interface MemberOauthAccountUsecase {

    MemberOauthAccountListResult getCurrentMemberOauthAccounts(UUID memberId);

    MemberOauthAccountUnlinkResult unlinkCurrentMemberOauthAccount(UUID memberId, String provider);
}
