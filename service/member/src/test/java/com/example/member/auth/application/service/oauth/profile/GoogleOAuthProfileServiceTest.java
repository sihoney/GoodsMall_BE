package com.example.member.auth.application.service.oauth.profile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.example.member.auth.application.dto.OAuthUserProfile;
import com.example.member.auth.domain.enumtype.OAuthProvider;
import com.example.member.auth.infrastructure.google.GoogleOAuthClient;
import com.example.member.auth.infrastructure.google.GoogleTokenResponse;
import com.example.member.auth.infrastructure.google.GoogleUserProfileResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GoogleOAuthProfileServiceTest {

    @Mock
    private GoogleOAuthClient googleOAuthClient;

    private GoogleOAuthProfileService googleOAuthProfileService;

    @BeforeEach
    void setUp() {
        googleOAuthProfileService = new GoogleOAuthProfileService(googleOAuthClient);
    }

    @Test
    void fetchOAuthUserProfile_whenGoogleProfileValid_returnsCommonProfile() {
        when(googleOAuthClient.exchangeCode("code")).thenReturn(new GoogleTokenResponse(
                "google-access-token",
                "Bearer",
                3600L,
                null,
                "openid email profile"
        ));
        when(googleOAuthClient.fetchUserProfile("google-access-token")).thenReturn(new GoogleUserProfileResponse(
                "google-sub",
                "member@test.com",
                true,
                "tester",
                "https://example.com/profile.png"
        ));

        OAuthUserProfile profile = googleOAuthProfileService.fetchOAuthUserProfile("code");

        assertEquals(OAuthProvider.GOOGLE, profile.provider());
        assertEquals("google-sub", profile.providerUserId());
        assertEquals("member@test.com", profile.email());
        assertEquals("tester", profile.nickname());
        assertEquals("https://example.com/profile.png", profile.profileImageUrl());
    }

    @Test
    void fetchOAuthUserProfile_whenGoogleEmailMissing_throwsException() {
        when(googleOAuthClient.exchangeCode("code")).thenReturn(new GoogleTokenResponse(
                "google-access-token",
                "Bearer",
                3600L,
                null,
                "openid email profile"
        ));
        when(googleOAuthClient.fetchUserProfile("google-access-token")).thenReturn(new GoogleUserProfileResponse(
                "google-sub",
                null,
                true,
                "tester",
                null
        ));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> googleOAuthProfileService.fetchOAuthUserProfile("code")
        );

        assertEquals("GOOGLE_OAUTH_EMAIL_REQUIRED", exception.getMessage());
    }

    @Test
    void fetchOAuthUserProfile_whenGoogleEmailNotVerified_throwsException() {
        when(googleOAuthClient.exchangeCode("code")).thenReturn(new GoogleTokenResponse(
                "google-access-token",
                "Bearer",
                3600L,
                null,
                "openid email profile"
        ));
        when(googleOAuthClient.fetchUserProfile("google-access-token")).thenReturn(new GoogleUserProfileResponse(
                "google-sub",
                "member@test.com",
                false,
                "tester",
                null
        ));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> googleOAuthProfileService.fetchOAuthUserProfile("code")
        );

        assertEquals("GOOGLE_OAUTH_EMAIL_NOT_VERIFIED", exception.getMessage());
    }
}
