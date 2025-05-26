package org.example.application.port.output;

import org.example.domain.exceptions.UserNotFoundException;
import org.example.domain.models.User;

public interface UserPersistenceOutputPort {

    User saveUser(User user);
    User getUserById(Long id) throws UserNotFoundException;

    User getUserByEmail(String email) throws UserNotFoundException;

    boolean userExistsByEmail(String email);
}
