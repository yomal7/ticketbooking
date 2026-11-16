package com.yomal7.ticketbooking.event;

public record EventSeatResponse(Long id, Long seatId, Integer priceCents, EventSeatStatus status) {

    public static EventSeatResponse from(EventSeat eventSeat) {
        return new EventSeatResponse(eventSeat.getId(), eventSeat.getSeatId(),
                eventSeat.getPriceCents(), eventSeat.getStatus());
    }
}