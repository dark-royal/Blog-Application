package org.example.infrastructure.adapters.input.rest.data.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginUserRequest {
    private String email;
    private String password;
}
