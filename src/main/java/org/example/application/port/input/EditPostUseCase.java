package org.example.application.port.input;

import org.example.domain.exceptions.PostNotFoundException;
import org.example.domain.exceptions.UserNotFoundException;
import org.example.domain.models.Post;
import org.example.domain.models.User;

import java.nio.file.AccessDeniedException;

public interface EditPostUseCase {


    Post editPost(User post, Post updatedPost) throws UserNotFoundException, PostNotFoundException, AccessDeniedException;
}
