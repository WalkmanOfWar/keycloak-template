package com.example.keycloaktemplate.security;

import lombok.Builder;
import lombok.Getter;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class CurrentUser {

    private final String id;
    private final String username;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final List<String> roles;

    @SuppressWarnings("unchecked")
    private static List<String> extractRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null) return List.of();
        Object roles = realmAccess.get("roles");
        if (roles instanceof List<?> list) return (List<String>) list;
        return List.of();
    }

    public static CurrentUser from(Jwt jwt) {
        return CurrentUser.builder()
            .id(jwt.getSubject())
            .username(jwt.getClaimAsString("preferred_username"))
            .email(jwt.getClaimAsString("email"))
            .firstName(jwt.getClaimAsString("given_name"))
            .lastName(jwt.getClaimAsString("family_name"))
            .roles(extractRoles(jwt))
            .build();
    }
}
