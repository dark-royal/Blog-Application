package org.example.infrastructure.adapters.output.persistence.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.application.port.output.CommentPersistenceOutputPort;
import org.example.domain.models.Comment;
import org.example.domain.models.Post;
import org.example.infrastructure.adapters.output.persistence.entity.CommentEntity;
import org.example.infrastructure.adapters.output.persistence.entity.PostEntity;
import org.example.infrastructure.adapters.output.persistence.mapper.CommentPersistenceMapper;
import org.example.infrastructure.adapters.output.persistence.repositories.CommentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentPersistenceAdapter implements CommentPersistenceOutputPort {

    private final CommentRepository commentRepository;
    private final CommentPersistenceMapper commentPersistenceMapper;

    @Override
    public Comment saveComment(Comment comment) {
        log.info("Saving comment: {}", comment);

        CommentEntity entity = commentPersistenceMapper.toCommentEntity(comment);
        log.info("Mapped to entity: {}", entity);

        entity = commentRepository.save(entity);
        log.info("Saved entity: {}", entity);

        Comment savedPost = commentPersistenceMapper.toComment(entity);
        log.info("Mapped back to domain comment: {}", savedPost);

        return savedPost;
    }

    @Override
    public List<Comment> getAllCommentsByPostId(Long id) {
        List<CommentEntity> commentEntities = commentRepository.findAllByPostId(id);
        return commentEntities
                .stream()
                .map(commentPersistenceMapper::toComment)
                .toList();
    }

}
