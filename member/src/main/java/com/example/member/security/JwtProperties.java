package com.example.member.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

// Spring이 application.yml의 jwt 설정을 읽어서 만들어 주는 설정 객체
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secret,
        String issuer,
        long accessExpiration,
        long refreshExpiration
) {
}
