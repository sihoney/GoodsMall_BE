package com.example.member.auth.application.port.in;

import com.example.member.auth.application.dto.command.AuthSessionMetadata;
import com.example.member.auth.application.dto.command.LoginCommand;
import com.example.member.auth.application.dto.command.TokenRefreshCommand;
import com.example.member.auth.application.dto.result.AuthSessionListResult;
import com.example.member.auth.application.dto.result.AuthTokenResult;
import java.util.UUID;

public interface AuthUsecase {

    AuthTokenResult login(LoginCommand command, AuthSessionMetadata metadata);

    AuthTokenResult refresh(TokenRefreshCommand command, AuthSessionMetadata metadata);

    AuthSessionListResult getSessions(UUID memberId, UUID currentSessionId);

    void logoutSession(String accessToken, UUID memberId, UUID currentSessionId, UUID targetSessionId);

    void logoutCurrentSession(String accessToken);

    void logoutAllSessions(String accessToken);

    void logout(UUID memberId);
}
