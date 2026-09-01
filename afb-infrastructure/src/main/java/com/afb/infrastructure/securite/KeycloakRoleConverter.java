package com.afb.infrastructure.securite;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Convertit les rôles Keycloak ("realm_access.roles") en autorités Spring
 * préfixées "ROLE_", pour que @PreAuthorize("hasRole('ADMIN')") fonctionne.
 */
public class KeycloakRoleConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    @SuppressWarnings("unchecked")
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Map<String, Object> realmAccess = (Map<String, Object>) jwt.getClaims().get("realm_access");

        Collection<GrantedAuthority> authorities = new ArrayList<>();
        if (realmAccess != null && realmAccess.get("roles") != null) {
            List<String> roles = (List<String>) realmAccess.get("roles");
            authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());
        }
        return new JwtAuthenticationToken(jwt, authorities);
    }
}