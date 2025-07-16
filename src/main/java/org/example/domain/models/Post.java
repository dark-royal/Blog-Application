package org.example.domain.models;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
public class Post {
    private Long id;
    private String title;
    private String content;
    private User user;
    private LocalDateTime publishedDate;
    private LocalDateTime editedAt;
    private List<Comment> comments;
    private LocalDateTime updatedDate;

}
