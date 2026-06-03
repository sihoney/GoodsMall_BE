package com.example.member.auth.application.service.oauth.profile;

import com.example.member.auth.domain.enumtype.OAuthProvider;
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
            throw new IllegalArgumentException("UNSUPPORTED_OAUTH_PROVIDER");
        }
        return profileService;
    }
}
