package com.todaylunch.gateway.auth;

import java.util.UUID;

public interface SessionWhitelistStore {

    boolean hasSession(UUID memberId, UUID sessionId);
}

