package com.todaylunch.gateway.auth;

import java.util.UUID;

public record AuthenticatedPrincipal(UUID memberId, String role, UUID sessionId) {
}
