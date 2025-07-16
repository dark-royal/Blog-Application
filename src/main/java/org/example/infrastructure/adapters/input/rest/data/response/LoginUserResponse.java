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
    @JsonProperty("scope")
    protected String scope;


}
