package com.example.member.common.config;

import java.time.Duration;

public interface OAuthProviderProperties {

    String frontendCallbackUrl();

    Duration stateTtl();

    Duration resultTtl();
}
