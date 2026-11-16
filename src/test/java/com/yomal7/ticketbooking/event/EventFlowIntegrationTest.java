package com.yomal7.ticketbooking.event;

import static org.assertj.core.api.Assertions.assertThat;

import com.yomal7.ticketbooking.TestcontainersConfiguration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class EventFlowIntegrationTest {

    @LocalServerPort
    int port;

    RestClient client;

    @BeforeEach
    void setUp() {
        client = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .defaultStatusHandler(HttpStatusCode::isError, (req, res) -> { })
                .build();
    }

    @Test
    void eventGetsOneSeatPerVenueSeat() {
        long venueId = postAndGetId("/api/venues",
                """
                {"name": "Test Arena", "city": "Colombo"}""");

        ResponseEntity<String> seats = post("/api/venues/" + venueId + "/seats",
                """
                {"section": "A", "rows": 2, "seatsPerRow": 3}""");
        assertThat(seats.getStatusCode().is2xxSuccessful()).isTrue();

        long eventId = postAndGetId("/api/events",
                """
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
        long venueId = postAndGetId("/api/venues",
                """
                {"name": "Another Arena", "city": "Kandy"}""");
        String body = """
                {"section": "B", "rows": 1, "seatsPerRow": 2}""";

        post("/api/venues/" + venueId + "/seats", body);
        ResponseEntity<String> second = post("/api/venues/" + venueId + "/seats", body);

        assertThat(second.getStatusCode().value()).isEqualTo(409);
    }

    @Test
    void unknownVenueReturns404() {
        ResponseEntity<String> response = post("/api/venues/999999/seats",
                """
                {"section": "A", "rows": 1, "seatsPerRow": 1}""");

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    // ---- helpers ----

    private ResponseEntity<String> post(String path, String json) {
        return client.post().uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .body(json)
                .retrieve().toEntity(String.class);
    }

    private long postAndGetId(String path, String json) {
        String body = post(path, json).getBody();
        Matcher m = Pattern.compile("\"id\"\\s*:\\s*(\\d+)").matcher(body);
        assertThat(m.find()).as("response should contain an id: " + body).isTrue();
        return Long.parseLong(m.group(1));
    }

    private int count(String text, String needle) {
        return text.split(Pattern.quote(needle), -1).length - 1;
    }
}