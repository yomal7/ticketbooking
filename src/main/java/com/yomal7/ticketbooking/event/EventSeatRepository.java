package com.yomal7.ticketbooking.event;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventSeatRepository extends JpaRepository<EventSeat, Long> {

    List<EventSeat> findByEventId(Long eventId);

    List<EventSeat> findByEventIdAndStatus(Long eventId, EventSeatStatus status);
}