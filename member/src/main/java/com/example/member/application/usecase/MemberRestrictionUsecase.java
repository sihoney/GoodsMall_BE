package com.example.member.application.usecase;

import com.example.member.presentation.dto.CreateMemberRestrictionRequest;
import com.example.member.presentation.dto.MemberRestrictionResponse;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import java.util.List;
import java.util.UUID;

public interface MemberRestrictionUsecase {

    MemberRestrictionResponse createRestriction(AuthenticatedMember authenticatedMember, CreateMemberRestrictionRequest request);

    MemberRestrictionResponse deactivateRestriction(AuthenticatedMember authenticatedMember, UUID restrictionId);

    List<MemberRestrictionResponse> getMemberRestrictions(AuthenticatedMember authenticatedMember, UUID memberId);
}
