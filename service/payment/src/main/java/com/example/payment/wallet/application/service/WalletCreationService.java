package com.example.payment.wallet.application.service;

import com.example.payment.wallet.application.dto.CreateWalletCommand;
import com.example.payment.wallet.application.dto.CreateWalletResult;
import com.example.payment.wallet.application.usecase.CreateWalletUseCase;
import com.example.payment.wallet.domain.entity.Wallet;
import com.example.payment.common.common.exception.InvalidChargeRequestException;
import com.example.payment.wallet.domain.repository.WalletRepository;
import com.example.payment.common.domain.service.IdentifierGenerator;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
/**
 * ?뚯썝??payment wallet ?앹꽦 ?좎뒪耳?댁뒪瑜??대떦?쒕떎.
 * ?대? wallet???덉쑝硫?湲곗〈 ?뺣낫瑜?諛섑솚?섍퀬, ?놁쑝硫???wallet???앹꽦?쒕떎.
 */
public class WalletCreationService implements CreateWalletUseCase {

    private final WalletRepository walletRepository;
    private final IdentifierGenerator identifierGenerator;

    public WalletCreationService(
            WalletRepository walletRepository,
            IdentifierGenerator identifierGenerator
    ) {
        this.walletRepository = walletRepository;
        this.identifierGenerator = identifierGenerator;
    }

    @Override
    /**
     * memberId 湲곗? wallet 議댁옱 ?щ?瑜?癒쇱? ?뺤씤?????놁쓣 ?뚮쭔 ?앹꽦?쒕떎.
     * 以묐났 ?앹꽦 寃쎌웳??諛쒖깮?대룄 湲곗〈 wallet???ㅼ떆 議고쉶??硫깅벑?섍쾶 ?묐떟?쒕떎.
     */
    public CreateWalletResult createWallet(CreateWalletCommand command) {
        validateCommand(command);

        return walletRepository.findByMemberId(command.memberId())
                .map(wallet -> new CreateWalletResult(
                        wallet.getWalletId(),
                        wallet.getMemberId(),
                        wallet.getBalance(),
                        false
                ))
                .orElseGet(() -> createNewWallet(command));
    }

    /**
     * wallet???덈줈 ??ν븯?? ?숈떆 ?앹꽦?쇰줈 unique ?쒖빟??異⑸룎?섎㈃ 湲곗〈 wallet???ъ“?뚰빐 諛섑솚?쒕떎.
     */
    private CreateWalletResult createNewWallet(CreateWalletCommand command) {
        Wallet wallet = Wallet.create(
                identifierGenerator.generateUuid(),
                command.memberId(),
                java.math.BigDecimal.ZERO,
                command.createdAt(),
                command.createdAt()
        );
        Wallet savedWallet;
        try {
            savedWallet = walletRepository.save(wallet);
        } catch (DataIntegrityViolationException e) {
            Wallet existingWallet = walletRepository.findByMemberId(command.memberId())
                    .orElseThrow(() -> e);
            return new CreateWalletResult(
                    existingWallet.getWalletId(),
                    existingWallet.getMemberId(),
                    existingWallet.getBalance(),
                    false
            );
        }
        return new CreateWalletResult(
                savedWallet.getWalletId(),
                savedWallet.getMemberId(),
                savedWallet.getBalance(),
                true
        );
    }

    /**
     * wallet ?앹꽦???꾩슂??理쒖냼 ?낅젰留?寃利앺븳??
     */
    private void validateCommand(CreateWalletCommand command) {
        if (command.memberId() == null) {
            throw new InvalidChargeRequestException("?뚯썝 ID???꾩닔?낅땲??");
        }
        if (command.createdAt() == null) {
            throw new InvalidChargeRequestException("?앹꽦 ?쒓컖? ?꾩닔?낅땲??");
        }
    }
}
