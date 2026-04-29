package com.example.member.application.dto.result;

import java.util.List;

public record AuthSessionListResult(
        List<AuthSessionResult> sessions
) {
}
