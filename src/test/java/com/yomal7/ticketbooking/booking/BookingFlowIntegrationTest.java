package com.yomal7.ticketbooking.booking;

import static org.assertj.core.api.Assertions.assertThat;

import com.yomal7.ticketbooking.AbstractIntegrationTest;
import com.yomal7.ticketbooking.TestcontainersConfiguration;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class BookingFlowIntegrationTest extends AbstractIntegrationTest {

    @Test
    void bookingCanBeCreatedAndConfirmed() {
        long eventId = publishedEvent(1, 1);
        long userId = createUser();
        List<Long> seats = allAvailable(eventId);

        ResponseEntity<String> created = tryBooking(eventId, userId, seats);
        assertThat(created.getStatusCode().value()).isEqualTo(201);
        assertThat(created.getBody()).contains("\"status\":\"PENDING\"").contains("\"seatCount\":1");
        long bookingId = idOf(created.getBody());

        ResponseEntity<String> confirmed = confirm(bookingId, userId);
        assertThat(confirmed.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(confirmed.getBody()).contains("\"status\":\"CONFIRMED\"");

        assertThat(allAvailable(eventId)).isEmpty(); // proof the seat is BOOKED, not just response text

        ResponseEntity<String> again = confirm(bookingId, userId); // idempotent
        assertThat(again.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(again.getBody()).contains("\"status\":\"CONFIRMED\"");
    }

    @Test
    void cancelledBookingReleasesSeatBackToAvailable() {
        long eventId = publishedEvent(1, 1);
        long userId = createUser();
        long seatId = allAvailable(eventId).getFirst();
        long bookingId = idOf(tryBooking(eventId, userId, List.of(seatId)).getBody());

        ResponseEntity<String> cancelled = cancel(bookingId, userId);
        assertThat(cancelled.getBody()).contains("\"status\":\"CANCELLED\"");
        assertThat(allAvailable(eventId)).containsExactly(seatId);
    }

    @Test
    void failedBookingHoldsNothing() {
        long eventId = publishedEvent(1, 3);
        long userA = createUser();
        long userB = createUser();
        List<Long> seats = allAvailable(eventId);

        assertThat(tryBooking(eventId, userA, List.of(seats.get(0))).getStatusCode().value()).isEqualTo(201);
        ResponseEntity<String> r = tryBooking(eventId, userB, List.of(seats.get(0), seats.get(1)));

        assertThat(r.getStatusCode().value()).isEqualTo(409);
        assertThat(allAvailable(eventId)).containsExactlyInAnyOrder(seats.get(1), seats.get(2));
    }

    @Test
    void expiredHoldCannotBeConfirmedAndSeatCanBeTakenOver() {
        long eventId = publishedEvent(1, 1);
        long userA = createUser();
        long userB = createUser();
        List<Long> seats = allAvailable(eventId);

        ResponseEntity<String> first = tryBooking(eventId, userA, seats);
        long bookingId = idOf(first.getBody());

        jdbc.update("UPDATE event_seats SET hold_expires_at = now() - interval '1 minute' "
                + "WHERE booking_id = ?", bookingId);

        assertThat(confirm(bookingId, userA).getStatusCode().value()).isEqualTo(409);
        assertThat(tryBooking(eventId, userB, seats).getStatusCode().value()).isEqualTo(201);
    }

    @Test
    void nonOwnerConfirmIsNotFound() {
        long eventId = publishedEvent(1, 1);
        long owner = createUser();
        long stranger = createUser();
        long bookingId = idOf(tryBooking(eventId, owner, allAvailable(eventId)).getBody());

        assertThat(confirm(bookingId, stranger).getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void cancellingConfirmedBookingIsConflict() {
        long eventId = publishedEvent(1, 1);
        long userId = createUser();
        long bookingId = idOf(tryBooking(eventId, userId, allAvailable(eventId)).getBody());
        confirm(bookingId, userId);

        assertThat(cancel(bookingId, userId).getStatusCode().value()).isEqualTo(409);
    }

    @Test
    void unknownUserIsNotFound() {
        long eventId = publishedEvent(1, 1);

        assertThat(tryBooking(eventId, 999_999L, allAvailable(eventId)).getStatusCode().value())
                .isEqualTo(404);
    }

    @Test
    void invalidSeatListsAreRejected() {
        long eventId = publishedEvent(1, 7);
        long userId = createUser();
        List<Long> seats = allAvailable(eventId);

        assertThat(tryBooking(eventId, userId, List.of()).getStatusCode().value()).isEqualTo(400);
        assertThat(tryBooking(eventId, userId, seats).getStatusCode().value()).isEqualTo(400); // 7 > 6
        assertThat(tryBooking(eventId, userId, List.of(seats.getFirst(), seats.getFirst()))
                .getStatusCode().value()).isEqualTo(400); // duplicates

        assertThat(allAvailable(eventId)).hasSize(7); // nothing was held by any rejected request
    }
}