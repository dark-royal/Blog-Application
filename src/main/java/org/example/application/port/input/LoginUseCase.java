package org.example.application.port.input;

import org.example.domain.exceptions.AuthenticationException;
import org.example.domain.exceptions.InvalidCredentialsException;
import org.example.domain.exceptions.UserNotFoundException;
import org.example.domain.models.User;

public interface LoginUseCase {
    User login(User user) throws UserNotFoundException, InvalidCredentialsException, AuthenticationException;

}
