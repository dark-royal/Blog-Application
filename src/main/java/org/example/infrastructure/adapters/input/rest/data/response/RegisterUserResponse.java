package org.example.infrastructure.adapters.input.rest.data.response;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RegisterUserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String message;
    private String username;
    private String role;
}
