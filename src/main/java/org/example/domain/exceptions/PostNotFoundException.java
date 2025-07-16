package org.example.domain.exceptions;

public class PostNotFoundException extends Throwable {
    public PostNotFoundException(String message) {
        super(message);
    }
}
