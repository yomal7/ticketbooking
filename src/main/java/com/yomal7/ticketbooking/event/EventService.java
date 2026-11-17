package com.yomal7.ticketbooking.event;

import com.yomal7.ticketbooking.common.ConflictException;
import com.yomal7.ticketbooking.common.NotFoundException;
import com.yomal7.ticketbooking.venue.Seat;
import com.yomal7.ticketbooking.venue.SeatRepository;
import com.yomal7.ticketbooking.venue.VenueRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final EventSeatRepository eventSeatRepository;
    private final VenueRepository venueRepository;
    private final SeatRepository seatRepository;

    public EventService(EventRepository eventRepository,
                        EventSeatRepository eventSeatRepository,
                        VenueRepository venueRepository,
                        SeatRepository seatRepository) {
        this.eventRepository = eventRepository;
        this.eventSeatRepository = eventSeatRepository;
        this.venueRepository = venueRepository;
        this.seatRepository = seatRepository;
    }

    @Transactional
    public EventResponse create(CreateEventRequest request) {
        venueRepository.findById(request.venueId())
                .orElseThrow(() -> new NotFoundException("Venue " + request.venueId() + " not found"));

        List<Seat> seats = seatRepository.findByVenueId(request.venueId());
        if (seats.isEmpty()) {
            throw new ConflictException("Venue " + request.venueId() + " has no seats; generate seats first");
        }

        Event event = eventRepository.save(new Event(request.venueId(), request.name(), request.startsAt()));

        List<EventSeat> inventory = seats.stream()
                .map(seat -> new EventSeat(event.getId(), seat.getId(), request.priceCents()))
                .toList();
        eventSeatRepository.saveAll(inventory);

        return EventResponse.from(event);
    }

    public List<EventResponse> list() {
        return eventRepository.findAll().stream().map(EventResponse::from).toList();
    }

    public EventResponse get(Long id) {
        return EventResponse.from(findEvent(id));
    }

    public List<EventSeatResponse> seats(Long eventId, EventSeatStatus status) {
        findEvent(eventId);

        List<EventSeat> rows;
        if (status == null) {
            rows = eventSeatRepository.findByEventIdOrderByIdAsc(eventId);
        } else if (status == EventSeatStatus.AVAILABLE) {
            rows = eventSeatRepository.findAvailableByEventId(eventId);
        } else {
            rows = eventSeatRepository.findByEventIdAndStatusOrderByIdAsc(eventId, status);
        }

        return rows.stream().map(EventSeatResponse::from).toList();
    }

    @Transactional
    public EventResponse publish(Long id){
        Event event = findEvent(id);
        if (event.getStatus() != EventStatus.DRAFT) {
            throw new ConflictException("Event " + id + " is " + event.getStatus()
                    + "; only DRAFT events can be published");
        }
        event.publish();
        return EventResponse.from(event);
    }

    private Event findEvent(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event " + id + " not found"));
    }
}