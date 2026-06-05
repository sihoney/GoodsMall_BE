package com.example.member.member.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "member.signup")
public record MemberSignupProperties(
        Boolean requireEmailVerification
) {

    public MemberSignupProperties {
        requireEmailVerification = requireEmailVerification == null || requireEmailVerification;
    }
}
