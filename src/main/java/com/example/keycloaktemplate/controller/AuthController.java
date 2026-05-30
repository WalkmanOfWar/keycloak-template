package com.example.keycloaktemplate.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Proxy endpoints for obtaining / refreshing / revoking Keycloak tokens.
 *
 * In production you usually let the frontend talk to Keycloak directly (PKCE flow).
 * These endpoints are useful for:
 *   - server-to-server (client_credentials grant)
 *   - mobile / backend-for-frontend patterns
 *   - development / testing without a frontend
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Value("${app.keycloak.server-url}")
    private String serverUrl;

    @Value("${app.keycloak.realm}")
    private String realm;

    @Value("${app.keycloak.client-id}")
    private String clientId;

    @Value("${app.keycloak.client-secret}")
    private String clientSecret;

    private final RestClient restClient = RestClient.create();

    /**
     * Password grant — useful for dev/testing only.
     * Disable in production and use PKCE from your frontend instead.
     */
    @PostMapping("/token")
    public ResponseEntity<Object> token(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        String tokenUrl = "%s/realms/%s/protocol/openid-connect/token".formatted(serverUrl, realm);

        String form = "grant_type=password"
            + "&client_id=" + clientId
            + "&client_secret=" + clientSecret
            + "&username=" + username
            + "&password=" + password;

        Object response = restClient.post()
            .uri(tokenUrl)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .body(form)
            .retrieve()
            .body(Object.class);

        return ResponseEntity.ok(response);
    }

    /**
     * Refresh a token using a refresh_token.
     */
    @PostMapping("/refresh")
    public ResponseEntity<Object> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refresh_token");

        String tokenUrl = "%s/realms/%s/protocol/openid-connect/token".formatted(serverUrl, realm);

        String form = "grant_type=refresh_token"
            + "&client_id=" + clientId
            + "&client_secret=" + clientSecret
            + "&refresh_token=" + refreshToken;

        Object response = restClient.post()
            .uri(tokenUrl)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .body(form)
            .retrieve()
            .body(Object.class);

        return ResponseEntity.ok(response);
    }

    /**
     * Revoke (logout) the given token on Keycloak side.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refresh_token");

        String logoutUrl = "%s/realms/%s/protocol/openid-connect/logout".formatted(serverUrl, realm);

        String form = "client_id=" + clientId
            + "&client_secret=" + clientSecret
            + "&refresh_token=" + refreshToken;

        restClient.post()
            .uri(logoutUrl)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .body(form)
            .retrieve()
            .toBodilessEntity();

        return ResponseEntity.noContent().build();
    }
}
