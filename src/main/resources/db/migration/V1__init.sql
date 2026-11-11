CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       email VARCHAR(150) NOT NULL UNIQUE,
                       password_hash VARCHAR(100) NOT NULL,
                       role VARCHAR(20) NOT NULL DEFAULT 'USER',
                       created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE venues (
                        id BIGSERIAL PRIMARY KEY,
                        name VARCHAR(100) NOT NULL,
                        city VARCHAR(100) NOT NULL
);

CREATE TABLE seats (               -- physical seats of a venue
                       id BIGSERIAL PRIMARY KEY,
                       venue_id BIGINT NOT NULL REFERENCES venues(id),
                       section VARCHAR(20) NOT NULL,
                       row_label VARCHAR(5) NOT NULL,
                       seat_number INT NOT NULL,
                       UNIQUE (venue_id, section, row_label, seat_number)
);

CREATE TABLE events (
                        id BIGSERIAL PRIMARY KEY,
                        venue_id BIGINT NOT NULL REFERENCES venues(id),
                        name VARCHAR(150) NOT NULL,
                        starts_at TIMESTAMPTZ NOT NULL,
                        status VARCHAR(20) NOT NULL DEFAULT 'DRAFT'  -- DRAFT, ON_SALE, CLOSED
);

CREATE TABLE bookings (
                          id BIGSERIAL PRIMARY KEY,
                          user_id BIGINT NOT NULL REFERENCES users(id),
                          event_id BIGINT NOT NULL REFERENCES events(id),
                          status VARCHAR(20) NOT NULL,     -- PENDING, CONFIRMED, CANCELLED, EXPIRED
                          total_cents BIGINT NOT NULL,
                          created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE event_seats (         -- one row per seat per event: the hot table
                             id BIGSERIAL PRIMARY KEY,
                             event_id BIGINT NOT NULL REFERENCES events(id),
                             seat_id BIGINT NOT NULL REFERENCES seats(id),
                             price_cents INT NOT NULL,
                             status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',  -- AVAILABLE, HELD, BOOKED
                             booking_id BIGINT REFERENCES bookings(id),
                             hold_expires_at TIMESTAMPTZ,
                             UNIQUE (event_id, seat_id)
);
CREATE INDEX idx_event_seats_event_status ON event_seats (event_id, status);