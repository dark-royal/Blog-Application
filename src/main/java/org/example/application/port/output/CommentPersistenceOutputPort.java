package org.example.application.port.output;

import org.example.domain.models.Comment;

public interface CommentPersistenceOutputPort {

    Comment saveComment(Comment comment);

}
