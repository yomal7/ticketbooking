package com.yomal7.ticketbooking.venue;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record GenerateSeatsRequest(
        @NotBlank String section,
        @Positive @Max(100) int rows,
        @Positive @Max(100) int seatsPerRow
) {
}