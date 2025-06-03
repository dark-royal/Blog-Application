package org.example.application.port.output;


import org.example.domain.exceptions.AuthenticationException;
import org.example.domain.exceptions.IdentityManagerException;
import org.example.domain.exceptions.UserAlreadyExistException;
import org.example.domain.exceptions.UserNotFoundException;
import org.example.domain.models.User;

import java.util.Optional;

public interface IdentityManagementOutputPort {

    User createUser(User user) throws IdentityManagerException, UserAlreadyExistException;
    boolean doesUserExist(String email);

//    void deleteUser(User user) throws UserNotFoundException;

    User loginUser(User user) throws AuthenticationException;

//    User resetPassword(User user) throws AuthenticationException, UserNotFoundException;

    Optional<User> getUserByEmail(String email) throws UserNotFoundException;

    void logoutUser(User user) throws IdentityManagerException;
}
