package org.example.application.port.input;

import org.example.domain.exceptions.CommentNotFoundException;

public interface DeleteCommentUseCase {

    void deleteComment(Long postId,Long commentId) throws CommentNotFoundException;
}
