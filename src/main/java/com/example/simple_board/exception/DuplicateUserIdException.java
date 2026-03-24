package com.example.simple_board.exception;

public class DuplicateUserIdException extends RuntimeException{
    public DuplicateUserIdException(String userId) {
        super("이미 사용 중인 아이디입니다: " + userId);
    }
}
