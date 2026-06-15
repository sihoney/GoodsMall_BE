package com.example.member.member.infrastructure.storage.s3;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

// S3Presigner는 AWS SDK v2에서 제공하는 S3 사전 서명 URL 생성을 위한 클라이언트입니다.
@Configuration
public class S3Config {

    @Bean
    public S3Presigner s3Presigner(S3Properties s3Properties) {
        return S3Presigner.builder()
                .region(Region.of(s3Properties.getRegion()))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        s3Properties.getAccessKey(),
                                        s3Properties.getSecretKey()
                                )
                        )
                )
                .build();
    }
}
