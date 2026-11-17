package com.yomal7.ticketbooking.booking;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping("/events/{eventId}/bookings")
    public ResponseEntity<BookingResponse> create(@PathVariable Long eventId,
                                                  @RequestHeader("X-User-Id") Long userId,
                                                  @Valid @RequestBody CreateBookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.create(eventId, userId, request));
    }

    @PostMapping("/bookings/{id}/confirm")
    public BookingResponse confirm(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId) {
        return bookingService.confirm(id, userId);
    }

    @DeleteMapping("/bookings/{id}")
    public BookingResponse cancel(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId) {
        return bookingService.cancel(id, userId);
    }

    @GetMapping("/bookings/{id}")
    public BookingResponse get(@PathVariable Long id, @RequestHeader("X-User-Id") Long userId) {
        return bookingService.get(id, userId);
    }
}