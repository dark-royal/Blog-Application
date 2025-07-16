package org.example.infrastructure.adapters.output.persistence.mapper;

import org.example.domain.models.Comment;
import org.example.infrastructure.adapters.output.persistence.entity.CommentEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CommentPersistenceMapper {

    CommentEntity toCommentEntity(Comment comment);

    Comment toComment(CommentEntity entity);

}
