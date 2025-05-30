package org.example.domain.services;

import lombok.RequiredArgsConstructor;
import org.example.application.port.input.CommentOnPostUseCase;
import org.example.application.port.output.CommentPersistenceOutputPort;
import org.example.application.port.output.PostPersistenceOutputPort;
import org.example.application.port.output.UserPersistenceOutputPort;
import org.example.domain.exceptions.PostNotFoundException;
import org.example.domain.exceptions.UserNotFoundException;
import org.example.domain.models.Comment;
import org.example.domain.models.Post;
import org.example.domain.models.User;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDateTime;

import static org.example.domain.validator.InputValidator.validateInput;

@Service
@RequiredArgsConstructor
public class CommentService implements CommentOnPostUseCase {

    private final PostPersistenceOutputPort postPersistenceOutputPort;
    private final UserPersistenceOutputPort userPersistenceOutputPort;
    private final CommentPersistenceOutputPort commentPersistenceOutputPort;


    @Override
    public Comment writeComment(Comment comment,  User user, Long postId)
            throws PostNotFoundException, UserNotFoundException {
        validateInput(comment.getContent());
        User foundUser = userPersistenceOutputPort.getUserById(user.getId());

        Post post = postPersistenceOutputPort.getPostById(postId);

        comment.setCommentedAt(LocalDateTime.now());
        comment.setPost(post);
        comment.setUser(foundUser);

        return commentPersistenceOutputPort.saveComment(comment);
    }
}
