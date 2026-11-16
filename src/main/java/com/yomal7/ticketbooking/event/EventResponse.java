package com.yomal7.ticketbooking.event;

import java.time.OffsetDateTime;

public record EventResponse(Long id, Long venueId, String name, OffsetDateTime startsAt, EventStatus status) {

    public static EventResponse from(Event event) {
        return new EventResponse(event.getId(), event.getVenueId(), event.getName(),
                event.getStartsAt(), event.getStatus());
    }
}