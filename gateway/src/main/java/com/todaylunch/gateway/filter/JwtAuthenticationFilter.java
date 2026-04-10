package com.todaylunch.gateway.filter;

import com.todaylunch.gateway.security.GatewayAuthProperties;
import com.todaylunch.gateway.security.GatewayJwtValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final String MEMBER_ID_HEADER = "X-Member-Id";
    private static final String MEMBER_ROLE_HEADER = "X-Member-Role";

    private final GatewayJwtValidator gatewayJwtValidator;
    private final GatewayAuthProperties gatewayAuthProperties;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(
        ServerWebExchange exchange,
        GatewayFilterChain chain
    ) {
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequest().getMethod().name())) {
            return chain.filter(exchange);
        }

        String path = exchange.getRequest().getURI().getPath();
        if (!path.startsWith("/api/")) {
            return chain.filter(exchange);
        }

        if (isPublic(path)) {
            return chain.filter(exchange);
        }

        if (!gatewayAuthProperties.jwtValidationEnabled()) {
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

    private boolean isPublic(String path) {
        return gatewayAuthProperties.publicPaths().stream()
                .anyMatch(pattern -> antPathMatcher.match(pattern, path));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
