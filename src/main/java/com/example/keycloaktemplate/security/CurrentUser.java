package com.example.keycloaktemplate.security;

import lombok.Builder;
import lombok.Getter;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

@Getter
@Builder
public class CurrentUser {

    private final String id;           // Keycloak subject (UUID)
    private final String username;     // preferred_username
    private final String email;
    private final String firstName;
    private final String lastName;
    private final List<String> roles;

    public static CurrentUser from(Jwt jwt) {
        return CurrentUser.builder()
            .id(jwt.getSubject())
            .username(jwt.getClaimAsString("preferred_username"))
            .email(jwt.getClaimAsString("email"))
            .firstName(jwt.getClaimAsString("given_name"))
            .lastName(jwt.getClaimAsString("family_name"))
            .roles(List.of()) // roles are in SecurityContext — see @PreAuthorize
            .build();
    }
}
