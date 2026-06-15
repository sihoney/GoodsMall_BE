package com.example.member.auth.application.port.in;

import com.example.member.auth.application.dto.result.AuthSessionListResult;
import java.util.UUID;

public interface AuthSessionUsecase {

    AuthSessionListResult getSessions(UUID memberId, UUID currentSessionId);

    void logoutSession(String accessToken, UUID memberId, UUID currentSessionId, UUID targetSessionId);

    void logoutCurrentSession(String accessToken);

    void logoutAllSessions(String accessToken);
}
