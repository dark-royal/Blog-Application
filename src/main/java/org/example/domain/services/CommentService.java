package org.example.domain.services;

import lombok.RequiredArgsConstructor;
import org.example.application.port.input.CommentOnPostUseCase;
import org.example.application.port.input.DeleteCommentUseCase;
import org.example.application.port.input.ViewAllPostCommentUseCase;
import org.example.application.port.output.CommentPersistenceOutputPort;
import org.example.application.port.output.PostPersistenceOutputPort;
import org.example.application.port.output.UserPersistenceOutputPort;
import org.example.domain.exceptions.CommentNotFoundException;
import org.example.domain.exceptions.PostNotFoundException;
import org.example.domain.exceptions.UserNotFoundException;
import org.example.domain.models.Comment;
import org.example.domain.models.Post;
import org.example.domain.models.User;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;

import static org.example.domain.validator.InputValidator.validateInput;

@Service
@RequiredArgsConstructor
public class CommentService implements CommentOnPostUseCase, ViewAllPostCommentUseCase, DeleteCommentUseCase {

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

    @Override
    public List<Comment> viewAllPostCommentsByPostId(Long postId) throws PostNotFoundException {
        Post post = postPersistenceOutputPort.getPostById(postId);
        return commentPersistenceOutputPort.getAllCommentsByPostId(post.getId());
    }


    @Override
    public void deleteComment(Long commentId, Long postId, User user)
            throws CommentNotFoundException, AccessDeniedException {

        Comment comment = commentPersistenceOutputPort.getCommentByIdAndPostId(commentId, postId);

        Long commentAuthorId = comment.getUser().getId();
        Long postOwnerId = comment.getPost().getUser().getId();

        if (!user.getId().equals(commentAuthorId) && !user.getId().equals(postOwnerId)) {
            throw new AccessDeniedException("You are not allowed to delete this comment");
        }

        commentPersistenceOutputPort.deleteCommentById(comment.getId());
    }



}
