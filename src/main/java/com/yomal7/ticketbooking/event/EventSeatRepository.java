package com.yomal7.ticketbooking.event;

import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventSeatRepository extends JpaRepository<EventSeat, Long> {

    List<EventSeat> findByEventIdOrderByIdAsc(Long eventId);

    List<EventSeat> findByEventIdAndStatusOrderByIdAsc(Long eventId, EventSeatStatus status);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            UPDATE event_seats
            SET status = 'HELD', booking_id = :bookingId, hold_expires_at = :expiresAt
            WHERE event_id = :eventId
              AND id IN :eventSeatIds
              AND (status = 'AVAILABLE' OR (status = 'HELD' AND hold_expires_at < now()))
            """, nativeQuery = true)
    int holdSeats(@Param("eventId") Long eventId,
                  @Param("eventSeatIds") List<Long> eventSeatIds,
                  @Param("bookingId") Long bookingId,
                  @Param("expiresAt") OffsetDateTime expiresAt);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            UPDATE event_seats
            SET status = 'BOOKED', hold_expires_at = NULL
            WHERE booking_id = :bookingId AND status = 'HELD' AND hold_expires_at > now()
            """, nativeQuery = true)
    int confirmSeats(@Param("bookingId") Long bookingId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            UPDATE event_seats
            SET status = 'AVAILABLE', booking_id = NULL, hold_expires_at = NULL
            WHERE booking_id = :bookingId AND status = 'HELD'
            """, nativeQuery = true)
    int releaseSeats(@Param("bookingId") Long bookingId);

    @Query(value = """
            SELECT * FROM event_seats
            WHERE event_id = :eventId
              AND (status = 'AVAILABLE' OR (status = 'HELD' AND hold_expires_at < now()))
              ORDER BY id ASC
            """, nativeQuery = true)
    List<EventSeat> findAvailableByEventId(@Param("eventId") Long eventId);

    List<EventSeat> findByBookingId(Long bookingId);
}