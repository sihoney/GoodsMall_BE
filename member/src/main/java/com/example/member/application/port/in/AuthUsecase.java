package com.example.member.application.port.in;

import com.example.member.application.dto.command.LoginCommand;
import com.example.member.application.dto.command.TokenRefreshCommand;
import com.example.member.application.dto.result.AuthTokenResult;
import java.util.UUID;

public interface AuthUsecase {

    AuthTokenResult login(LoginCommand command);

    AuthTokenResult refresh(TokenRefreshCommand command);

    void logoutCurrentSession(String accessToken);

    void logoutAllSessions(String accessToken);

    void logout(UUID memberId);
}
