package org.example.infrastructure.adapters.input.rest.data.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class CommentResponse {
    private Long id;
    private String content;
    private LocalDateTime commentedAt;

}
