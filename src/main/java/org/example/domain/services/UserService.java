package org.example.domain.services;

import lombok.extern.slf4j.Slf4j;
import org.example.application.port.input.LoginUseCase;
import org.example.application.port.input.LogoutUseCase;
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

import java.util.List;

import static org.example.domain.validator.InputValidator.validateInput;

@Slf4j
@Service
public class UserService implements SignUpUseCase, LoginUseCase, LogoutUseCase{

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
        validateInput(user.getUsername());
        validateInput(user.getFirstName());
        validateInput(user.getLastName());
        validateInput(user.getPassword());
        validateInput(user.getRole());

        if (userPersistenceOutputPort.userExistsByEmail(user.getEmail())) {
            throw new UserAlreadyExistException(ErrorMessages.USER_EXISTS_ALREADY);
        }
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

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
        validateInput(user.getPassword());

        User foundUser = userPersistenceOutputPort.getUserByEmail(user.getEmail());
        if (foundUser == null) {
            throw new UserNotFoundException(ErrorMessages.USER_NOT_FOUND);
        }

        log.info("found user password {}", foundUser.getPassword());

        if (!passwordEncoder.matches(user.getPassword(), foundUser.getPassword())) {
            throw new InvalidCredentialsException("Invalid login credentials");
        }

        if (!foundUser.isEnabled()) {
            throw new IllegalStateException("Account is not enabled");
        }

        log.info("found user enabled {}", foundUser);

        identityManagementOutputPort.loginUser(foundUser);

        log.info("User with email {} has logged in successfully", user.getEmail());
        return foundUser;
    }


    @Override
    public void logout(User user) throws IdentityManagerException {
        validateInput(user.getRefreshToken());

        try {
            identityManagementOutputPort.logoutUser(user);

            log.info("User logged out successfully");
        } catch (IdentityManagerException e) {
            log.error("Logout failed for user: {}", e.getMessage());
            throw e;
        }
    }


}
