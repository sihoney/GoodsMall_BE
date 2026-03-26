package com.todaylunch.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        String routeId = route == null ? "unmatched" : route.getId();

        log.info(
                "[Gateway Request] method={} path={} route={}",
                request.getMethod(),
                request.getURI().getPath(),
                routeId
        );

        return chain.filter(exchange)
                .doOnSuccess(ignored -> log.info(
                        "[Gateway Response] method={} path={} route={} status={}",
                        request.getMethod(),
                        request.getURI().getPath(),
                        routeId,
                        exchange.getResponse().getStatusCode()
                ))
                .doOnError(exception -> log.error(
                        "[Gateway Error] method={} path={} route={} message={}",
                        request.getMethod(),
                        request.getURI().getPath(),
                        routeId,
                        exception.getMessage(),
                        exception
                ));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
