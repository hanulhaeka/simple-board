package com.example.simple_board.exception;

public class NotCorrectPasswordException extends RuntimeException {
    public NotCorrectPasswordException(String message) {
        super(message);
    }
}
