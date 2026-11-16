package com.yomal7.ticketbooking.event;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.OffsetDateTime;

public record CreateEventRequest(
        @NotNull Long venueId,
        @NotBlank String name,
        @NotNull OffsetDateTime startsAt,
        @Positive int priceCents
) {
}