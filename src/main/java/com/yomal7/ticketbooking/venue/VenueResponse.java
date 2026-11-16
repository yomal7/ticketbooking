package com.yomal7.ticketbooking.venue;

public record VenueResponse(Long id, String name, String city) {

    public static VenueResponse from(Venue venue) {
        return new VenueResponse(venue.getId(), venue.getName(), venue.getCity());
    }
}