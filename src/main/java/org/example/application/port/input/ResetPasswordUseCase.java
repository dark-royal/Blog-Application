package org.example.application.port.input;

import org.example.domain.exceptions.AuthenticationException;
import org.example.domain.exceptions.UserNotFoundException;
import org.example.domain.models.User;

public interface ResetPasswordUseCase {

    User resetPassword(User user) throws AuthenticationException, UserNotFoundException;
}
