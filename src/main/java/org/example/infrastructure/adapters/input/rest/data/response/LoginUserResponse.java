package org.example.infrastructure.adapters.input.rest.data.response;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class LoginUserResponse {
    @JsonProperty("access_token")
    protected String accessToken;
    @JsonProperty("expires_in")
    protected long expiresIn;
    @JsonProperty("refresh_expires_in")
    protected long refreshExpiresIn;
    @JsonProperty("refresh_token")
    protected String refreshToken;
    @JsonProperty("token_type")
    protected String tokenType;
    @JsonProperty("id_token")
    protected String idToken;
    @JsonProperty("not-before-policy")
    protected int notBeforePolicy;
    @JsonProperty("session_state")
    protected String sessionState;
    protected Map<String, Object> otherClaims = new HashMap();
    @JsonProperty("scope")
    protected String scope;
    @JsonProperty("error")
    protected String error;

}
