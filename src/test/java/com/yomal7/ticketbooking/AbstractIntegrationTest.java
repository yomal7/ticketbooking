package com.yomal7.ticketbooking;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.client.RestClient;

public abstract class AbstractIntegrationTest {

    @LocalServerPort
    protected int port;

    @Autowired
    protected JdbcTemplate jdbc;

    protected RestClient client;

    @BeforeEach
    void setUpClient() {
        client = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .defaultStatusHandler(HttpStatusCode::isError, (req, res) -> { })
                .build();
    }

    protected ResponseEntity<String> post(String path, String json) {
        return client.post().uri(path)
                .contentType(MediaType.APPLICATION_JSON)
                .body(json)
                .retrieve().toEntity(String.class);
    }

    protected long postAndGetId(String path, String json) {
        return idOf(post(path, json).getBody());
    }

    protected long idOf(String body) {
        Matcher m = Pattern.compile("\"id\"\\s*:\\s*(\\d+)").matcher(body);
        assertThat(m.find()).as("response should contain an id: " + body).isTrue();
        return Long.parseLong(m.group(1));
    }

    protected long createUser() {
        return postAndGetId("/api/users",
                "{\"email\":\"user-" + UUID.randomUUID() + "@test.com\"}");
    }

    protected long publishedEvent(int rows, int seatsPerRow) {
        long venueId = postAndGetId("/api/venues", """
                {"name":"Arena","city":"Colombo"}""");
        ResponseEntity<String> seats = post("/api/venues/" + venueId + "/seats",
                "{\"section\":\"A\",\"rows\":%d,\"seatsPerRow\":%d}".formatted(rows, seatsPerRow));
        assertThat(seats.getStatusCode().is2xxSuccessful()).isTrue();
        long eventId = postAndGetId("/api/events", """
                {"venueId": %d, "name":"Show",
                 "startsAt":"2026-12-20T19:00:00+05:30", "priceCents":5000}""".formatted(venueId));
        ResponseEntity<String> published = client.post()
                .uri("/api/events/{id}/publish", eventId).retrieve().toEntity(String.class);
        assertThat(published.getStatusCode().is2xxSuccessful()).isTrue();
        return eventId;
    }

    protected List<Long> allAvailable(long eventId) {
        String body = client.get().uri("/api/events/{id}/seats?status=AVAILABLE", eventId)
                .retrieve().body(String.class);
        List<Long> ids = new ArrayList<>();
        Matcher m = Pattern.compile("\"id\"\\s*:\\s*(\\d+)").matcher(body);
        while (m.find()) {
            ids.add(Long.parseLong(m.group(1)));
        }
        return ids;
    }

    protected ResponseEntity<String> tryBooking(long eventId, long userId, List<Long> seatIds) {
        return client.post().uri("/api/events/{id}/bookings", eventId)
                .header("X-User-Id", String.valueOf(userId))
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"eventSeatIds\":" + seatIds + "}") // List prints as [1, 2] — valid JSON
                .retrieve().toEntity(String.class);
    }

    protected ResponseEntity<String> confirm(long bookingId, long userId) {
        return client.post().uri("/api/bookings/{id}/confirm", bookingId)
                .header("X-User-Id", String.valueOf(userId))
                .retrieve().toEntity(String.class);
    }

    protected ResponseEntity<String> cancel(long bookingId, long userId) {
        return client.delete().uri("/api/bookings/{id}", bookingId)
                .header("X-User-Id", String.valueOf(userId))
                .retrieve().toEntity(String.class);
    }
}