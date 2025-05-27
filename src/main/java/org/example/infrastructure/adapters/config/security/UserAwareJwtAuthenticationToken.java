package org.example.infrastructure.adapters.config.security;

import org.example.domain.models.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;

public class UserAwareJwtAuthenticationToken extends JwtAuthenticationToken {

    private final User user;

    public UserAwareJwtAuthenticationToken(Jwt jwt, Collection<? extends GrantedAuthority> authorities, User user) {
        super(jwt, authorities, user.getEmail());
        this.user = user;
    }

    @Override
    public Object getPrincipal() {
        return this.user;
    }
}
