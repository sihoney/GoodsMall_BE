package com.todaylunch.gateway.filter;

import com.todaylunch.gateway.security.GatewayJwtValidator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final String MEMBER_ID_HEADER = "X-Member-Id";
    private static final String MEMBER_ROLE_HEADER = "X-Member-Role";
    private static final List<String> PUBLIC_PATH_PREFIXES = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/refresh"
    );

    private final GatewayJwtValidator gatewayJwtValidator;

    @Override
    public Mono<Void> filter(
        ServerWebExchange exchange, 
        GatewayFilterChain chain
    ) {
        String path = exchange.getRequest().getURI().getPath();
        if (!path.startsWith("/api/v1/")) {
            return chain.filter(exchange);
        }

        if (isPublic(exchange)) {
            return chain.filter(exchange);
        }

        String authorizationHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        try {
            String token = authorizationHeader.substring(7);
            GatewayJwtValidator.AuthenticatedPrincipal principal = gatewayJwtValidator.validateAccessToken(token);
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .headers(headers -> {
                        headers.remove(MEMBER_ID_HEADER);
                        headers.remove(MEMBER_ROLE_HEADER);
                        headers.add(MEMBER_ID_HEADER, principal.memberId().toString());
                        headers.add(MEMBER_ROLE_HEADER, principal.role());
                    })
                    .build();
            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (Exception exception) {
            return unauthorized(exchange);
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean isPublic(ServerWebExchange exchange) {
        String path = exchange.getRequest().getURI().getPath();
        if ("POST".equalsIgnoreCase(exchange.getRequest().getMethod().name()) && "/api/v1/members".equals(path)) {
            return true;
        }
        return PUBLIC_PATH_PREFIXES.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
