package com.example.member.auth.application.service.oauth.profile;

import com.example.member.auth.domain.enumtype.OAuthProvider;
import com.example.member.auth.exception.AuthErrorCode;
import com.example.member.common.exception.BusinessException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class OAuthProfileServiceRegistry {

    private final Map<OAuthProvider, OAuthProfileService> services;

    public OAuthProfileServiceRegistry(List<OAuthProfileService> profileServices) {
        Map<OAuthProvider, OAuthProfileService> registeredServices = new EnumMap<>(OAuthProvider.class);

        for (OAuthProfileService profileService : profileServices) {
            OAuthProfileService previous = registeredServices.put(profileService.provider(), profileService);
            if (previous != null) {
                throw new IllegalStateException("DUPLICATE_OAUTH_PROFILE_SERVICE");
            }
        }
        this.services = Map.copyOf(registeredServices);
    }

    public OAuthProfileService get(OAuthProvider provider) {
        OAuthProfileService profileService = services.get(provider);
        if (profileService == null) {
            throw new BusinessException(AuthErrorCode.UNSUPPORTED_OAUTH_PROVIDER);
        }
        return profileService;
    }
}
