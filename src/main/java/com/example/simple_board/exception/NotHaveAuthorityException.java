package com.example.simple_board.exception;

public class NotHaveAuthorityException extends RuntimeException {
    public NotHaveAuthorityException(String message) {
        super(message);
    }
}
