package com.yomal7.ticketbooking.common;

public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}