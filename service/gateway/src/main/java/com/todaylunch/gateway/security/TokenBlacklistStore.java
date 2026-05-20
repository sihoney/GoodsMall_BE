package com.todaylunch.gateway.security;

import java.util.UUID;

public interface TokenBlacklistStore {

    boolean isAccessTokenBlacklisted(String accessTokenId);

    boolean isSessionBlacklisted(UUID sessionId);
}
