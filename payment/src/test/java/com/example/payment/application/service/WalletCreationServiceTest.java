package com.example.payment.application.service;

import com.example.payment.application.dto.CreateWalletCommand;
import com.example.payment.application.dto.CreateWalletResult;
import com.example.payment.domain.entity.Wallet;
import com.example.payment.common.exception.InvalidChargeRequestException;
import com.example.payment.domain.repository.WalletRepository;
import com.example.payment.domain.service.IdentifierGenerator;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.dao.DataIntegrityViolationException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("WalletCreationService 테스트")
class WalletCreationServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private IdentifierGenerator identifierGenerator;

    @InjectMocks
    private WalletCreationService walletCreationService;

    @Test
    @DisplayName("기존 wallet가 있으면 새로 생성하지 않고 기존 wallet를 반환한다")
    void createWallet_existingWallet_returnsExistingWallet() {
        UUID memberId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
        Wallet existingWallet = Wallet.create(walletId, memberId, 0L, now, now);

        given(walletRepository.findByMemberId(memberId)).willReturn(Optional.of(existingWallet));

        CreateWalletResult result = walletCreationService.createWallet(new CreateWalletCommand(memberId, now));

        assertThat(result.walletId()).isEqualTo(walletId);
        assertThat(result.memberId()).isEqualTo(memberId);
        assertThat(result.balance()).isEqualTo(0L);
        assertThat(result.created()).isFalse();
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    @DisplayName("wallet가 없으면 0원 wallet를 생성한다")
    void createWallet_absentWallet_createsWallet() {
        UUID memberId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
        Wallet createdWallet = Wallet.create(walletId, memberId, 0L, now, now);

        given(walletRepository.findByMemberId(memberId)).willReturn(Optional.empty());
        given(identifierGenerator.generateUuid()).willReturn(walletId);
        given(walletRepository.save(any(Wallet.class))).willReturn(createdWallet);

        CreateWalletResult result = walletCreationService.createWallet(new CreateWalletCommand(memberId, now));

        assertThat(result.walletId()).isEqualTo(walletId);
        assertThat(result.memberId()).isEqualTo(memberId);
        assertThat(result.balance()).isEqualTo(0L);
        assertThat(result.created()).isTrue();
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    @DisplayName("중복 생성 경쟁으로 unique 제약 예외가 나면 기존 wallet를 반환한다")
    void createWallet_duplicateInsert_returnsExistingWallet() {
        UUID memberId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
        Wallet existingWallet = Wallet.create(walletId, memberId, 0L, now, now);

        given(walletRepository.findByMemberId(memberId))
                .willReturn(Optional.empty())
                .willReturn(Optional.of(existingWallet));
        given(identifierGenerator.generateUuid()).willReturn(UUID.randomUUID());
        given(walletRepository.save(any(Wallet.class))).willThrow(new DataIntegrityViolationException("duplicate"));

        CreateWalletResult result = walletCreationService.createWallet(new CreateWalletCommand(memberId, now));

        assertThat(result.walletId()).isEqualTo(walletId);
        assertThat(result.created()).isFalse();
    }

    @Test
    @DisplayName("memberId가 없으면 예외가 발생한다")
    void createWallet_nullMemberId_throwsException() {
        LocalDateTime now = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

        assertThatThrownBy(() -> walletCreationService.createWallet(new CreateWalletCommand(null, now)))
                .isInstanceOf(InvalidChargeRequestException.class)
                .hasMessageContaining("memberId is required.");
    }
}
