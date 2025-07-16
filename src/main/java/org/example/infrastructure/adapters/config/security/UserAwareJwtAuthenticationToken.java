package org.example.infrastructure.adapters.config.security;

import lombok.Getter;
import lombok.Setter;
import org.example.domain.models.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;

@Getter
@Setter
public class UserAwareJwtAuthenticationToken extends JwtAuthenticationToken {
    private final User user;

    public UserAwareJwtAuthenticationToken(Jwt jwt,
                                           Collection<? extends GrantedAuthority> authorities,
                                           User user) {
        // Fix: Use user.getEmail() or user.getUsername() as the principal name
        super(jwt, authorities, user.getEmail());
        this.user = user;
    }

    @Override
    public Object getPrincipal() {
        return this.user;
    }
}