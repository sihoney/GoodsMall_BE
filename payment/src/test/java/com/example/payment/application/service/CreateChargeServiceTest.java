package com.example.payment.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.example.payment.application.dto.ChargeCreateCommand;
import com.example.payment.application.dto.ChargeCreateResult;
import com.example.payment.common.exception.InvalidChargeRequestException;
import com.example.payment.common.exception.WalletNotFoundException;
import com.example.payment.domain.entity.Charge;
import com.example.payment.domain.entity.Wallet;
import com.example.payment.domain.enumtype.ChargeStatus;
import com.example.payment.domain.repository.ChargeRepository;
import com.example.payment.domain.repository.WalletRepository;
import com.example.payment.domain.service.IdentifierGenerator;
import com.example.payment.domain.service.TimeProvider;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateChargeService 테스트")
class CreateChargeServiceTest {

    @Mock
    private ChargeRepository chargeRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private IdentifierGenerator identifierGenerator;

    @Mock
    private TimeProvider timeProvider;

    @InjectMocks
    private CreateChargeService createChargeService;

    private UUID memberId;
    private UUID walletId;
    private UUID chargeId;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        memberId = UUID.randomUUID();
        walletId = UUID.randomUUID();
        chargeId = UUID.randomUUID();
        now = LocalDateTime.of(2024, 1, 1, 12, 0, 0);
    }

    private BigDecimal amount(long value) {
        return BigDecimal.valueOf(value);
    }

    @Test
    @DisplayName("기존 지갑이 있을 때 충전 요청 시 charge가 PENDING으로 생성된다")
    void createCharge_existingWallet_createsChargeWithPendingStatus() {
        ChargeCreateCommand command = new ChargeCreateCommand(memberId, amount(10_000L));
        Wallet existingWallet = Wallet.create(walletId, memberId, amount(5_000L), now, now);

        given(timeProvider.now()).willReturn(now);
        given(walletRepository.findByMemberId(memberId)).willReturn(Optional.of(existingWallet));
        given(identifierGenerator.generateUuid()).willReturn(chargeId);
        given(chargeRepository.save(any(Charge.class))).willAnswer(invocation -> invocation.getArgument(0));

        ChargeCreateResult result = createChargeService.createCharge(command);

        assertThat(result.chargeStatus()).isEqualTo(ChargeStatus.PENDING);
        assertThat(result.chargeId()).isEqualTo(chargeId);
        assertThat(result.walletId()).isEqualTo(walletId);
        assertThat(result.amount()).isEqualTo(amount(10_000L));
    }

    @Test
    @DisplayName("지갑이 없을 때 충전 요청 시 WalletNotFoundException이 발생한다")
    void createCharge_noWallet_throwsWalletNotFoundException() {
        ChargeCreateCommand command = new ChargeCreateCommand(memberId, amount(10_000L));
        given(walletRepository.findByMemberId(memberId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> createChargeService.createCharge(command))
                .isInstanceOf(WalletNotFoundException.class)
                .hasMessageContaining("지갑 정보를 찾을 수 없습니다.");
        verify(walletRepository, never()).save(any(Wallet.class));
        verify(chargeRepository, never()).save(any(Charge.class));
    }

    @Test
    @DisplayName("pgOrderId는 'CHARGE-{chargeId}' 형식으로 생성된다")
    void createCharge_pgOrderIdFormat() {
        ChargeCreateCommand command = new ChargeCreateCommand(memberId, amount(10_000L));
        Wallet wallet = Wallet.create(walletId, memberId, amount(0L), now, now);

        given(timeProvider.now()).willReturn(now);
        given(walletRepository.findByMemberId(memberId)).willReturn(Optional.of(wallet));
        given(identifierGenerator.generateUuid()).willReturn(chargeId);
        given(chargeRepository.save(any(Charge.class))).willAnswer(invocation -> invocation.getArgument(0));

        ChargeCreateResult result = createChargeService.createCharge(command);

        assertThat(result.pgOrderId()).isEqualTo("CHARGE-" + chargeId);
    }

    @Test
    @DisplayName("memberId가 null이면 InvalidChargeRequestException이 발생한다")
    void createCharge_nullMemberId_throwsException() {
        ChargeCreateCommand command = new ChargeCreateCommand(null, amount(10_000L));

        assertThatThrownBy(() -> createChargeService.createCharge(command))
                .isInstanceOf(InvalidChargeRequestException.class)
                .hasMessageContaining("memberId is required.");
    }

    @Test
    @DisplayName("amount가 0이면 InvalidChargeRequestException이 발생한다")
    void createCharge_zeroAmount_throwsException() {
        ChargeCreateCommand command = new ChargeCreateCommand(memberId, amount(0L));

        assertThatThrownBy(() -> createChargeService.createCharge(command))
                .isInstanceOf(InvalidChargeRequestException.class)
                .hasMessageContaining("amount must be positive.");
    }

    @Test
    @DisplayName("amount가 음수이면 InvalidChargeRequestException이 발생한다")
    void createCharge_negativeAmount_throwsException() {
        ChargeCreateCommand command = new ChargeCreateCommand(memberId, amount(-1_000L));

        assertThatThrownBy(() -> createChargeService.createCharge(command))
                .isInstanceOf(InvalidChargeRequestException.class)
                .hasMessageContaining("amount must be positive.");
    }

}
