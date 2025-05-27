package org.example.application.port.input;

import org.example.domain.exceptions.PostNotFoundException;
import org.example.domain.exceptions.UserNotFoundException;
import org.example.domain.models.Post;
import org.example.domain.models.User;

public interface DeletePostUseCase {

    void deletePost(User user, Post post) throws UserNotFoundException, PostNotFoundException;
}
