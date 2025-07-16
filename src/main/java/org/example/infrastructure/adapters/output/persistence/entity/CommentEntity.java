package org.example.infrastructure.adapters.output.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.example.domain.models.User;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
public class CommentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String content;
    @ManyToOne
    private UserEntity user;
    private LocalDateTime commentedAt;
    @ManyToOne
    private PostEntity post;
}
