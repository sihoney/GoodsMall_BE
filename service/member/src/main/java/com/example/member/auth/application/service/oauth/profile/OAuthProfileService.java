package com.example.member.auth.application.service.oauth.profile;

import com.example.member.auth.application.dto.OAuthUserProfile;
import com.example.member.auth.domain.enumtype.OAuthProvider;

public interface OAuthProfileService {

    OAuthProvider provider();

    String buildAuthorizeUrl(String state);

    OAuthUserProfile fetchOAuthUserProfile(String code);
}
