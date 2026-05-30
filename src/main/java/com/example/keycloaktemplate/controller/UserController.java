package com.example.keycloaktemplate.controller;

import com.example.keycloaktemplate.security.CurrentUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    /**
     * Returns the currently authenticated user's profile.
     * Any authenticated user can call this — no specific role required.
     */
    @GetMapping("/me")
    public ResponseEntity<CurrentUser> me(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(CurrentUser.from(jwt));
    }

    /**
     * Example endpoint restricted to users with the "admin" realm or client role.
     * Annotate further endpoints the same way.
     */
    @GetMapping("/admin-only")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<String> adminOnly() {
        return ResponseEntity.ok("You have the admin role.");
    }
}
