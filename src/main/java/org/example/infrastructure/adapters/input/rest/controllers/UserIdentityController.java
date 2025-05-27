package org.example.infrastructure.adapters.input.rest.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.example.application.port.input.LoginUseCase;
import org.example.application.port.input.ResetPasswordUseCase;
import org.example.application.port.input.SignUpUseCase;
import org.example.domain.exceptions.*;
import org.example.domain.models.User;
import org.example.infrastructure.adapters.input.rest.data.request.LoginUserRequest;
import org.example.infrastructure.adapters.input.rest.data.request.RegisterUserRequest;
import org.example.infrastructure.adapters.input.rest.data.request.ResetPasswordRequest;
import org.example.infrastructure.adapters.input.rest.data.response.LoginUserResponse;
import org.example.infrastructure.adapters.input.rest.data.response.RegisterUserResponse;
import org.example.infrastructure.adapters.input.rest.data.response.ResetPasswordResponse;
import org.example.infrastructure.adapters.input.rest.mapper.UserRestMapper;
import org.example.infrastructure.adapters.output.mapper.UserMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/users")
public class UserIdentityController {

    private final SignUpUseCase signUpUseCase;
    private final LoginUseCase loginUseCase;
    private final UserRestMapper userRestMapper;
    private final ResetPasswordUseCase resetPasswordUseCase;
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

        LoginUserResponse loginUserResponse = userRestMapper.toLoginUserResponse(authenticatedUser);

        log.info("Mapped login user response: {}", loginUserResponse);
        return ResponseEntity.ok(loginUserResponse);
    }


    @PostMapping("/new-password")
    public ResponseEntity<ResetPasswordResponse> resetPassword(@RequestBody @Valid ResetPasswordRequest request) throws UserNotFoundException, AuthenticationException {

        User user = userRestMapper.toUser(request);
        user.setPassword(request.getNewPassword());

        User resetPassword = resetPasswordUseCase.resetPassword(user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userRestMapper.toResetPasswordResponse(resetPassword));
    }




}

