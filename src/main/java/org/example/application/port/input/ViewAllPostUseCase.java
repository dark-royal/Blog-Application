package org.example.application.port.input;

import org.example.domain.exceptions.PostNotFoundException;
import org.example.domain.exceptions.UserNotFoundException;
import org.example.domain.models.Post;

import java.util.List;

public interface ViewAllPostUseCase {

    List<Post> getAllPostsByUserId(Long id) throws UserNotFoundException, PostNotFoundException;



}
