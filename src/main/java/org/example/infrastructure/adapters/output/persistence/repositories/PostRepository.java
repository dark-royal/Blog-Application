package org.example.infrastructure.adapters.output.persistence.repositories;

import org.example.domain.models.Post;
import org.example.infrastructure.adapters.output.persistence.entity.PostEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<PostEntity, Long> {
    Optional<PostEntity> findByTitle(String title);

    boolean existsByTitleAndUserId(String title, Long id);

    List<PostEntity> findAllByUserId(Long id);

}
