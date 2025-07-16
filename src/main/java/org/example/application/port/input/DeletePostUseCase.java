package org.example.application.port.input;

import org.example.domain.exceptions.PostAlreadyExistsException;
import org.example.domain.exceptions.PostNotFoundException;
import org.example.domain.exceptions.UserNotFoundException;
import org.example.domain.models.Post;
import org.example.domain.models.User;

import java.nio.file.AccessDeniedException;

public interface DeletePostUseCase {


    void deletePost(User user, Long id) throws UserNotFoundException, PostNotFoundException, AccessDeniedException;
}
