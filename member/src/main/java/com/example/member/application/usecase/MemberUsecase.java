package com.example.member.application.usecase;

import java.util.UUID;

import com.example.member.presentation.dto.CreateMemberResponse;
import com.example.member.presentation.dto.CreateMemberRequest;
import com.example.member.presentation.dto.MemberResponse;
import com.example.member.presentation.dto.UpdateMemberRequest;

public interface MemberUsecase {
	CreateMemberResponse createMember(CreateMemberRequest request);

	MemberResponse updateMember(UUID memberId, UpdateMemberRequest request);

	MemberResponse getCurrentMember(UUID memberId);

	MemberResponse updateCurrentMember(UUID memberId, UpdateMemberRequest request);

	MemberResponse getMember(UUID memberId);
}
