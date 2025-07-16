package org.example.domain.models;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class Comment {
    private Long id;
    private String content;
    private LocalDateTime commentedAt;
    private User user;
    private Post post;

}



