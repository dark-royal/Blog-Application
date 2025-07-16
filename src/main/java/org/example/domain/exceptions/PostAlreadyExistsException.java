package org.example.domain.exceptions;

public class PostAlreadyExistsException extends Throwable {
    public PostAlreadyExistsException(String message) {
        super(message);
    }
}
