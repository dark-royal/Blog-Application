package org.example.domain.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {
    private Long id;
    private String username;
    private String email;
    @JsonProperty("access_token")
    protected String accessToken;
    @JsonProperty("expires_in")
    protected long expiresIn;
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
