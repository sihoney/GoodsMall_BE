package com.example.member.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "member.signup")
public record MemberSignupProperties(
        Boolean requireEmailVerification
) {

    public MemberSignupProperties {
        requireEmailVerification = requireEmailVerification == null || requireEmailVerification;
    }
}
