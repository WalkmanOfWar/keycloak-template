package com.example.keycloaktemplate.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Maps Keycloak roles from the JWT token to Spring Security GrantedAuthorities.
 *
 * Keycloak puts roles in two places inside the JWT:
 *   - realm_access.roles   — realm-level roles
 *   - resource_access.<client-id>.roles — client-level roles
 *
 * Both are extracted and prefixed with "ROLE_" so @PreAuthorize("hasRole('admin')") works.
 */
@Component
public class KeycloakJwtConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();

    @Value("${app.keycloak.client-id}")
    private String clientId;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = Stream.concat(
            defaultConverter.convert(jwt).stream(),
            extractKeycloakRoles(jwt).stream()
        ).collect(Collectors.toSet());

        // principal name taken from "preferred_username" claim
        String principalName = jwt.getClaimAsString("preferred_username");
        return new JwtAuthenticationToken(jwt, authorities, principalName);
    }

    private Collection<GrantedAuthority> extractKeycloakRoles(Jwt jwt) {
        Stream<String> realmRoles = realmRoles(jwt);
        Stream<String> clientRoles = clientRoles(jwt);

        return Stream.concat(realmRoles, clientRoles)
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
            .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private Stream<String> realmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess == null) return Stream.empty();
        List<String> roles = (List<String>) realmAccess.get("roles");
        return roles == null ? Stream.empty() : roles.stream();
    }

    @SuppressWarnings("unchecked")
    private Stream<String> clientRoles(Jwt jwt) {
        Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
        if (resourceAccess == null) return Stream.empty();
        Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get(clientId);
        if (clientAccess == null) return Stream.empty();
        List<String> roles = (List<String>) clientAccess.get("roles");
        return roles == null ? Stream.empty() : roles.stream();
    }
}
