package com.example.member.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.member.application.dto.command.CreateMemberRestrictionCommand;
import com.example.member.application.dto.result.MemberRestrictionResult;
import com.example.member.common.exception.DuplicateActiveRestrictionException;
import com.example.member.domain.entity.Member;
import com.example.member.domain.entity.MemberRestriction;
import com.example.member.domain.enumtype.MemberStatus;
import com.example.member.domain.enumtype.RestrictionType;
import com.example.member.infrastructure.persistence.jpa.MemberJpaAdapter;
import com.example.member.infrastructure.persistence.jpa.MemberRestrictionJpaAdapter;
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
    private MemberJpaAdapter memberPersistencePort;

    @Mock
    private MemberRestrictionJpaAdapter memberRestrictionPersistencePort;

    @InjectMocks
    private MemberRestrictionService memberRestrictionService;

    @Test
    void createRestriction_success_savesRestriction() {
        UUID memberId = UUID.randomUUID();
        AuthenticatedMember admin = new AuthenticatedMember(UUID.randomUUID(), MemberRole.ADMIN, UUID.randomUUID());
        CreateMemberRestrictionCommand command = new CreateMemberRestrictionCommand(
                memberId,
                "abuse",
                RestrictionType.LOGIN_BAN,
                24
        );

        when(memberPersistencePort.findById(memberId)).thenReturn(Optional.of(createMember(memberId)));
        when(memberRestrictionPersistencePort.existsActiveRestriction(any(), any(), any())).thenReturn(false);
        when(memberRestrictionPersistencePort.save(any(MemberRestriction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MemberRestrictionResult response = memberRestrictionService.createRestriction(admin, command);

        ArgumentCaptor<MemberRestriction> restrictionCaptor = ArgumentCaptor.forClass(MemberRestriction.class);
        verify(memberRestrictionPersistencePort).save(restrictionCaptor.capture());

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
        CreateMemberRestrictionCommand command = new CreateMemberRestrictionCommand(
                memberId,
                "abuse",
                RestrictionType.LOGIN_BAN,
                24
        );

        when(memberPersistencePort.findById(memberId)).thenReturn(Optional.of(createMember(memberId)));
        when(memberRestrictionPersistencePort.existsActiveRestriction(any(), any(), any())).thenReturn(true);

        assertThrows(DuplicateActiveRestrictionException.class,
                () -> memberRestrictionService.createRestriction(admin, command));

        verify(memberRestrictionPersistencePort, never()).save(any(MemberRestriction.class));
    }

    @Test
    void createRestriction_nonAdmin_throwsAccessDenied() {
        AuthenticatedMember user = new AuthenticatedMember(UUID.randomUUID(), MemberRole.USER, UUID.randomUUID());
        CreateMemberRestrictionCommand command = new CreateMemberRestrictionCommand(
                UUID.randomUUID(),
                "abuse",
                RestrictionType.LOGIN_BAN,
                24
        );

        assertThrows(AuthorizationDeniedException.class,
                () -> memberRestrictionService.createRestriction(user, command));
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
