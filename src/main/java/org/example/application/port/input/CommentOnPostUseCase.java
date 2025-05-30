package org.example.application.port.input;

import org.example.domain.exceptions.PostNotFoundException;
import org.example.domain.exceptions.UserNotFoundException;
import org.example.domain.models.Comment;
import org.example.domain.models.User;

public interface CommentOnPostUseCase {

    Comment writeComment(Comment comment, User user, Long postId) throws PostNotFoundException, UserNotFoundException;

}
