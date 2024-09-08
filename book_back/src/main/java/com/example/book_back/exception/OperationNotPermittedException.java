package com.example.book_back.exception;

public class OperationNotPermittedException extends RuntimeException {
    public OperationNotPermittedException() {
    }

    public OperationNotPermittedException(String message) {
        super(message);
    }
}
