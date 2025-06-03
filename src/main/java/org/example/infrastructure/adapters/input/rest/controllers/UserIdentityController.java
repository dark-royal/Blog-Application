package org.example.infrastructure.adapters.input.rest.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.example.application.port.input.LoginUseCase;
import org.example.application.port.input.LogoutUseCase;
import org.example.application.port.input.SignUpUseCase;
import org.example.domain.exceptions.*;
import org.example.domain.models.User;
import org.example.infrastructure.adapters.input.rest.data.request.LoginUserRequest;
import org.example.infrastructure.adapters.input.rest.data.request.RegisterUserRequest;
import org.example.infrastructure.adapters.input.rest.data.response.LoginUserResponse;
import org.example.infrastructure.adapters.input.rest.data.response.RegisterUserResponse;
import org.example.infrastructure.adapters.input.rest.mapper.UserRestMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/users")
public class UserIdentityController {

    private final SignUpUseCase signUpUseCase;
    private final LoginUseCase loginUseCase;
    private final UserRestMapper userRestMapper;
    private final LogoutUseCase logoutUseCase;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/user")
    public ResponseEntity<RegisterUserResponse> registerUser(
            @RequestBody @Valid RegisterUserRequest registerRequest) throws UserNotFoundException, UserAlreadyExistException, IdentityManagerException {

        log.info("Registration request for email: {}", registerRequest.getEmail());

        User user = userRestMapper.toUser(registerRequest);
        user.setPassword(registerRequest.getPassword());

        User registeredUser = signUpUseCase.signUp(user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userRestMapper.toCreateUserResponse(registeredUser));
    }

    @PostMapping("/login")
    @Operation(summary = "User Login", description = "Authenticates the user and returns login response")
    public ResponseEntity<LoginUserResponse> login(@RequestBody @Valid LoginUserRequest loginUserRequest) throws UserNotFoundException, InvalidCredentialsException, AuthenticationException {

        log.info("Login request received for email: {}", loginUserRequest.getEmail());

        User userIdentity = userRestMapper.toUser(loginUserRequest);
        log.info("Mapped user identity access token: {}", userIdentity.getAccessToken());

        User authenticatedUser = loginUseCase.login(userIdentity);
        log.info("Authenticated user: {}", authenticatedUser.getEmail());

        LoginUserResponse response = userRestMapper
                .toLoginUserResponse(userIdentity);


        return ResponseEntity.status(HttpStatus.OK)
                .body(response);
    }



    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader("Authorization") String authHeader)
            throws IdentityManagerException {

        String token = authHeader.replace("Bearer ", "").trim();
        log.info("Logout request received for token: {}", token);

        User user = new User();
        user.setRefreshToken(token);

        logoutUseCase.logout(user);

        return ResponseEntity.noContent()
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .build();
    }


}

