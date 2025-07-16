package org.example.infrastructure.adapters.input.rest.data.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Setter
@Getter
public class DeleteCommentResponse {
    private String message;
    private LocalDateTime dateDeleted;
}
