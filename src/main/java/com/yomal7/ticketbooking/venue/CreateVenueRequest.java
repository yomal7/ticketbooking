package com.yomal7.ticketbooking.venue;

import jakarta.validation.constraints.NotBlank;

public record CreateVenueRequest(
        @NotBlank String name,
        @NotBlank String city
) {
}