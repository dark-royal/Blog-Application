package org.example.domain.exceptions;

public class InvalidCredentialsException extends Throwable {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
