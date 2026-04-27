package com.example.member.application.port.in;

import com.example.member.application.dto.result.MemberOauthAccountListResult;
import com.example.member.application.dto.result.MemberOauthAccountUnlinkResult;
import java.util.UUID;

public interface MemberOauthAccountUsecase {

    MemberOauthAccountListResult getCurrentMemberOauthAccounts(UUID memberId);

    MemberOauthAccountUnlinkResult unlinkCurrentMemberOauthAccount(UUID memberId, String provider);
}
