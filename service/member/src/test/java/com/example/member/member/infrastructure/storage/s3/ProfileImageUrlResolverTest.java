package com.example.member.member.infrastructure.storage.s3;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.member.common.exception.BusinessException;
import com.example.member.member.exception.MemberErrorCode;
import org.junit.jupiter.api.Test;

class ProfileImageUrlResolverTest {

    @Test
    void resolve_whenObjectKeyIsBlank_returnsNull() {
        ProfileImageUrlResolver resolver = new ProfileImageUrlResolver(null, s3Properties());

        assertNull(resolver.resolve(" "));
    }

    @Test
    void resolve_whenObjectKeyIsNotProfileImageKey_throwsBusinessException() {
        ProfileImageUrlResolver resolver = new ProfileImageUrlResolver(null, s3Properties());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> resolver.resolve("members/other/image.png")
        );

        assertEquals(MemberErrorCode.INVALID_PROFILE_IMAGE_KEY, exception.getErrorCode());
    }

    private S3Properties s3Properties() {
        S3Properties properties = new S3Properties();
        properties.setBucket("test-bucket");
        properties.setProfileImagePrefix("members/profile");
        properties.setGetPresignExpirationSeconds(60);
        return properties;
    }
}
