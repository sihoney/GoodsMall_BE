package com.todaylunch.gateway.security;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gateway.auth")
public record GatewayAuthProperties(
        boolean jwtValidationEnabled,
        List<String> publicPaths
) {
    public GatewayAuthProperties {
        publicPaths = publicPaths == null ? List.of() : List.copyOf(publicPaths);
    }
}
