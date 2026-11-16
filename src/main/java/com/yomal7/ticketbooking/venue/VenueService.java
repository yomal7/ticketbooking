package com.yomal7.ticketbooking.venue;

import com.yomal7.ticketbooking.common.ConflictException;
import com.yomal7.ticketbooking.common.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VenueService {

    private final VenueRepository venueRepository;
    private final SeatRepository seatRepository;

    public VenueService(VenueRepository venueRepository, SeatRepository seatRepository) {
        this.venueRepository = venueRepository;
        this.seatRepository = seatRepository;
    }

    public VenueResponse create(CreateVenueRequest request) {
        Venue saved = venueRepository.save(new Venue(request.name(), request.city()));
        return VenueResponse.from(saved);
    }

    public List<VenueResponse> list() {
        return venueRepository.findAll().stream().map(VenueResponse::from).toList();
    }

    @Transactional
    public List<SeatResponse> generateSeats(Long venueId, GenerateSeatsRequest request) {
        venueRepository.findById(venueId)
                .orElseThrow(() -> new NotFoundException("Venue " + venueId + " not found"));

        List<Seat> seats = new ArrayList<>();
        for (int row = 0; row < request.rows(); row++) {
            String rowLabel = String.valueOf((char) ('A' + row));
            for (int number = 1; number <= request.seatsPerRow(); number++) {
                seats.add(new Seat(venueId, request.section(), rowLabel, number));
            }
        }

        try {
            return seatRepository.saveAll(seats).stream().map(SeatResponse::from).toList();
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Section " + request.section() + " already exists for venue " + venueId);
        }
    }
}