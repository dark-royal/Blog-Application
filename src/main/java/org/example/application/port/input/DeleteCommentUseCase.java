package org.example.application.port.input;

import org.example.domain.exceptions.CommentNotFoundException;
import org.example.domain.models.User;
import org.springframework.security.access.AccessDeniedException;

public interface DeleteCommentUseCase {

    void deleteComment(Long commentId, Long postId, User user)
            throws CommentNotFoundException, AccessDeniedException, java.nio.file.AccessDeniedException;
}
