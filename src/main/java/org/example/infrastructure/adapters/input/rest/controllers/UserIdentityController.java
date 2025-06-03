package org.example.infrastructure.adapters.input.rest.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Tag(name = "Users", description = "Operations related to user authentication and management")
public class UserIdentityController {

    private final SignUpUseCase signUpUseCase;
    private final LoginUseCase loginUseCase;
    private final UserRestMapper userRestMapper;
    private final LogoutUseCase logoutUseCase;

    @Operation(summary = "Register a new user", description = "Creates a new user account")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered successfully", content = @Content(schema = @Schema(implementation = RegisterUserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid user data"),
            @ApiResponse(responseCode = "409", description = "User already exists"),
            @ApiResponse(responseCode = "409", description = "fields are  empty"),
    })
    @PostMapping("/user")
    public ResponseEntity<RegisterUserResponse> registerUser(
            @RequestBody @Valid @Parameter(description = "User registration details") RegisterUserRequest registerRequest)
            throws UserNotFoundException, UserAlreadyExistException, IdentityManagerException {

        log.info("Registration request for email: {}", registerRequest.getEmail());

        User user = userRestMapper.toUser(registerRequest);
        user.setPassword(registerRequest.getPassword());
        User registeredUser = signUpUseCase.signUp(user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userRestMapper.toCreateUserResponse(registeredUser));
    }

    @Operation(summary = "User login", description = "Authenticates a user and returns a JWT token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User authenticated successfully", content = @Content(schema = @Schema(implementation = LoginUserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "404", description = "User not found"),
    })
    @PostMapping("/login")
    public ResponseEntity<LoginUserResponse> login(
            @RequestBody @Valid @Parameter(description = "User login credentials") LoginUserRequest loginUserRequest)
            throws UserNotFoundException, InvalidCredentialsException, AuthenticationException {

        log.info("Login request received for email: {}", loginUserRequest.getEmail());

        User user = userRestMapper.toUser(loginUserRequest);
        User authenticatedUser = loginUseCase.login(user);
        LoginUserResponse response = userRestMapper.toLoginUserResponse(authenticatedUser);

        log.info("Successful login for email: {}", authenticatedUser.getEmail());

        return ResponseEntity.ok(response);
    }

//    @Operation(summary = "User logout", description = "Logs out a user by invalidating the refresh token")
//    @ApiResponses({
//            @ApiResponse(responseCode = "201", description = "User logged out successfully"),
//            @ApiResponse(responseCode = "401", description = "Invalid token"),
//    })
//
//    @SecurityRequirement(name = "Keycloak")
//    @PostMapping("/logout")
//    public ResponseEntity<Void> logout(
//            @RequestHeader("Authorization") @Parameter(description = "JWT token (Bearer)") String authHeader)
//            throws IdentityManagerException {
//
//        String token = authHeader.replace("Bearer ", "").trim();
//        log.info("Logout request received for token: {}", token);
//
//        User user = new User();
//        user.setRefreshToken(token);
//        logoutUseCase.logout(user);
//
//        return ResponseEntity.noContent()
//                .header(HttpHeaders.CACHE_CONTROL, "no-store")
//                .build();
//    }
}