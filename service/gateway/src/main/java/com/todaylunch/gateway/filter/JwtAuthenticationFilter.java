package com.todaylunch.gateway.filter;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.todaylunch.gateway.auth.AuthErrorCode;
import com.todaylunch.gateway.auth.AuthErrorResponse;
import com.todaylunch.gateway.auth.AuthException;
import com.todaylunch.gateway.auth.AuthenticatedPrincipal;
import com.todaylunch.gateway.auth.GatewayAuthProperties;
import com.todaylunch.gateway.auth.GatewayJwtValidator;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final String MEMBER_ID_HEADER = "X-Member-Id";
    private static final String MEMBER_ROLE_HEADER = "X-Member-Role";
    private static final String SESSION_ID_HEADER = "X-Session-Id";

    private final GatewayJwtValidator gatewayJwtValidator;
    private final GatewayAuthProperties gatewayAuthProperties;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(
        ServerWebExchange exchange,
        GatewayFilterChain chain
    ) {
        // [1] CORS preflight requests bypass authentication.
        HttpMethod requestMethod = exchange.getRequest().getMethod();
        if (requestMethod != null && HttpMethod.OPTIONS.equals(requestMethod)) {
            return chain.filter(exchange);
        }

        // [2] Non-API requests are outside this auth filter.
        String path = exchange.getRequest().getURI().getPath();
        String method = requestMethod == null ? null : requestMethod.name();
        if (!path.startsWith("/api/")) {
            return chain.filter(exchange);
        }

        // [3] Public APIs are forwarded without JWT validation.
        if (isPublic(method, path)) {
            return chain.filter(exchange);
        }

        // Local development escape hatch. Keep disabled unless explicitly needed.
        // if (!gatewayAuthProperties.jwtValidationEnabled()) {
        //     return chain.filter(exchange);
        // }

        // [4] Extract Bearer token from the Authorization header.
        String authorizationHeader = exchange.getRequest()
            .getHeaders()
            .getFirst(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, AuthErrorCode.UNAUTHORIZED);
        }

        try {
            // [5] Validate JWT and Redis session whitelist.
            String token = authorizationHeader.substring(7);
            AuthenticatedPrincipal principal = gatewayJwtValidator.validateAccessToken(token);

            // [6] Enforce gateway-level role rules.
            if (!isAllowed(method, path, principal.role())) {
                return forbidden(exchange);
            }

            // [7] Forward trusted identity headers to downstream services.
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
        } catch (AuthException exception) {
            // [8] Domain auth failures are returned as defined auth errors.
            log.warn("[JWT] validation failed: {} - {}", exception.getErrorCode(), exception.getMessage());
            return unauthorized(exchange, exception.getErrorCode());
        } catch (Exception exception) {
            // [9] Unexpected validation failures are treated as invalid token.
            log.warn("[JWT] validation failed: {} - {}", exception.getClass().getSimpleName(), exception.getMessage(), exception);
            return unauthorized(exchange, AuthErrorCode.INVALID_TOKEN);
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean isPublic(String method, String path) {
        // Role rules take precedence over public path configuration.
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
        // Match method/path role rules and compare them with the caller role.
        var matchedRules = gatewayAuthProperties.roleRules().stream()
                .filter(rule -> matchesMethod(rule.methods(), method) && antPathMatcher.match(rule.pattern(), path))
                .toList();

        if (matchedRules.isEmpty()) {
            return false;
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

    private Mono<Void> unauthorized(ServerWebExchange exchange, AuthErrorCode errorCode) {
        return writeErrorResponse(exchange, HttpStatus.UNAUTHORIZED, errorCode);
    }

    private Mono<Void> forbidden(ServerWebExchange exchange) {
        return writeErrorResponse(exchange, HttpStatus.FORBIDDEN, AuthErrorCode.ACCESS_DENIED);
    }

    private Mono<Void> writeErrorResponse(
            ServerWebExchange exchange,
            HttpStatus status,
            AuthErrorCode errorCode
    ) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        byte[] body;
        try {
            body = objectMapper.writeValueAsBytes(AuthErrorResponse.from(errorCode));
        } catch (JacksonException exception) {
            body = ("{\"code\":\"" + errorCode.name() + "\",\"message\":\""
                    + errorCode.getMessage() + "\"}")
                    .getBytes(StandardCharsets.UTF_8);
        }

        return exchange.getResponse().writeWith(Flux.just(
                exchange.getResponse().bufferFactory().wrap(body)
        ));
    }
}
