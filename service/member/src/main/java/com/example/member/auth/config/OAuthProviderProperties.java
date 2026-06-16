package com.example.member.auth.config;

import java.time.Duration;

public interface OAuthProviderProperties {

    String frontendCallbackUrl();

    Duration stateTtl();
}
