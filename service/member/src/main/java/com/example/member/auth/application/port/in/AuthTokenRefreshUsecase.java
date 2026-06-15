package com.example.member.auth.application.port.in;

import com.example.member.auth.application.dto.command.TokenRefreshCommand;
import com.example.member.auth.application.dto.result.AuthTokenResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface AuthTokenRefreshUsecase {

    AuthTokenResult refresh(@Valid @NotNull TokenRefreshCommand command);
}
