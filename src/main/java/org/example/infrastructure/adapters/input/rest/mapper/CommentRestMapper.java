package org.example.infrastructure.adapters.input.rest.mapper;

import jakarta.validation.Valid;
import org.example.domain.models.Comment;
import org.example.infrastructure.adapters.input.rest.data.request.CommentRequest;
import org.example.infrastructure.adapters.input.rest.data.response.CommentResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CommentRestMapper {
    
    Comment toComment(@Valid CommentRequest commentRequest);

    CommentResponse toCommentResponse(Comment savedComment);

    CommentResponse toViewAllPostCommentResponse(Comment comment);
}
