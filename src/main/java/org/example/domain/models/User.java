package org.example.domain.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

@Getter
@Setter
public class User {
    private Long id;
    private String username;
    private String email;
    private UserRepresentation userRepresentation;
    @JsonProperty("access_token")
    protected String accessToken;
    @JsonProperty("expires_in")
    protected long expiresIn;
    private List<Post> posts;
    private String tokenType;
    private String idToken;
    private String scope;
    @JsonProperty("refresh_expires_in")
    protected long refreshExpiresIn;
    @JsonProperty("refresh_token")
    protected String refreshToken;
    private String firstName;
    private String lastName;
    private String role;
    private String password;
    private String keycloakId;
    private boolean emailVerified;
    private boolean enabled;

}
