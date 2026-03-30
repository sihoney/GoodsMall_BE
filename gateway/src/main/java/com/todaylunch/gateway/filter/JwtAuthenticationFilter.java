package com.todaylunch.gateway.filter;

import com.todaylunch.gateway.security.GatewayJwtValidator;
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
    private static final String PUBLIC_AUTH_PREFIX = "/api/auth";

    private final GatewayJwtValidator gatewayJwtValidator;

    @Override
    public Mono<Void> filter(
        ServerWebExchange exchange,
        GatewayFilterChain chain
    ) {
        // OPTIONS 요청과 /api/ 이하가 아닌 경로는 
        // 인증 필터를 적용하지 않고 바로 다음 필터로 전달
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequest().getMethod().name())) {
            return chain.filter(exchange);
        }
        
        // /api/ 이하의 경로에 대해서만 인증 필터 적용
        String path = exchange.getRequest().getURI().getPath();
        if (!path.startsWith("/api/")) {
            return chain.filter(exchange);
        }

        // 인증이 필요 없는 경로는 필터를 적용하지 않고 다음 필터로 전달
        if (isPublic(exchange)) {
            return chain.filter(exchange);
        }

        // Authorization 헤더에서 Bearer 토큰 추출
        String authorizationHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        try {
            String token = authorizationHeader.substring(7);
            GatewayJwtValidator.AuthenticatedPrincipal principal = gatewayJwtValidator.validateAccessToken(token); // 토큰 검증 및 인증 정보 추출
            // 인증 정보가 유효한 경우, 원래 요청에 사용자 ID와 역할 정보를 헤더에 추가하여 다음 필터로 전달
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
        return path.startsWith(PUBLIC_AUTH_PREFIX);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
