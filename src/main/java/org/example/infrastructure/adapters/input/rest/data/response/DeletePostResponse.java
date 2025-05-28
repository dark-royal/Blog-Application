package org.example.infrastructure.adapters.input.rest.data.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class DeletePostResponse {
    private String message;
    private LocalDateTime dateDeleted;
}
