package org.example.application.port.input;

import org.example.domain.exceptions.PostAlreadyExistsException;
import org.example.domain.exceptions.PostNotFoundException;
import org.example.domain.exceptions.UserNotFoundException;
import org.example.domain.models.Post;
import org.example.domain.models.User;

public interface CreatePostUseCase {


    Post createPost(User user, Post post) throws PostAlreadyExistsException, UserNotFoundException;
}
