package com.papel.imdb_clone.exceptions;

/**
 * Exception thrown when invalid input is provided to a method.
 */
public class InvalidInputException extends Exception {
    private static final long serialVersionUID = 1L;
    public InvalidInputException() {
        super();
    }

    public InvalidInputException(String message) {
        super(message);
    }

    public InvalidInputException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidInputException(Throwable cause) {
        super(cause);
    }
}
