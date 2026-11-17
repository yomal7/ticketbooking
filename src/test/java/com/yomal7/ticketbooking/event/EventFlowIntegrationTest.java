package com.yomal7.ticketbooking.event;

import static org.assertj.core.api.Assertions.assertThat;

import com.yomal7.ticketbooking.AbstractIntegrationTest;
import com.yomal7.ticketbooking.TestcontainersConfiguration;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class EventFlowIntegrationTest extends AbstractIntegrationTest {

    @Test
    void eventGetsOneSeatPerVenueSeat() {
        long venueId = postAndGetId("/api/venues", """
                {"name": "Test Arena", "city": "Colombo"}""");

        ResponseEntity<String> seats = post("/api/venues/" + venueId + "/seats", """
                {"section": "A", "rows": 2, "seatsPerRow": 3}""");
        assertThat(seats.getStatusCode().is2xxSuccessful()).isTrue();

        long eventId = postAndGetId("/api/events", """
                {"venueId": %d, "name": "Test Event",
                 "startsAt": "2026-12-20T19:00:00+05:30", "priceCents": 5000}"""
                .formatted(venueId));

        ResponseEntity<String> available = client.get()
                .uri("/api/events/{id}/seats?status=AVAILABLE", eventId)
                .retrieve().toEntity(String.class);

        assertThat(count(available.getBody(), "\"seatId\"")).isEqualTo(6);
    }

    @Test
    void generatingTheSameSectionTwiceReturns409() {
        long venueId = postAndGetId("/api/venues", """
                {"name": "Another Arena", "city": "Kandy"}""");
        String body = """
                {"section": "B", "rows": 1, "seatsPerRow": 2}""";

        post("/api/venues/" + venueId + "/seats", body);
        ResponseEntity<String> second = post("/api/venues/" + venueId + "/seats", body);

        assertThat(second.getStatusCode().value()).isEqualTo(409);
    }

    @Test
    void unknownVenueReturns404() {
        ResponseEntity<String> response = post("/api/venues/999999/seats", """
                {"section": "A", "rows": 1, "seatsPerRow": 1}""");

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    private int count(String text, String needle) {
        return text.split(Pattern.quote(needle), -1).length - 1;
    }
}