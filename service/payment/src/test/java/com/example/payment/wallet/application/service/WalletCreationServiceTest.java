package com.example.payment.wallet.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.payment.wallet.application.dto.CreateWalletCommand;
import com.example.payment.wallet.application.dto.CreateWalletResult;
import com.example.payment.common.exception.InvalidChargeRequestException;
import com.example.payment.wallet.domain.entity.Wallet;
import com.example.payment.wallet.domain.repository.WalletRepository;
import com.example.payment.common.domain.service.IdentifierGenerator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
@DisplayName("payment test")
class WalletCreationServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private IdentifierGenerator identifierGenerator;

    @InjectMocks
    private WalletCreationService walletCreationService;

    private BigDecimal amount(long value) {
        return BigDecimal.valueOf(value);
    }

    @Test
    @DisplayName("payment test")
    void createWallet_existingWallet_returnsExistingWallet() {
        UUID memberId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
        Wallet existingWallet = Wallet.create(walletId, memberId, amount(0L), now, now);

        given(walletRepository.findByMemberId(memberId)).willReturn(Optional.of(existingWallet));

        CreateWalletResult result = walletCreationService.createWallet(new CreateWalletCommand(memberId, now));

        assertThat(result.walletId()).isEqualTo(walletId);
        assertThat(result.memberId()).isEqualTo(memberId);
        assertThat(result.balance()).isEqualTo(amount(0L));
        assertThat(result.created()).isFalse();
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    @DisplayName("payment test")
    void createWallet_absentWallet_createsWallet() {
        UUID memberId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
        Wallet createdWallet = Wallet.create(walletId, memberId, amount(0L), now, now);

        given(walletRepository.findByMemberId(memberId)).willReturn(Optional.empty());
        given(identifierGenerator.generateUuid()).willReturn(walletId);
        given(walletRepository.save(any(Wallet.class))).willReturn(createdWallet);

        CreateWalletResult result = walletCreationService.createWallet(new CreateWalletCommand(memberId, now));

        assertThat(result.walletId()).isEqualTo(walletId);
        assertThat(result.memberId()).isEqualTo(memberId);
        assertThat(result.balance()).isEqualTo(amount(0L));
        assertThat(result.created()).isTrue();
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    @DisplayName("payment test")
    void createWallet_duplicateInsert_returnsExistingWallet() {
        UUID memberId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
        Wallet existingWallet = Wallet.create(walletId, memberId, amount(0L), now, now);

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
    @DisplayName("payment test")
    void createWallet_nullMemberId_throwsException() {
        LocalDateTime now = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

        assertThatThrownBy(() -> walletCreationService.createWallet(new CreateWalletCommand(null, now)))
                .isInstanceOf(InvalidChargeRequestException.class);
    }
}
