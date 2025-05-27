package org.example.infrastructure.adapters.input.rest.data.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class CreatePostResponse {

    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;


}
