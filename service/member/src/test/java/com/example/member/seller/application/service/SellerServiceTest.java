package com.example.member.seller.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.member.seller.application.dto.command.SellerRegisterCommand;
import com.example.member.verification.application.dto.result.AccountVerificationSendResult;
import com.example.member.seller.application.dto.result.SellerResult;
import com.example.member.verification.application.port.in.AccountVerificationUsecase;
import com.example.member.common.exception.BusinessException;
import com.example.member.member.exception.MemberErrorCode;
import com.example.member.seller.exception.SellerErrorCode;
import com.example.member.member.domain.entity.Member;
import com.example.member.seller.domain.entity.Seller;
import com.example.member.member.domain.enumtype.MemberStatus;
import com.example.member.member.infrastructure.persistence.jpa.MemberJpaAdapter;
import com.example.member.seller.infrastructure.persistence.jpa.SellerJpaAdapter;
import com.todaylunch.common.security.auth.enumtype.MemberRole;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SellerServiceTest {

    @Mock
    private SellerJpaAdapter sellerPersistencePort;

    @Mock
    private MemberJpaAdapter memberPersistencePort;

    @Mock
    private AccountVerificationUsecase accountVerificationUsecase;

    @Test
    void registerSeller_success_delegatesToAccountVerification() {
        SellerService sellerService = new SellerService(sellerPersistencePort, memberPersistencePort, accountVerificationUsecase);
        UUID memberId = UUID.randomUUID();
        Member member = createMember(memberId);
        SellerRegisterCommand command = new SellerRegisterCommand("Kakao Bank", "123-456-7890");
        AccountVerificationSendResult verificationResult = new AccountVerificationSendResult(
                "av_test_session",
                "PENDING",
                "123-****-7890",
                "482931",
                LocalDateTime.now().plusMinutes(5),
                0,
                0
        );

        when(memberPersistencePort.findById(memberId)).thenReturn(Optional.of(member));
        when(sellerPersistencePort.existsByMemberId(memberId)).thenReturn(false);
        when(accountVerificationUsecase.createAccountVerification(any(UUID.class), any())).thenReturn(verificationResult);

        AccountVerificationSendResult response = sellerService.registerSeller(memberId, command);

        assertEquals("av_test_session", response.sessionId());
        assertEquals("PENDING", response.status());
        assertEquals("123-****-7890", response.maskedAccountNumber());
        assertEquals("482931", response.verificationCode());
        verify(accountVerificationUsecase).createAccountVerification(any(UUID.class), any());
        verify(sellerPersistencePort, never()).save(any());
    }

    @Test
    void registerSeller_duplicateSeller_throwsException() {
        SellerService sellerService = new SellerService(sellerPersistencePort, memberPersistencePort, accountVerificationUsecase);
        UUID memberId = UUID.randomUUID();
        when(memberPersistencePort.findById(memberId)).thenReturn(Optional.of(createMember(memberId)));
        when(sellerPersistencePort.existsByMemberId(memberId)).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> sellerService.registerSeller(memberId, new SellerRegisterCommand("Bank", "1234"))
        );
        assertEquals(SellerErrorCode.SELLER_ALREADY_REGISTERED, exception.getErrorCode());

        verify(accountVerificationUsecase, never()).createAccountVerification(any(), any());
    }

    @Test
    void registerSeller_memberNotFound_throwsException() {
        SellerService sellerService = new SellerService(sellerPersistencePort, memberPersistencePort, accountVerificationUsecase);
        UUID memberId = UUID.randomUUID();
        when(memberPersistencePort.findById(memberId)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> sellerService.registerSeller(memberId, new SellerRegisterCommand("Bank", "1234"))
        );
        assertEquals(MemberErrorCode.MEMBER_NOT_FOUND, exception.getErrorCode());

        verify(accountVerificationUsecase, never()).createAccountVerification(any(), any());
    }

    @Test
    void getCurrentSeller_success_returnsSellerResponse() {
        SellerService sellerService = new SellerService(sellerPersistencePort, memberPersistencePort, accountVerificationUsecase);
        UUID memberId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        LocalDateTime approvedAt = LocalDateTime.now();
        Seller seller = Seller.create(sellerId, memberId, "Kakao Bank", "123-456-7890", approvedAt);

        when(memberPersistencePort.findById(memberId)).thenReturn(Optional.of(createMember(memberId)));
        when(sellerPersistencePort.findByMemberId(memberId)).thenReturn(Optional.of(seller));

        SellerResult response = sellerService.getCurrentSeller(memberId);

        assertEquals(sellerId, response.sellerId());
        assertEquals(memberId, response.memberId());
        assertEquals("Kakao Bank", response.bankName());
        assertEquals("123-456-7890", response.account());
        assertEquals(approvedAt, response.approvedAt());
    }

    @Test
    void getCurrentSeller_sellerNotFound_throwsException() {
        SellerService sellerService = new SellerService(sellerPersistencePort, memberPersistencePort, accountVerificationUsecase);
        UUID memberId = UUID.randomUUID();

        when(memberPersistencePort.findById(memberId)).thenReturn(Optional.of(createMember(memberId)));
        when(sellerPersistencePort.findByMemberId(memberId)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> sellerService.getCurrentSeller(memberId)
        );
        assertEquals(SellerErrorCode.SELLER_NOT_FOUND, exception.getErrorCode());
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
