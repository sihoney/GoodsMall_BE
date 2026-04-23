package com.example.member.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.member.common.exception.DuplicateActiveRestrictionException;
import com.example.member.domain.entity.Member;
import com.example.member.domain.entity.MemberRestriction;
import com.example.member.domain.enumtype.MemberStatus;
import com.example.member.domain.enumtype.RestrictionType;
import com.example.member.infrastructure.repository.MemberRepository;
import com.example.member.infrastructure.repository.MemberRestrictionRepository;
import com.example.member.presentation.dto.CreateMemberRestrictionRequest;
import com.example.member.presentation.dto.MemberRestrictionResponse;
import com.todaylunch.common.security.auth.dto.AuthenticatedMember;
import com.todaylunch.common.security.auth.enumtype.MemberRole;
import com.todaylunch.common.security.exception.AuthorizationDeniedException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberRestrictionServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberRestrictionRepository memberRestrictionRepository;

    @InjectMocks
    private MemberRestrictionService memberRestrictionService;

    @Test
    void createRestriction_success_savesRestriction() {
        UUID memberId = UUID.randomUUID();
        AuthenticatedMember admin = new AuthenticatedMember(UUID.randomUUID(), MemberRole.ADMIN, UUID.randomUUID());
        CreateMemberRestrictionRequest request = new CreateMemberRestrictionRequest(
                memberId,
                "abuse",
                RestrictionType.LOGIN_BAN,
                24
        );

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(createMember(memberId)));
        when(memberRestrictionRepository.existsActiveRestriction(any(), any(), any())).thenReturn(false);
        when(memberRestrictionRepository.save(any(MemberRestriction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MemberRestrictionResponse response = memberRestrictionService.createRestriction(admin, request);

        ArgumentCaptor<MemberRestriction> restrictionCaptor = ArgumentCaptor.forClass(MemberRestriction.class);
        verify(memberRestrictionRepository).save(restrictionCaptor.capture());

        MemberRestriction savedRestriction = restrictionCaptor.getValue();
        assertEquals(memberId, savedRestriction.getMemberId());
        assertEquals(admin.memberId(), savedRestriction.getAdminId());
        assertEquals(RestrictionType.LOGIN_BAN, savedRestriction.getRestrictionType());
        assertEquals(savedRestriction.getRestrictionId(), response.restrictionId());
    }

    @Test
    void createRestriction_duplicateActiveRestriction_throwsException() {
        UUID memberId = UUID.randomUUID();
        AuthenticatedMember admin = new AuthenticatedMember(UUID.randomUUID(), MemberRole.ADMIN, UUID.randomUUID());
        CreateMemberRestrictionRequest request = new CreateMemberRestrictionRequest(
                memberId,
                "abuse",
                RestrictionType.LOGIN_BAN,
                24
        );

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(createMember(memberId)));
        when(memberRestrictionRepository.existsActiveRestriction(any(), any(), any())).thenReturn(true);

        assertThrows(DuplicateActiveRestrictionException.class,
                () -> memberRestrictionService.createRestriction(admin, request));

        verify(memberRestrictionRepository, never()).save(any(MemberRestriction.class));
    }

    @Test
    void createRestriction_nonAdmin_throwsAccessDenied() {
        AuthenticatedMember user = new AuthenticatedMember(UUID.randomUUID(), MemberRole.USER, UUID.randomUUID());
        CreateMemberRestrictionRequest request = new CreateMemberRestrictionRequest(
                UUID.randomUUID(),
                "abuse",
                RestrictionType.LOGIN_BAN,
                24
        );

        assertThrows(AuthorizationDeniedException.class,
                () -> memberRestrictionService.createRestriction(user, request));
    }

    private Member createMember(UUID memberId) {
        LocalDateTime now = LocalDateTime.now();
        return Member.create(
                memberId,
                "member@test.com",
                "encoded-password",
                "tester",
                null,
                null,
                null,
                MemberRole.USER,
                MemberStatus.ACTIVE,
                now,
                now
        );
    }
}
