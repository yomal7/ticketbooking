package com.yomal7.ticketbooking.venue;

public record SeatResponse(Long id, String section, String rowLabel, Integer seatNumber) {

    public static SeatResponse from(Seat seat) {
        return new SeatResponse(seat.getId(), seat.getSection(), seat.getRowLabel(), seat.getSeatNumber());
    }
}