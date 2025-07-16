package org.example.application.port.input;

import org.example.domain.exceptions.IdentityManagerException;
import org.example.domain.models.User;

public interface LogoutUseCase {

    void logout(User user) throws IdentityManagerException;
}
