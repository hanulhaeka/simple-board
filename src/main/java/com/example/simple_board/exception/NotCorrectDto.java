package com.example.simple_board.exception;

public class NotCorrectDto extends RuntimeException {
    public NotCorrectDto(String message) {
        super(message);
    }
}
