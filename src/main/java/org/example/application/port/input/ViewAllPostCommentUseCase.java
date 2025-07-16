package org.example.application.port.input;

import org.example.domain.exceptions.PostNotFoundException;
import org.example.domain.models.Comment;

import java.util.List;

public interface ViewAllPostCommentUseCase {

    List<Comment> viewAllPostCommentsByPostId(Long postId) throws PostNotFoundException;
}
