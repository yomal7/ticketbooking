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
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long venueId;

    private String name;

    private OffsetDateTime startsAt;

    @Enumerated(EnumType.STRING)
    private EventStatus status;

    protected Event() {
    }

    public Event(Long venueId, String name, OffsetDateTime startsAt) {
        this.venueId = venueId;
        this.name = name;
        this.startsAt = startsAt;
        this.status = EventStatus.DRAFT;
    }

    public Long getId() { return id; }
    public Long getVenueId() { return venueId; }
    public String getName() { return name; }
    public OffsetDateTime getStartsAt() { return startsAt; }
    public EventStatus getStatus() { return status; }

    public void publish(){
        this.status = EventStatus.ON_SALE;
    }
}