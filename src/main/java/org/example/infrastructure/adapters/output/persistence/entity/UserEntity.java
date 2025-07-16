package org.example.infrastructure.adapters.output.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@Entity
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String password;
    private String role;
    @OneToMany
    private List<PostEntity> posts;
    private String lastName;
    private boolean emailVerified;
    private boolean enabled;
}