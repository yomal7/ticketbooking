package com.yomal7.ticketbooking.venue;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByVenueId(Long venueId);
}