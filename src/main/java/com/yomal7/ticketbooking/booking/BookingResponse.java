package com.yomal7.ticketbooking.booking;

import java.time.OffsetDateTime;

public record BookingResponse(Long id, Long userId, Long eventId, BookingStatus status,
                              Integer seatCount, Long totalCents, OffsetDateTime expiresAt) {

    public static BookingResponse from(Booking b) {
        return new BookingResponse(b.getId(), b.getUserId(), b.getEventId(), b.getStatus(),
                b.getSeatCount(), b.getTotalCents(), b.getExpiresAt());
    }
}