package org.example.infrastructure.adapters.input.rest.data.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RegisterUserRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String username;
    private String role;
}
