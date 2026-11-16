package com.yomal7.ticketbooking.venue;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/venues")
public class VenueController {

    private final VenueService venueService;

    public VenueController(VenueService venueService) {
        this.venueService = venueService;
    }

    @PostMapping
    public ResponseEntity<VenueResponse> create(@Valid @RequestBody CreateVenueRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(venueService.create(request));
    }

    @GetMapping
    public List<VenueResponse> list() {
        return venueService.list();
    }

    @PostMapping("/{venueId}/seats")
    public ResponseEntity<List<SeatResponse>> generateSeats(@PathVariable Long venueId,
                                                            @Valid @RequestBody GenerateSeatsRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(venueService.generateSeats(venueId, request));
    }
}