package com.example.member.auth.application.service.oauth.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.member.auth.application.dto.OAuthUserProfile;
import com.example.member.auth.domain.enumtype.OAuthProvider;
import java.util.List;
import org.junit.jupiter.api.Test;

class OAuthProfileServiceRegistryTest {

    @Test
    void get_whenProviderServiceExists_returnsProfileService() {
        OAuthProfileService kakaoProfileService = new FakeOAuthProfileService(OAuthProvider.KAKAO);
        OAuthProfileServiceRegistry registry = new OAuthProfileServiceRegistry(List.of(kakaoProfileService));

        OAuthProfileService result = registry.get(OAuthProvider.KAKAO);

        assertEquals(kakaoProfileService, result);
    }

    @Test
    void get_whenProviderServiceMissing_throwsException() {
        OAuthProfileServiceRegistry registry = new OAuthProfileServiceRegistry(List.of(
                new FakeOAuthProfileService(OAuthProvider.KAKAO)
        ));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> registry.get(OAuthProvider.GOOGLE)
        );

        assertEquals("UNSUPPORTED_OAUTH_PROVIDER", exception.getMessage());
    }

    @Test
    void constructor_whenProviderServiceDuplicated_throwsException() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> new OAuthProfileServiceRegistry(List.of(
                        new FakeOAuthProfileService(OAuthProvider.KAKAO),
                        new FakeOAuthProfileService(OAuthProvider.KAKAO)
                ))
        );

        assertEquals("DUPLICATE_OAUTH_PROFILE_SERVICE", exception.getMessage());
    }

    private record FakeOAuthProfileService(OAuthProvider provider) implements OAuthProfileService {

        @Override
        public String buildAuthorizeUrl(String state) {
            return "https://oauth.example.com?state=" + state;
        }

        @Override
        public OAuthUserProfile fetchOAuthUserProfile(String code) {
            return new OAuthUserProfile(provider, code, null, null, null);
        }
    }
}
