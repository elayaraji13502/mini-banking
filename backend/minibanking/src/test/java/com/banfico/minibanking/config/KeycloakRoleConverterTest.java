package com.banfico.minibanking.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class KeycloakRoleConverterTest {

    private final KeycloakRoleConverter converter = new KeycloakRoleConverter();

    @Test
    void convertsRealmRolesWithoutDoublePrefix() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("realm_access", Map.of("roles", List.of("ROLE_ADMIN", "MAKER")))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .subject("demo-user")
                .build();

        Set<String> roles = converter.convert(jwt).stream()
                .map(GrantedAuthority::getAuthority)
                .collect(java.util.stream.Collectors.toSet());

        assertThat(roles)
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_MAKER");
    }

    @Test
    void convertsClientRolesFromResourceAccess() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("resource_access", Map.of(
                        "mini-banking-client", Map.of("roles", List.of("ADMIN"))
                ))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .subject("demo-user")
                .build();

        Set<String> roles = converter.convert(jwt).stream()
                .map(GrantedAuthority::getAuthority)
                .collect(java.util.stream.Collectors.toSet());

        assertThat(roles)
                .containsExactly("ROLE_ADMIN");
    }
}
