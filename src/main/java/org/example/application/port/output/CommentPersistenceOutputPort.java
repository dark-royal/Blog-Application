package org.example.application.port.output;

import org.example.domain.models.Comment;

import java.util.List;

public interface CommentPersistenceOutputPort {

    Comment saveComment(Comment comment);

    List<Comment> getAllCommentsByPostId(Long id);

}
