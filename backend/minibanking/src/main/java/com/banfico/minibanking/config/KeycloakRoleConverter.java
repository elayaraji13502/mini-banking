package com.banfico.minibanking.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class KeycloakRoleConverter
        implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(
            Jwt jwt
    ) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        addAuthoritiesFromRealmRoles(jwt, authorities);
        addAuthoritiesFromResourceAccessRoles(jwt, authorities);

        return authorities;
    }

    private void addAuthoritiesFromRealmRoles(
            Jwt jwt,
            List<GrantedAuthority> authorities
    ) {
        Map<String, Object> realmAccess =
                jwt.getClaim("realm_access");

        if (realmAccess == null) {
            return;
        }

        Object rolesObject = realmAccess.get("roles");
        addAuthoritiesFromRoleCollection(rolesObject, authorities);
    }

    private void addAuthoritiesFromResourceAccessRoles(
            Jwt jwt,
            List<GrantedAuthority> authorities
    ) {
        Map<String, Object> resourceAccess =
                jwt.getClaim("resource_access");

        if (resourceAccess == null) {
            return;
        }

        for (Object clientRoles : resourceAccess.values()) {
            if (!(clientRoles instanceof Map<?, ?> clientRoleMap)) {
                continue;
            }

            Object rolesObject = clientRoleMap.get("roles");
            addAuthoritiesFromRoleCollection(rolesObject, authorities);
        }
    }

    private void addAuthoritiesFromRoleCollection(
            Object rolesObject,
            List<GrantedAuthority> authorities
    ) {
        if (!(rolesObject instanceof Collection<?> roles)) {
            return;
        }

        for (Object role : roles) {
            if (!(role instanceof String roleName)) {
                continue;
            }

            String normalizedRole = normalizeRole(roleName);
            if (normalizedRole == null) {
                continue;
            }

            authorities.add(
                    new SimpleGrantedAuthority(
                            "ROLE_" + normalizedRole.toUpperCase()
                    )
            );
        }
    }

    private String normalizeRole(String roleName) {
        String normalized = Objects.requireNonNull(roleName)
                .trim();

        if (normalized.isEmpty()) {
            return null;
        }

        if (normalized.startsWith("ROLE_")) {
            normalized = normalized.substring(5);
        }

        return normalized;
    }
}