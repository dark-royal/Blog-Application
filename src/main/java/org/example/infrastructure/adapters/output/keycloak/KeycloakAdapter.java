package org.example.infrastructure.adapters.output.keycloak;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.application.port.output.IdentityManagementOutputPort;
import org.example.domain.exceptions.AuthenticationException;
import org.example.domain.exceptions.IdentityManagerException;
import org.example.domain.exceptions.UserAlreadyExistException;
import org.example.domain.exceptions.UserNotFoundException;
import org.example.domain.models.User;
import org.example.infrastructure.adapters.input.rest.data.response.LoginUserResponse;
import org.example.infrastructure.adapters.input.rest.messages.ErrorMessages;
import org.example.infrastructure.adapters.output.mapper.UserMapper;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class KeycloakAdapter implements IdentityManagementOutputPort {

    @Value("${app.keycloak.realm}")
    private String realm;

    @Value("${app.keycloak.admin.clientId}")
    private String clientId;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${app.keycloak.tokenUrl}")
    private String tokenUrl;

    @Value("${app.keycloak.admin.clientSecret}")
    private String clientSecret;

    @Autowired
    private ObjectMapper objectMapper;

    private final Keycloak keycloak;


    @Override
    public User createUser(User user) throws IdentityManagerException, UserAlreadyExistException {
        if (doesUserExist(user.getEmail())) {
            throw new UserAlreadyExistException(ErrorMessages.USER_EXISTS_ALREADY);
        }
        UserRepresentation userRepresentation = createUserRepresentation(user);
        log.info("Using realm: {}", keycloak.realm(realm).toRepresentation().getRealm());
        log.debug("Creating user with email: {}, password: {}", user.getEmail(), user.getPassword());
        try (Response response = getUserResource().create(userRepresentation)) {
            log.info("Keycloak user creation response status: {}, body: {}", response.getStatus(), response.readEntity(String.class));
            log.info("Sent to Keycloak: {}", userRepresentation);

            if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
                String userId = response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1");
                UserRepresentation createdUser = getUserById(userId).toRepresentation();
                log.info("Created user enabled: {}, emailVerified: {}", createdUser.isEnabled(), createdUser.isEmailVerified());
                assignRole(userId, user.getRole());
                user.setKeycloakId(userId);
                return user;
            } else {
                String errorMessage = response.readEntity(String.class);
                log.error("Keycloak user creation failed. Status: {}, Error: {}", response.getStatus(), errorMessage);
                if (response.getStatus() == Response.Status.CONFLICT.getStatusCode()) {
                    throw new UserAlreadyExistException(ErrorMessages.USER_EXISTS_ALREADY);
                } else {
                    throw new IdentityManagerException("Failed to create user in Keycloak: " + errorMessage);
                }
            }
        }
    }



    @Override
    public boolean doesUserExist(String email) {
        validateInput(email);
        List<UserRepresentation> userRepresentations = getUserResource().list();
        for (UserRepresentation user : userRepresentations) {
            if (user.getUsername().equals(email)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void deleteUser(User user) throws UserNotFoundException {

        UserRepresentation  username = findUserByUsername(user.getEmail());
        keycloak.realm(realm).users().get(username.getId()).remove();
    }

    @Override
    public User loginUser(User user) throws AuthenticationException {
        try {
            ResponseEntity<String> response = authenticateUserWithKeycloak(user);
            log.info("Response from auth server: {}", response.getBody());

            LoginUserResponse loginUserResponse = objectMapper.readValue(response.getBody(), LoginUserResponse.class);

            user.setAccessToken(loginUserResponse.getAccessToken());
            user.setRefreshToken(loginUserResponse.getRefreshToken());

            return user;

        } catch (JsonProcessingException e) {
            log.error("Error parsing Keycloak response: ", e);
            throw new AuthenticationException("Failed to process login response");

        } catch (HttpClientErrorException e) {
            log.error("Error authenticating user: {}", e.getResponseBodyAsString());
            throw new AuthenticationException("Invalid credentials");
        }
    }

    @Override
    public User resetPassword(User user) throws AuthenticationException {
        try {
            UserRepresentation foundUser = findUserByUsername(user.getEmail());
            CredentialRepresentation credential = createPasswordCredentials(user.getPassword());

            getUserById(foundUser.getId()).resetPassword(credential);

            return user;
        } catch (UserNotFoundException e) {
            throw new AuthenticationException("User not found");
        } catch (Exception e) {
            throw new AuthenticationException("Password reset failed");
        }
    }


    @Override
    public Optional<User> getUserByEmail(String email) throws UserNotFoundException {
        log.info("passed email::{}", email);
        UserRepresentation userRepresentation = findUserByUsername(email);
        if (userRepresentation == null) {
            return Optional.empty();
        }
        User user = userMapper.toDomain(userRepresentation);
        log.info("Found user in keycloak::=====>>> {}", user);
        user.setUserRepresentation(userRepresentation);
        return Optional.of(user);
    }


    private ResponseEntity<String> authenticateUserWithKeycloak(User user) {
        log.info("Attempting Keycloak auth for: {}", user.getEmail());
        log.debug("Using client_id: {}, tokenUrl: {}", clientId, tokenUrl);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "password");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("username", user.getEmail());
        params.add("password", user.getPassword());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(tokenUrl, request, String.class);
            log.info("Keycloak response status: {}", response.getStatusCode());
            return response;
        } catch (HttpClientErrorException e) {
            log.error("Keycloak error: Status={}, Body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        }
    }




    private UserRepresentation findUserByUsername(String username) throws UserNotFoundException {
        List<UserRepresentation> userUsername = getUserResource().search(username);
        if(userUsername == null) {
            throw new UserNotFoundException(ErrorMessages.USER_NOT_FOUND);
        }
        return userUsername.getFirst();
    }


    public static void validateInput(String value) {
        if (value == null || StringUtils.isEmpty(value) || StringUtils.isBlank(value) || value.equals(ErrorMessages.UNDEFINED)) {
            throw new IllegalArgumentException(ErrorMessages.EMPTY_INPUT_ERROR);
        }
    }


    private void assignRole(String userId, String role) throws IdentityManagerException {
        UserResource usersResource = getUserById(userId);
        RoleRepresentation roleRepresentation = getRolesResource().get(role).toRepresentation();
        if (roleRepresentation == null) {
            throw new IdentityManagerException("Role '" + role + "' does not exist");
        }
        usersResource.roles().realmLevel().add(Collections.singletonList(roleRepresentation));
    }

    private RolesResource getRolesResource() {
        return keycloak.realm(realm).roles();
    }

    private UserResource getUserById(String userId) {
        return getUserResource().get(userId);
    }

    private UsersResource getUserResource() {
        return keycloak.realm(realm).users();
    }

    private UserRepresentation createUserRepresentation(User user) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(user.getEmail());
        userRepresentation.setEmail(user.getEmail());
        userRepresentation.setFirstName(user.getFirstName());
        userRepresentation.setLastName(user.getLastName());
        userRepresentation.setEnabled(true);
        userRepresentation.setEmailVerified(true);
        userRepresentation.setCredentials(List.of(createPasswordCredentials(user.getPassword())));
        return userRepresentation;
    }

    private CredentialRepresentation createPasswordCredentials(String password) {
        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        credentialRepresentation.setValue(password);
        credentialRepresentation.setTemporary(false);
        return credentialRepresentation;
    }





}
