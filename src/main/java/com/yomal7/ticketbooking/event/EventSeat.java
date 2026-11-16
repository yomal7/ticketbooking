package com.yomal7.ticketbooking.event;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "event_seats")
public class EventSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long eventId;

    private Long seatId;

    private Integer priceCents;

    @Enumerated(EnumType.STRING)
    private EventSeatStatus status;

    private Long bookingId;

    private OffsetDateTime holdExpiresAt;

    protected EventSeat() {
    }

    public EventSeat(Long eventId, Long seatId, Integer priceCents) {
        this.eventId = eventId;
        this.seatId = seatId;
        this.priceCents = priceCents;
        this.status = EventSeatStatus.AVAILABLE;
    }

    public Long getId() { return id; }
    public Long getEventId() { return eventId; }
    public Long getSeatId() { return seatId; }
    public Integer getPriceCents() { return priceCents; }
    public EventSeatStatus getStatus() { return status; }
    public Long getBookingId() { return bookingId; }
    public OffsetDateTime getHoldExpiresAt() { return holdExpiresAt; }
}