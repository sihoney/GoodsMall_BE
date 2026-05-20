package com.example.member.auth.application.dto.result;

import java.util.List;

public record AuthSessionListResult(
        List<AuthSessionResult> sessions
) {
}
