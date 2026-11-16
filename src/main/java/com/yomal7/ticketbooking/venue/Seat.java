package com.yomal7.ticketbooking.venue;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "seats")
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long venueId;

    private String section;

    private String rowLabel;

    private Integer seatNumber;

    protected Seat() {
    }

    public Seat(Long venueId, String section, String rowLabel, Integer seatNumber) {
        this.venueId = venueId;
        this.section = section;
        this.rowLabel = rowLabel;
        this.seatNumber = seatNumber;
    }

    public Long getId() { return id; }
    public Long getVenueId() { return venueId; }
    public String getSection() { return section; }
    public String getRowLabel() { return rowLabel; }
    public Integer getSeatNumber() { return seatNumber; }
}