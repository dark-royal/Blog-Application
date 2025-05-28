package org.example.domain.services;

import lombok.RequiredArgsConstructor;
import org.example.application.port.input.CreatePostUseCase;
import org.example.application.port.input.DeletePostUseCase;
import org.example.application.port.output.PostPersistenceOutputPort;
import org.example.application.port.output.UserPersistenceOutputPort;
import org.example.domain.exceptions.PostAlreadyExistsException;
import org.example.domain.exceptions.PostNotFoundException;
import org.example.domain.exceptions.UserNotFoundException;
import org.example.domain.models.Post;
import org.example.domain.models.User;
import org.example.infrastructure.adapters.input.rest.messages.ErrorMessages;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;

import static org.example.domain.validator.InputValidator.validateInput;

@Service
@RequiredArgsConstructor
public class PostService implements CreatePostUseCase, DeletePostUseCase{


    private final UserPersistenceOutputPort userPersistenceOutputPort;

    private final PostPersistenceOutputPort postPersistenceOutputPort;

    @Override
    public Post createPost(User user, Post post) throws PostAlreadyExistsException, UserNotFoundException {
        validateInput(post.getTitle());
        validateInput(post.getContent());

        if(!userPersistenceOutputPort.existsById(user.getId())) {
            throw new UserNotFoundException(ErrorMessages.USER_NOT_FOUND);
        }

        if(postPersistenceOutputPort.existsByTitleAndUserId(post.getTitle(), user.getId())){
            throw new PostAlreadyExistsException(ErrorMessages.POST_ALREADY_EXIST);
        }

        post.setUser(user);
        post.setPublishedDate(LocalDateTime.now());
        return postPersistenceOutputPort.savePost(post);
    }


    @Override
    public void deletePost(User user, Long id) throws UserNotFoundException, PostNotFoundException, AccessDeniedException {


        if (!userPersistenceOutputPort.existsById(user.getId())) {
            throw new UserNotFoundException(ErrorMessages.USER_NOT_FOUND);
        }

        Post postFromDb = postPersistenceOutputPort.getPostById(id);

        if (!postFromDb.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You're not allowed to delete this post");
        }

        postPersistenceOutputPort.deletePost(postFromDb);
    }

}
