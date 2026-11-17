package com.yomal7.ticketbooking.booking;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private Long eventId;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private Long totalCents;

    private OffsetDateTime createdAt;

    private Integer seatCount;

    private OffsetDateTime expiresAt;

    protected Booking() {
    }

    public Booking(Long userId, Long eventId, Integer seatCount, OffsetDateTime expiresAt) {
        this.userId = userId;
        this.eventId = eventId;
        this.seatCount = seatCount;
        this.expiresAt = expiresAt;
        this.status = BookingStatus.PENDING;
        this.totalCents = 0L;
        this.createdAt = OffsetDateTime.now();
    }

    public void confirm() { this.status = BookingStatus.CONFIRMED; }
    public void cancel() { this.status = BookingStatus.CANCELLED; }
    public void setTotalCents(Long totalCents) { this.totalCents = totalCents; }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getEventId() { return eventId; }
    public BookingStatus getStatus() { return status; }
    public Long getTotalCents() { return totalCents; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public Integer getSeatCount() { return seatCount; }
    public OffsetDateTime getExpiresAt() { return expiresAt; }
}