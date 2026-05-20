package com.example.member.application.port.in;

import com.example.member.application.dto.command.SellerRegisterCommand;
import com.example.member.application.dto.result.AccountVerificationSendResult;
import com.example.member.application.dto.result.SellerResult;
import java.util.UUID;

public interface SellerUsecase {

    AccountVerificationSendResult registerSeller(UUID memberId, SellerRegisterCommand command);

    SellerResult getCurrentSeller(UUID memberId);
}
