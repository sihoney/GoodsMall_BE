package com.todaylunch.gateway.config;

import java.util.List;
import java.util.Locale;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gateway.auth")
public record GatewayAuthProperties(
        boolean jwtValidationEnabled,
        List<PublicRule> publicRules,
        List<RoleRule> roleRules
) {
    public GatewayAuthProperties {
        publicRules = publicRules == null ? List.of() : publicRules.stream()
                .map(PublicRule::normalized)
                .toList();
        roleRules = roleRules == null ? List.of() : roleRules.stream()
                .map(RoleRule::normalized)
                .toList();
    }

    public record PublicRule(
            List<String> methods,
            String pattern
    ) {
        private PublicRule normalized() {
            List<String> normalizedMethods = methods == null ? List.of() : methods.stream()
                    .map(method -> method.toUpperCase(Locale.ROOT))
                    .toList();
            return new PublicRule(normalizedMethods, pattern);
        }
    }

    public record RoleRule(
            List<String> methods,
            String pattern,
            List<String> allowedRoles
    ) {
        private RoleRule normalized() {
            List<String> normalizedMethods = methods == null ? List.of() : methods.stream()
                    .map(method -> method.toUpperCase(Locale.ROOT))
                    .toList();
            List<String> normalizedAllowedRoles = allowedRoles == null ? List.of() : allowedRoles.stream()
                    .map(role -> role.toUpperCase(Locale.ROOT))
                    .toList();
            return new RoleRule(normalizedMethods, pattern, normalizedAllowedRoles);
        }
    }
}
