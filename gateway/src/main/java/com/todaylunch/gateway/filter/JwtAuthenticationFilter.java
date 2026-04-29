package com.todaylunch.gateway.filter;

import com.todaylunch.gateway.security.GatewayAuthProperties;
import com.todaylunch.gateway.security.GatewayJwtValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
//변경감지
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final String MEMBER_ID_HEADER = "X-Member-Id";
    private static final String MEMBER_ROLE_HEADER = "X-Member-Role";
    private static final String SESSION_ID_HEADER = "X-Session-Id";

    private final GatewayJwtValidator gatewayJwtValidator;
    private final GatewayAuthProperties gatewayAuthProperties;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(
        ServerWebExchange exchange,
        GatewayFilterChain chain
    ) {
        HttpMethod requestMethod = exchange.getRequest().getMethod();
        if (requestMethod != null && HttpMethod.OPTIONS.equals(requestMethod)) {
            return chain.filter(exchange);
        }

        String path = exchange.getRequest().getURI().getPath();
        String method = requestMethod == null ? null : requestMethod.name();
        if (!path.startsWith("/api/")) {
            return chain.filter(exchange);
        }

        if (isPublic(method, path)) {
            return chain.filter(exchange);
        }

        if (!gatewayAuthProperties.jwtValidationEnabled()) {
            return chain.filter(exchange);
        }

        String authorizationHeader = exchange.getRequest()
            .getHeaders()
            .getFirst(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        try {
            String token = authorizationHeader.substring(7);
            GatewayJwtValidator.AuthenticatedPrincipal principal = gatewayJwtValidator.validateAccessToken(token);
            if (!isAllowed(method, path, principal.role())) {
                return forbidden(exchange);
            }

            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .headers(headers -> {
                        headers.remove(MEMBER_ID_HEADER);
                        headers.remove(MEMBER_ROLE_HEADER);
                        headers.remove(SESSION_ID_HEADER);
                        headers.add(MEMBER_ID_HEADER, principal.memberId().toString());
                        headers.add(MEMBER_ROLE_HEADER, principal.role());
                        headers.add(SESSION_ID_HEADER, principal.sessionId().toString());
                    })
                    .build();
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (Exception exception) {
            log.warn("[JWT] validation failed: {} - {}", exception.getClass().getSimpleName(), exception.getMessage(), exception);
            return unauthorized(exchange);
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean isPublic(String method, String path) {
        boolean hasMatchingRoleRule = gatewayAuthProperties.roleRules().stream()
                .anyMatch(rule -> matchesMethod(rule.methods(), method)
                        && antPathMatcher.match(rule.pattern(), path));
        if (hasMatchingRoleRule) {
            return false;
        }

        boolean matchedPublicPath = gatewayAuthProperties.publicPaths().stream()
                .anyMatch(pattern -> antPathMatcher.match(pattern, path));

        if (matchedPublicPath) {
            return true;
        }

        return gatewayAuthProperties.publicRules().stream()
                .anyMatch(rule -> matchesMethod(rule.methods(), method) && antPathMatcher.match(rule.pattern(), path));
    }

    private boolean isAllowed(String method, String path, String role) {
        var matchedRules = gatewayAuthProperties.roleRules().stream()
                .filter(rule -> matchesMethod(rule.methods(), method) && antPathMatcher.match(rule.pattern(), path))
                .toList();

        if (matchedRules.isEmpty()) {
            return false; // 명시된 규칙이 없는 경우 접근 금지 (명시적 거부 정책, rule 미매칭 보호 API의 경우)
        }

        return matchedRules.stream()
                .anyMatch(rule -> rule.allowedRoles().stream()
                        .anyMatch(allowedRole -> allowedRole.equalsIgnoreCase(role)));
    }

    private boolean matchesMethod(Iterable<String> methods, String requestMethod) {
        if (requestMethod == null) {
            return false;
        }

        for (String method : methods) {
            if (requestMethod.equalsIgnoreCase(method)) {
                return true;
            }
        }
        return false;
    }

    // 401 Unauthorized
    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    // 403 Forbidden
    private Mono<Void> forbidden(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        return exchange.getResponse().setComplete();
    }
}
