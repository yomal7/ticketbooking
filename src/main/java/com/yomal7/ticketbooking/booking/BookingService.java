package com.yomal7.ticketbooking.booking;

import com.yomal7.ticketbooking.common.BadRequestException;
import com.yomal7.ticketbooking.common.ConflictException;
import com.yomal7.ticketbooking.common.NotFoundException;
import com.yomal7.ticketbooking.common.SeatsUnavailableException;
import com.yomal7.ticketbooking.event.Event;
import com.yomal7.ticketbooking.event.EventRepository;
import com.yomal7.ticketbooking.event.EventSeat;
import com.yomal7.ticketbooking.event.EventSeatRepository;
import com.yomal7.ticketbooking.event.EventSeatStatus;
import com.yomal7.ticketbooking.event.EventStatus;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.List;

import com.yomal7.ticketbooking.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {

    private static final int HOLD_MINUTES = 10;

    private final BookingRepository bookingRepository;
    private final EventRepository eventRepository;
    private final EventSeatRepository eventSeatRepository;
    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository,
                          EventRepository eventRepository,
                          EventSeatRepository eventSeatRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.eventRepository = eventRepository;
        this.eventSeatRepository = eventSeatRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public BookingResponse create(Long eventId, Long userId, CreateBookingRequest request) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User " + userId + " not found");
        }
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event " + eventId + " not found"));
        if (event.getStatus() != EventStatus.ON_SALE) {
            throw new ConflictException("Event " + eventId + " is not ON_SALE");
        }
        List<Long> ids = request.eventSeatIds();
        if (new HashSet<>(ids).size() != ids.size()) {
            throw new BadRequestException("eventSeatIds contains duplicates");
        }

        OffsetDateTime expiresAt = OffsetDateTime.now().plusMinutes(HOLD_MINUTES);
        Booking booking = bookingRepository.save(new Booking(userId, eventId, ids.size(), expiresAt));

        int held = eventSeatRepository.holdSeats(eventId, ids, booking.getId(), expiresAt);
        if (held != ids.size()) {
            throw new SeatsUnavailableException("One or more requested seats are no longer available");
        }

        long total = eventSeatRepository.findByBookingId(booking.getId()).stream()
                .mapToLong(EventSeat::getPriceCents)
                .sum();
        booking.setTotalCents(total);
        bookingRepository.save(booking);

        return BookingResponse.from(booking);
    }

    @Transactional
    public BookingResponse confirm(Long bookingId, Long userId) {
        Booking booking = findBooking(bookingId);
        requireOwner(booking, userId);
        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            return BookingResponse.from(booking);
        }
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new ConflictException("Booking " + bookingId + " is " + booking.getStatus()
                    + " and cannot be confirmed");
        }
        int booked = eventSeatRepository.confirmSeats(bookingId);
        if (booked != booking.getSeatCount()) {
            throw new ConflictException("Hold expired for booking " + bookingId);
        }
        booking.confirm();
        bookingRepository.save(booking);
        return BookingResponse.from(booking);
    }

    @Transactional
    public BookingResponse cancel(Long bookingId, Long userId) {
        Booking booking = findBooking(bookingId);
        requireOwner(booking, userId);
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return BookingResponse.from(booking);
        }
        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new ConflictException("Booking " + bookingId + " is " + booking.getStatus()
                    + " and cannot be cancelled");
        }
        eventSeatRepository.releaseSeats(bookingId);
        booking.cancel();
        bookingRepository.save(booking);
        return BookingResponse.from(booking);
    }

    public BookingResponse get(Long bookingId, Long userId) {
        Booking booking = findBooking(bookingId);
        requireOwner(booking, userId);
        return BookingResponse.from(booking);
    }

    private Booking findBooking(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Booking " + id + " not found"));
    }

    private void requireOwner(Booking booking, Long userId) {
        if (!booking.getUserId().equals(userId)) {
            throw new NotFoundException("Booking " + booking.getId() + " not found");
        }
    }
}