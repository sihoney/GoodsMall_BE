package com.example.member.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "aws.s3") // application.yml에서 aws.s3로 시작하는 프로퍼티들을 이 클래스의 필드에 매핑
public class S3Properties {

    private String bucket;
    private String region;
    private String accessKey;
    private String secretKey;
    private String profileImagePrefix;
    private String publicBaseUrl;
    private long presignExpirationSeconds;
}
