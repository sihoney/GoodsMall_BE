package com.example.member.application.usecase;

import java.util.UUID;

import com.example.member.presentation.dto.LoginRequest;
import com.example.member.presentation.dto.LoginResponse;
import com.example.member.presentation.dto.TokenRefreshRequest;
import com.example.member.presentation.dto.TokenRefreshResponse;

public interface AuthUsecase {

	LoginResponse login(LoginRequest request);

	TokenRefreshResponse refresh(TokenRefreshRequest request);

	void logout(UUID memberId);
}
