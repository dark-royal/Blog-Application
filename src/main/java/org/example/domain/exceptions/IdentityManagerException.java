package org.example.domain.exceptions;

import org.springframework.http.HttpStatus;

public class IdentityManagerException extends Exception {
    public IdentityManagerException(String message) {
        super(message);

    }
}
