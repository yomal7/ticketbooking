package com.yomal7.ticketbooking.venue;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/venues")
public class VenueController {

    private final VenueRepository venueRepository;

    public VenueController(VenueRepository venueRepository) {
        this.venueRepository = venueRepository;
    }

    @PostMapping
    public ResponseEntity<VenueResponse> create(@Valid @RequestBody CreateVenueRequest request) {
        Venue saved = venueRepository.save(new Venue(request.name(), request.city()));
        return ResponseEntity.status(HttpStatus.CREATED).body(VenueResponse.from(saved));
    }

    @GetMapping
    public List<VenueResponse> list() {
        return venueRepository.findAll().stream()
                .map(VenueResponse::from)
                .toList();
    }
}