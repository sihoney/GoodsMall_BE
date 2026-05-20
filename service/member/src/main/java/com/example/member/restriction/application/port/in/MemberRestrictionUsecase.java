package com.example.member.restriction.application.port.in;

import com.example.member.restriction.application.dto.command.CreateMemberRestrictionCommand;
import com.example.member.restriction.application.dto.result.MemberRestrictionResult;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import java.util.List;
import java.util.UUID;

public interface MemberRestrictionUsecase {

    MemberRestrictionResult createRestriction(
            AuthenticatedMember authenticatedMember,
            CreateMemberRestrictionCommand command
    );

    MemberRestrictionResult deactivateRestriction(AuthenticatedMember authenticatedMember, UUID restrictionId);

    List<MemberRestrictionResult> getAllMemberRestrictions(AuthenticatedMember authenticatedMember);

    List<MemberRestrictionResult> getMemberRestrictions(AuthenticatedMember authenticatedMember, UUID memberId);
}
