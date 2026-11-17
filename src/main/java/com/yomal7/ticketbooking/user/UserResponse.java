package com.yomal7.ticketbooking.user;

import java.time.OffsetDateTime;

public record UserResponse(Long id, String email, String role, OffsetDateTime createdAt) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getRole(), user.getCreatedAt());
    }
}