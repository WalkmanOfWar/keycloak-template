package com.example.keycloaktemplate.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void me_returnsUserProfile_whenAuthenticated() throws Exception {
        mvc.perform(get("/api/user/me")
                .with(jwt().jwt(j -> j
                    .subject("test-user-id")
                    .claim("preferred_username", "john")
                    .claim("email", "john@example.com")
                )))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("john"))
            .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void me_returns401_whenNotAuthenticated() throws Exception {
        mvc.perform(get("/api/user/me"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void adminOnly_returns403_whenMissingRole() throws Exception {
        mvc.perform(get("/api/user/admin-only")
                .with(jwt()))   // valid JWT but no roles
            .andExpect(status().isForbidden());
    }

    @Test
    void adminOnly_returns200_whenHasAdminRole() throws Exception {
        mvc.perform(get("/api/user/admin-only")
                .with(jwt().authorities(
                    new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_admin")
                )))
            .andExpect(status().isOk());
    }
}
