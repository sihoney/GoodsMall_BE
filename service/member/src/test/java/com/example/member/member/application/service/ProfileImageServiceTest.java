package com.example.member.member.application.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.example.member.member.application.dto.command.ProfileImagePresignCommand;
import com.example.member.member.infrastructure.storage.s3.S3Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

class ProfileImageServiceTest {

    private ProfileImageService profileImageService;

    @BeforeEach
    void setUp() {
        S3Properties s3Properties = new S3Properties();
        s3Properties.setBucket("bucket");
        s3Properties.setRegion("ap-northeast-2");
        s3Properties.setProfileImagePrefix("members/profile");
        s3Properties.setPutPresignExpirationSeconds(300);
        s3Properties.setGetPresignExpirationSeconds(600);
        profileImageService = new ProfileImageService(mock(S3Presigner.class), s3Properties);
    }

    @Test
    void createPresignedUpload_unsupportedExtension_throwsException() {
        ProfileImagePresignCommand request = new ProfileImagePresignCommand("avatar.gif", "image/gif");

        assertThrows(IllegalArgumentException.class, () -> profileImageService.createPresignedUpload(request));
    }

    @Test
    void createPresignedUpload_mismatchedMimeType_throwsException() {
        ProfileImagePresignCommand request = new ProfileImagePresignCommand("avatar.png", "image/jpeg");

        assertThrows(IllegalArgumentException.class, () -> profileImageService.createPresignedUpload(request));
    }
}

