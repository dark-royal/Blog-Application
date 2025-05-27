package org.example.application.port.input;

import org.example.domain.exceptions.IdentityManagerException;
import org.example.domain.exceptions.InvalidCredentialsException;
import org.example.domain.exceptions.UserAlreadyExistException;
import org.example.domain.exceptions.UserNotFoundException;
import org.example.domain.models.User;

public interface SignUpUseCase {

    User signUp(User user) throws UserNotFoundException, UserAlreadyExistException, IdentityManagerException;

}
