package com.example.member.auth.application.port.in;

import com.example.member.auth.application.dto.command.TokenRefreshCommand;
import com.example.member.auth.application.dto.result.AuthTokenResult;

public interface AuthTokenRefreshUsecase {

    AuthTokenResult refresh(TokenRefreshCommand command);
}
