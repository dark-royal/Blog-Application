package org.example.domain.services;

import lombok.extern.slf4j.Slf4j;
import org.example.application.port.input.LoginUseCase;
import org.example.application.port.input.ResetPasswordUseCase;
import org.example.application.port.input.SignUpUseCase;
import org.example.application.port.output.IdentityManagementOutputPort;
import org.example.application.port.output.UserPersistenceOutputPort;
import org.example.domain.exceptions.*;
import org.example.domain.models.User;
import org.example.infrastructure.adapters.input.rest.messages.ErrorMessages;
import org.example.infrastructure.adapters.output.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static org.example.domain.validator.InputValidator.validateInput;

@Slf4j
@Service
public class UserService implements SignUpUseCase, LoginUseCase, ResetPasswordUseCase {

    @Autowired
    private UserPersistenceOutputPort userPersistenceOutputPort;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private IdentityManagementOutputPort identityManagementOutputPort;

    @Autowired
    private UserMapper userMapper;


    @Override
    public User signUp(User user) throws UserAlreadyExistException, IdentityManagerException {
        validateInput(user.getEmail());
        if (userPersistenceOutputPort.userExistsByEmail(user.getEmail())) {
            throw new UserAlreadyExistException(ErrorMessages.USER_EXISTS_ALREADY);
        }

        user = identityManagementOutputPort.createUser(user);
        log.info("Created new user: {}", user);

        user.setEnabled(true);
        user = userPersistenceOutputPort.saveUser(user);
        log.info("User saved to database: email={}, id={}", user.getEmail(), user.getId());
        return user;
    }

    @Override
    public User login(User user) throws UserNotFoundException, InvalidCredentialsException, AuthenticationException {
        validateInput(user.getEmail());

        // 1. Authenticate with Keycloak (this contains tokens)
        User keycloakUser = identityManagementOutputPort.loginUser(user);

        // 2. Get local user record
        User foundUser = userPersistenceOutputPort.getUserByEmail(keycloakUser.getEmail());
        if (foundUser == null) {
            throw new UserNotFoundException(ErrorMessages.USER_NOT_FOUND);
        }

//        // 3. Verify credentials
//        if (!passwordEncoder.matches(user.getPassword(), foundUser.getPassword())) {
//            throw new InvalidCredentialsException("Invalid login credentials");
//        }

        if (!foundUser.isEnabled()) {
            throw new IllegalStateException("Account is not enabled");
        }

        foundUser.setAccessToken(keycloakUser.getAccessToken());
        foundUser.setRefreshToken(keycloakUser.getRefreshToken());
        foundUser.setExpiresIn(keycloakUser.getExpiresIn());
        foundUser.setRefreshExpiresIn(keycloakUser.getRefreshExpiresIn());
        foundUser.setTokenType(keycloakUser.getTokenType());
        foundUser.setIdToken(keycloakUser.getIdToken());
        foundUser.setScope(keycloakUser.getScope());

        log.info("User {} logged in successfully with token", user.getEmail());

        log.info("User {} logged in. Token expires in: {}", user.getEmail(), foundUser.getExpiresIn());

        return foundUser;
    }


    @Override
    public User resetPassword(User user) throws AuthenticationException, UserNotFoundException {

        validateInput(user.getEmail());
        validateInput(user.getPassword());

        User existingUser = userPersistenceOutputPort.getUserByEmail(user.getEmail());
        if (existingUser == null) {
            throw new UserNotFoundException("User with email " + user.getEmail() + " not found");
        }

        try {

            User updatedUser = identityManagementOutputPort.resetPassword(user);

            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
            userPersistenceOutputPort.saveUser(existingUser);

            return updatedUser;
        } catch (AuthenticationException e) {
            throw new AuthenticationException("Failed to reset password: " + e.getMessage());
        }
    }



}
