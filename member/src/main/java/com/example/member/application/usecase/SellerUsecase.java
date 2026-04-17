package com.example.member.application.usecase;

import com.example.member.presentation.dto.SellerRegisterRequest;
import com.example.member.presentation.dto.AccountVerificationSendResponse;
import com.example.member.presentation.dto.SellerResponse;
import java.util.UUID;

public interface SellerUsecase {

    AccountVerificationSendResponse registerSeller(UUID memberId, SellerRegisterRequest request);

    SellerResponse getCurrentSeller(UUID memberId);
}
