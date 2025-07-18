package org.example.domain.services;

import lombok.RequiredArgsConstructor;
import org.example.application.port.input.*;
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
import java.util.List;

import static org.example.domain.validator.InputValidator.validateInput;

@Service
@RequiredArgsConstructor
public class PostService implements CreatePostUseCase, DeletePostUseCase, EditPostUseCase, ViewPostUseCase, ViewAllPostUseCase {


    private final UserPersistenceOutputPort userPersistenceOutputPort;

    private final PostPersistenceOutputPort postPersistenceOutputPort;

    @Override
    public Post createPost(User user, Post post) throws PostAlreadyExistsException, UserNotFoundException {
        validateInput(post.getTitle());
        validateInput(post.getContent());

        if (!userPersistenceOutputPort.existsById(user.getId())) {
            throw new UserNotFoundException(ErrorMessages.USER_NOT_FOUND);
        }

        if (postPersistenceOutputPort.existsByTitleAndUserId(post.getTitle(), user.getId())) {
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


    @Override
    public Post editPost(User user, Post updatedPost) throws UserNotFoundException, PostNotFoundException, AccessDeniedException {
        validateInput(updatedPost.getTitle());
        validateInput(updatedPost.getContent());

        if (!userPersistenceOutputPort.existsById(user.getId())) {
            throw new UserNotFoundException(ErrorMessages.USER_NOT_FOUND);
        }

        Post postFromDb = postPersistenceOutputPort.getPostById(updatedPost.getId());

        if (!postFromDb.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You're not allowed to edit this post");
        }

        postFromDb.setTitle(updatedPost.getTitle());
        postFromDb.setContent(updatedPost.getContent());
        postFromDb.setUpdatedDate(LocalDateTime.now());

        return postPersistenceOutputPort.savePost(postFromDb);
    }

    @Override
    public Post viewPost(Long id) throws PostNotFoundException {
        return postPersistenceOutputPort.getPostById(id);

    }

    @Override
    public List<Post> getAllPostsByUserId(Long id) throws UserNotFoundException, PostNotFoundException {
        if (!userPersistenceOutputPort.existsById(id)) {
            throw new UserNotFoundException(ErrorMessages.USER_NOT_FOUND);
        }

        List<Post> posts = postPersistenceOutputPort.getAllPostByUserId(id);

        if (posts.isEmpty()) {
            throw new PostNotFoundException("No posts found for this user");
        }

        return posts;
    }
}
