package org.example.infrastructure.adapters.output.persistence.repositories;

import org.example.domain.models.Comment;
import org.example.infrastructure.adapters.output.persistence.entity.CommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository  extends JpaRepository<CommentEntity, Long> {
}
