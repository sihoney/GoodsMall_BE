package com.example.member.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.member.application.event.MemberEventPublisher;
import com.example.member.common.exception.DuplicateMemberEmailException;
import com.example.member.domain.entity.Member;
import com.example.member.domain.enumtype.MemberRole;
import com.example.member.domain.enumtype.MemberStatus;
import com.example.member.infrastructure.repository.MemberRepository;
import com.example.member.presentation.dto.CreateMemberRequest;
import com.example.member.presentation.dto.CreateMemberResponse;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MemberEventPublisher memberEventPublisher;

    @InjectMocks
    private MemberService memberService;

    @Test
    void createMember_success_savesEncodedMemberAndPublishesEvent() {
        CreateMemberRequest request = new CreateMemberRequest(
                "member@test.com",
                "plain-password",
                "tester",
                "010-1111-2222",
                "Seoul",
                "https://image.test/profile.png",
                MemberRole.USER
        );

        when(memberRepository.existsByEmail("member@test.com")).thenReturn(false);
        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateMemberResponse response = memberService.createMember(request);

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(memberCaptor.capture());
        verify(memberEventPublisher).publishMemberSignedUp(memberCaptor.getValue());

        Member savedMember = memberCaptor.getValue();
        assertEquals("member@test.com", savedMember.getEmail());
        assertEquals("encoded-password", savedMember.getPassword());
        assertEquals("tester", savedMember.getNickname());
        assertEquals(MemberRole.USER, savedMember.getRole());
        assertEquals(MemberStatus.ACTIVE, savedMember.getStatus());
        assertEquals(savedMember.getMemberId(), response.memberId());
        assertEquals(savedMember.getNickname(), response.nickname());
    }

    @Test
    void createMember_duplicateEmail_throwsException() {
        CreateMemberRequest request = new CreateMemberRequest(
                "member@test.com",
                "plain-password",
                "tester",
                null,
                null,
                null,
                MemberRole.USER
        );

        when(memberRepository.existsByEmail("member@test.com")).thenReturn(true);

        assertThrows(DuplicateMemberEmailException.class, () -> memberService.createMember(request));

        verify(memberRepository, never()).save(any(Member.class));
        verify(memberEventPublisher, never()).publishMemberSignedUp(any(Member.class));
    }
}
