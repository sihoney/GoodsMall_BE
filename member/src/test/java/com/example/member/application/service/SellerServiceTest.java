package com.example.member.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.member.common.exception.MemberNotFoundException;
import com.example.member.common.exception.SellerAlreadyRegisteredException;
import com.example.member.common.exception.SellerNotFoundException;
import com.example.member.domain.entity.Member;
import com.example.member.domain.entity.Seller;
import com.example.member.domain.enumtype.MemberStatus;
import com.example.member.infrastructure.repository.MemberRepository;
import com.example.member.infrastructure.repository.SellerRepository;
import com.example.member.presentation.dto.SellerRegisterRequest;
import com.example.member.presentation.dto.SellerRegisterResponse;
import com.example.member.presentation.dto.SellerResponse;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import com.todaylunch.common.security.auth.enumtype.MemberRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SellerServiceTest {

    @Mock
    private SellerRepository sellerRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private SellerService sellerService;

    @Test
    void registerSeller_success_savesSeller() {
        UUID memberId = UUID.randomUUID();
        Member member = createMember(memberId);
        SellerRegisterRequest request = new SellerRegisterRequest("Kakao Bank", "123-456-7890");

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));
        when(sellerRepository.existsByMemberId(memberId)).thenReturn(false);
        when(sellerRepository.save(any(Seller.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SellerRegisterResponse response = sellerService.registerSeller(memberId, request);

        ArgumentCaptor<Seller> sellerCaptor = ArgumentCaptor.forClass(Seller.class);
        verify(sellerRepository).save(sellerCaptor.capture());
        Seller savedSeller = sellerCaptor.getValue();

        assertEquals(memberId, savedSeller.getMemberId());
        assertEquals("Kakao Bank", savedSeller.getBankName());
        assertEquals("123-456-7890", savedSeller.getAccount());
        assertEquals(savedSeller.getSellerId(), response.sellerId());
        assertEquals(savedSeller.getApprovedAt(), response.approvedAt());
    }

    @Test
    void registerSeller_duplicateSeller_throwsException() {
        UUID memberId = UUID.randomUUID();
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(createMember(memberId)));
        when(sellerRepository.existsByMemberId(memberId)).thenReturn(true);

        assertThrows(
                SellerAlreadyRegisteredException.class,
                () -> sellerService.registerSeller(memberId, new SellerRegisterRequest("Bank", "1234"))
        );

        verify(sellerRepository, never()).save(any(Seller.class));
    }

    @Test
    void registerSeller_memberNotFound_throwsException() {
        UUID memberId = UUID.randomUUID();
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        assertThrows(
                MemberNotFoundException.class,
                () -> sellerService.registerSeller(memberId, new SellerRegisterRequest("Bank", "1234"))
        );

        verify(sellerRepository, never()).save(any(Seller.class));
    }

    @Test
    void getCurrentSeller_success_returnsSellerResponse() {
        UUID memberId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        LocalDateTime approvedAt = LocalDateTime.now();
        Seller seller = Seller.create(sellerId, memberId, "Kakao Bank", "123-456-7890", approvedAt);

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(createMember(memberId)));
        when(sellerRepository.findByMemberId(memberId)).thenReturn(Optional.of(seller));

        SellerResponse response = sellerService.getCurrentSeller(memberId);

        assertEquals(sellerId, response.sellerId());
        assertEquals(memberId, response.memberId());
        assertEquals("Kakao Bank", response.bankName());
        assertEquals("123-456-7890", response.account());
        assertEquals(approvedAt, response.approvedAt());
    }

    @Test
    void getCurrentSeller_sellerNotFound_throwsException() {
        UUID memberId = UUID.randomUUID();

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(createMember(memberId)));
        when(sellerRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

        assertThrows(SellerNotFoundException.class, () -> sellerService.getCurrentSeller(memberId));
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
