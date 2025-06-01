package org.example.domain.exceptions;

public class CommentNotFoundException extends Throwable {
    public CommentNotFoundException(String message) {
        super(message);
    }
}
