package com.yomal7.ticketbooking.booking;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record CreateBookingRequest(
        @NotEmpty @Size(max = 6) List<Long> eventSeatIds
) {
}