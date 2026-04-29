package com.example.member.application.port.in;

import com.example.member.application.dto.command.AuthSessionMetadata;
import com.example.member.application.dto.command.LoginCommand;
import com.example.member.application.dto.command.TokenRefreshCommand;
import com.example.member.application.dto.result.AuthSessionListResult;
import com.example.member.application.dto.result.AuthTokenResult;
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
